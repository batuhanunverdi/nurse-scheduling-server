package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.entity.shift.Shift;
import com.example.nurseschedulingserver.repository.ShiftRepository;
import com.example.nurseschedulingserver.repository.WorkDayRepository;
import com.example.nurseschedulingserver.service.interfaces.CPService;
import com.example.nurseschedulingserver.service.interfaces.DepartmentService;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CPServiceImpl implements CPService {
    private final NurseService nurseService;
    private final DepartmentService departmentService;
    private final ShiftRepository shiftRepository;
    private final WorkDayRepository workDayRepository;

    public void createShiftsByDepartment() {
        Loader.loadNativeLibraries();
        List<Department> departments = departmentService.getAllDepartments();
        LocalDate currentDate = LocalDate.now();
        LocalDate nextDate = currentDate.plusMonths(1);
        int nextMonthDays = nextDate.lengthOfMonth();
        departments.forEach(department -> {
            List<Nurse> nurseList = nurseService.getNursesByDepartment(department.getId());
            if (nurseList.size() == 0 || !department.getName().equals("Acil Servis"))
                return;

            final int numNurses = nurseList.size();
            final int numShifts = 3;
            final int[] allNurses = IntStream.range(0, numNurses).toArray();
            final int[] allDays = IntStream.range(0, nextMonthDays).toArray();
            final int[] allShifts = IntStream.range(0, numShifts).toArray();

            CpModel model = new CpModel();

            Literal[][][] shifts = new Literal[numNurses][nextMonthDays][numShifts];
            for (int n : allNurses) {
                for (int d : allDays) {
                    for (int s : allShifts) {
                            shifts[n][d][s] = model.newBoolVar("shifts_n" + n + "d" + d + "s" + s);
                    }
                }
            }
            for (int d : allDays) {
                LinearExprBuilder totalNursesInShift0 = LinearExpr.newBuilder();
                LinearExprBuilder totalNursesInShift1 = LinearExpr.newBuilder();
                LinearExprBuilder totalNursesInShift2 = LinearExpr.newBuilder();
                LocalDate date = nextDate.withDayOfMonth(d + 1);
                for (int n : allNurses) {
                    if(!isWeekend(date)){
                        totalNursesInShift0.addTerm(shifts[n][d][0], 1);
                        totalNursesInShift1.addTerm(shifts[n][d][1], 1);
                        model.addEquality(shifts[n][d][2], 0);
                    }
                    else{
                        totalNursesInShift2.addTerm(shifts[n][d][2], 1);
                        model.addEquality(shifts[n][d][0], 0);
                        model.addEquality(shifts[n][d][1], 0);
                    }

                }
                if (!isWeekend(date)) {
                    model.addGreaterOrEqual(totalNursesInShift0.build(), 3);
                    model.addEquality(totalNursesInShift1.build(), 2);
                } else {
                    model.addEquality(totalNursesInShift2.build(), 2);
                }
            }
            for (int n : allNurses) {
                LinearExprBuilder totalHoursWorked = LinearExpr.newBuilder();
                for (int d : allDays) {
                    for (int s : allShifts) {
                        totalHoursWorked.addTerm(shifts[n][d][s], shiftDuration(s));
                    }
                }
                model.addGreaterOrEqual(totalHoursWorked.build(), 168);
            }

            for (int n : allNurses) {
                for (int d : allDays) {
                    model.addImplication(shifts[n][d][0], shifts[n][d][1].not());
                    model.addImplication(shifts[n][d][0], shifts[n][d][2].not());
                    model.addImplication(shifts[n][d][1], shifts[n][d][0].not());
                    model.addImplication(shifts[n][d][1], shifts[n][d][2].not());
                    model.addImplication(shifts[n][d][2], shifts[n][d][0].not());
                    model.addImplication(shifts[n][d][2], shifts[n][d][1].not());
                    if (d < allDays.length - 1) {
                        model.addImplication(shifts[n][d][1], shifts[n][d + 1][0].not());
                        model.addImplication(shifts[n][d][1], shifts[n][d + 1][1].not());
                        model.addImplication(shifts[n][d][1], shifts[n][d + 1][2].not());
                    }
                    if (d < allDays.length - 2) {
                        model.addImplication(shifts[n][d][2], shifts[n][d + 1][0].not());
                        model.addImplication(shifts[n][d][2], shifts[n][d + 1][1].not());
                        model.addImplication(shifts[n][d][2], shifts[n][d + 1][2].not());
                        model.addImplication(shifts[n][d][2], shifts[n][d + 2][0].not());
                        model.addImplication(shifts[n][d][2], shifts[n][d + 2][1].not());
                        model.addImplication(shifts[n][d][2], shifts[n][d + 2][2].not());
                    }
                }
            }



            final int solutionLimit = 1;
            CpSolver solver = new CpSolver();
            solver.getParameters().setLinearizationLevel(0);
            solver.getParameters().setRandomizeSearch(true);
            solver.getParameters().setEnumerateAllSolutions(true);
            solver.getParameters().setRandomSeed((int) System.currentTimeMillis());
            solver.getParameters().setRandomBranchesRatio(12.0);

            class VarArraySolutionPrinterWithLimit extends CpSolverSolutionCallback {
                private final List<Nurse> nurseList;
                public VarArraySolutionPrinterWithLimit(List<Nurse> nurseList,
                                                        int[] allNurses, int[] allDays, int[] allShifts, Literal[][][] shifts, int limit) {
                    this.nurseList = nurseList;
                    solutionCount = 0;
                    this.allNurses = allNurses;
                    this.allDays = allDays;
                    this.allShifts = allShifts;
                    this.shifts = shifts;
                    solutionLimit = limit;
                }

                @Override
                public void onSolutionCallback() {
                    List<Shift> shiftList = new ArrayList<>();
                    for (int d : allDays) {
                        System.out.printf("Day %d:%n", d);
                        for (int s : allShifts) {
                            System.out.printf("  Shift %d:%n", s);
                            for (int n : allNurses) {
                                Nurse nurse = nurseList.get(n);
                                if (booleanValue(shifts[n][d][s])) {
                                    int startHour = calculateStartHour(s);
                                    int endHour = (startHour + shiftDuration(s))%24;
                                    System.out.printf("    Nurse %d (%s): %02d:00 - %02d:00%n", n, nurse.getFirstName() , startHour, endHour);
                                    Shift shift = new Shift();
                                    shift.setNurseId(nurse.getId());
                                    Calendar calendar = Calendar.getInstance();
                                    LocalDate date = LocalDate.now();
                                    calendar.set(date.getYear(),date.getMonthValue(),d+1,startHour,0);
                                    shift.setStartDate(calendar.getTime());
                                    calendar.set(date.getYear(),date.getMonthValue(),d+1,endHour,0);
                                    shift.setEndDate(calendar.getTime());
                                    shiftList.add(shift);
                                }
                            }
                        }
                    }
                    System.out.println(shiftList.size());
                    shiftRepository.saveAll(shiftList);
                    solutionCount++;
                    if (solutionCount >= solutionLimit) {
                        System.out.printf("Stop search after %d solutions%n", solutionLimit);
                        stopSearch();
                    }
                }

                private int calculateStartHour(int shiftIndex) {
                    int[] startHours = {8, 16, 8};
                    return startHours[shiftIndex];
                }

                public int getSolutionCount() {
                    return solutionCount;
                }

                private int solutionCount;
                private final int[] allNurses;
                private final int[] allDays;
                private final int[] allShifts;
                private final Literal[][][] shifts;
                private final int solutionLimit;
            }

            VarArraySolutionPrinterWithLimit cb = new VarArraySolutionPrinterWithLimit(nurseList,allNurses, allDays, allShifts, shifts, solutionLimit);

            System.out.println("Department: " + department.getName());

            CpSolverStatus status = solver.solve(model, cb);
            System.out.println("Status: " + status);
            System.out.println(cb.getSolutionCount() + " solutions found.");

            System.out.println("Statistics");
            System.out.printf("  conflicts: %d%n", solver.numConflicts());
            System.out.printf("  branches : %d%n", solver.numBranches());
            System.out.printf("  wall time: %f s%n", solver.wallTime());
        });
    }

    private int shiftDuration(int shiftIndex) {
        int[] durations = {8,16,24};
        return durations[shiftIndex];
    }

    private boolean isWeekend(LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;
    }

}
