package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.service.interfaces.CPService;
import com.example.nurseschedulingserver.service.interfaces.DepartmentService;
import com.example.nurseschedulingserver.service.interfaces.NurseService;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CPServiceImpl implements CPService {
    private final NurseService nurseService;
    private final DepartmentService departmentService;

    public void createShiftsByDepartment() {
        Loader.loadNativeLibraries();
        List<Department> departments = departmentService.getAllDepartments();
        LocalDate currentDate = LocalDate.now();
        int nextMonthDays = currentDate.lengthOfMonth();
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

            // Vardiyaların tanımlanması
            Literal[][][] shifts = new Literal[numNurses][nextMonthDays][numShifts];
            for (int n : allNurses) {
                for (int d : allDays) {
                    for (int s : allShifts) {
                        shifts[n][d][s] = model.newBoolVar("shifts_n" + n + "d" + d + "s" + s);
                    }
                }
            }

            // Kısıtların eklenmesi

            // Her bir hemşire 1 ayda minimum 168 saat çalışmalı
            for (int n : allNurses) {
                LinearExprBuilder totalHoursWorked = LinearExpr.newBuilder();
                for (int d : allDays) {
                    for (int s : allShifts) {
                        // Vardiyada çalışıyorsa saat sayısını hesaba kat
                        totalHoursWorked.addTerm(shifts[n][d][s], shiftDuration(s));
                    }
                }
                model.addGreaterOrEqual(totalHoursWorked.build(), 168);
            }

            // Her gün vardiya 0 da en az 4 hemşire çalışmalı
            for (int d : allDays) {
                LinearExprBuilder totalNursesInShift0 = LinearExpr.newBuilder();
                for (int n : allNurses) {
                    totalNursesInShift0.addTerm(shifts[n][d][0], 1);
                }
                model.addGreaterOrEqual(totalNursesInShift0.build(), 4);
            }
            // Her gün vardiya 1 da en az 2 hemşire çalışmalı

            for (int d : allDays) {
                LinearExprBuilder totalNursesInShift0 = LinearExpr.newBuilder();
                for (int n : allNurses) {
                    totalNursesInShift0.addTerm(shifts[n][d][1], 1);
                }
                model.addGreaterOrEqual(totalNursesInShift0.build(), 2);
            }
            // Her gün vardiya 2 da en az 1 hemşire çalışmalı
            for (int d : allDays) {
                LinearExprBuilder totalNursesInShift0 = LinearExpr.newBuilder();
                for (int n : allNurses) {
                    totalNursesInShift0.addTerm(shifts[n][d][2], 1);
                }
                model.addGreaterOrEqual(totalNursesInShift0.build(), 1);
            }


            // Bir Hemşire Vardiya 1  çalışıyorsa ertesi gün hiç bir vardiyada çalışamaz. İzinlidir
            for (int n : allNurses) {
                for (int d : allDays) {
                    if (d < allDays.length - 1) {
                        model.addImplication(shifts[n][d][1], shifts[n][d + 1][0].not());
                        model.addImplication(shifts[n][d][1], shifts[n][d + 1][1].not());
                        model.addImplication(shifts[n][d][1], shifts[n][d + 1][2].not());
                    }
                }
            }
            // Bir Hemşire Vardiya 2  çalışıyorsa ertesi 2 gün hiç bir vardiyada çalışamaz. İzinlidir
            for (int n : allNurses) {
                for (int d : allDays) {
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
            //Eğer bir hemşire bir gün içerisinde bir vardiyada çalışıyorsa aynı gün içerisinde başka bir vardiyada çalışamaz



            CpSolver solver = new CpSolver();
            solver.getParameters().setLinearizationLevel(0);
            // Tüm çözümleri sırala
            solver.getParameters().setEnumerateAllSolutions(true);
            final int solutionLimit = 1;
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
                                }
                            }
                        }
                    }
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

            // Solver oluştur ve modeli çöz
            CpSolverStatus status = solver.solve(model, cb);
            System.out.println("Status: " + status);
            System.out.println(cb.getSolutionCount() + " solutions found.");

            // İstatistikler
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

}
