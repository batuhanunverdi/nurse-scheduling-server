package com.example.nurseschedulingserver.service.implementations;

import com.example.nurseschedulingserver.dto.shift.ShiftDto;
import com.example.nurseschedulingserver.entity.constraint.Constraint;
import com.example.nurseschedulingserver.entity.department.Department;
import com.example.nurseschedulingserver.entity.nurse.Nurse;
import com.example.nurseschedulingserver.entity.shift.Shift;
import com.example.nurseschedulingserver.entity.workday.WorkDay;
import com.example.nurseschedulingserver.enums.Role;
import com.example.nurseschedulingserver.repository.ConstraintRepository;
import com.example.nurseschedulingserver.service.interfaces.*;
import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CPServiceImpl implements CPService {
    private final DepartmentService departmentService;
    private final NurseService nurseService;
    private final WorkDayService workDayService;
    private final ConstraintRepository constraintRepository;
    private final ShiftService shiftService;

    @Scheduled(cron = "20 24 17 23 * ?", zone = "Europe/Istanbul")
    public void executeConstraint(){
        List<Department> departments = departmentService.getAllDepartments();
        for (Department department : departments) {
            Optional<Constraint> constraint = constraintRepository.findByDepartmentId(department.getId());
            List<Nurse> nurses = nurseService.getNursesByDepartment(department.getId());
            Optional<Nurse> chargeNurse = nurses.stream()
                    .filter(nurse -> nurse.getRole().equals(Role.CHARGE))
                    .findFirst();
            for (int i = 0; i <3 ; i++) {
                int finalI = i;
                constraint.ifPresent(value -> createShifts(nurses, value, finalI));
                createShiftForChargeNurse(chargeNurse.get(),finalI);
            }
        }
    }
    private void createShiftForChargeNurse(Nurse nurse,int x){
        LocalDate currentDate = LocalDate.now().plusMonths(x);
        LocalDate nextDate = currentDate.plusMonths(1);
        int nextMonthDays = nextDate.lengthOfMonth();
        List<Shift> shifts = new ArrayList<>();
        for (int i = 0; i < nextMonthDays; i++) {
         Date date = convertDate(nextDate, i);
         LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
         if(!isWeekend(localDate)){
             Shift shift = new Shift();
             shift.setNurseId(nurse.getId());
             Calendar calendar = Calendar.getInstance();
             LocalDate innerDate = LocalDate.now();
             calendar.set(innerDate.getYear(), innerDate.getMonthValue()+x, i + 1, 8, 0, 0);
             shift.setStartDate(calendar.getTime());
             calendar.set(innerDate.getYear(), innerDate.getMonthValue()+x, i + 1, 16, 0, 0);
             shift.setEndDate(calendar.getTime());
             shifts.add(shift);
         }
        }
        shiftService.saveAll(shifts);

    }

    private void createShifts(List<Nurse> nurseList, Constraint constraint,int x) {
        nurseList.removeIf(nurse -> nurse.getRole().equals(Role.CHARGE));
        long startTime = System.currentTimeMillis();
        Loader.loadNativeLibraries();
        LocalDate currentDate = LocalDate.now().plusMonths(x);
        int lastDay = currentDate.lengthOfMonth();
        int secondLastDay = currentDate.lengthOfMonth()-1;
        List<ShiftDto> lastDayShifts = shiftService.getShiftsByDepartmentAndDate(constraint.getDepartmentId(),currentDate.getMonthValue(),currentDate.getYear(),lastDay);
        List<ShiftDto> secondLastDayShifts = shiftService.getShiftsByDepartmentAndDate(constraint.getDepartmentId(),currentDate.getMonthValue(),currentDate.getYear(),secondLastDay);
        LocalDate nextDate = currentDate.plusMonths(1);
        int nextMonthDays = nextDate.lengthOfMonth();
        final int numNurses = nurseList.size();
        final int[] allNurses = IntStream.range(0, numNurses).toArray();
        final int[] allDays = IntStream.range(0, nextMonthDays).toArray();
        final int[] allShifts = IntStream.range(0, 3).toArray();
        final int solutionLimit = 1;
        List<WorkDay> workDays = getWorkDayByMonthAndDateAndDepartment(nextDate,constraint.getDepartmentId());
        HashMap<String, List<Nurse>> workDaysForNurses = new HashMap<>();
        for (int i = 0; i < nextMonthDays; i++) {
            Date date = convertDate(nextDate, i);
            String dateString = date.toString();
            workDaysForNurses.put(dateString, new ArrayList<>());
            for (Nurse nurse : nurseList) {
                boolean exists = checkWorkDateExists(date, nurse.getId(),workDays);
                if (exists) {
                    workDaysForNurses.get(dateString).add(nurse);
                }
            }
        }

        CpModel model = new CpModel();
        Literal[][][] shifts = createShiftVariables(nurseList,allNurses, allDays, allShifts, nextDate,model,workDays,workDaysForNurses,constraint,lastDayShifts,secondLastDayShifts);
        implementConsecutiveShifts(nurseList,allNurses,allDays,shifts,model,nextDate,constraint,workDays,workDaysForNurses,lastDayShifts,secondLastDayShifts);
        implementMinimumWorkingHours(allNurses,allDays,allShifts,shifts,nurseList,nextDate,model,workDays,workDaysForNurses,constraint,lastDayShifts,secondLastDayShifts);
        implementNotWorkingShifts(nurseList,allNurses,allDays,allShifts,shifts,model,nextDate,workDays,workDaysForNurses,constraint,lastDayShifts,secondLastDayShifts);

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
                    System.out.printf("Day %d:%n", d + 1);
                    for (int s : allShifts) {
                        System.out.printf("  Shift %d:%n", s);
                        for (int n : allNurses) {
                            Nurse nurse = nurseList.get(n);
                            if (shifts[n][d][s] != null && booleanValue(shifts[n][d][s])) {
                                int shiftDuration = shiftDuration(s);
                                int startHour = calculateStartHour(s);
                                int endHour = (startHour + shiftDuration) % 24;
                                System.out.printf("    Nurse %d (%s): %02d:00 - %02d:00%n", n, nurse.getFirstName(), startHour, endHour);
                                Shift shift = new Shift();
                                shift.setNurseId(nurse.getId());
                                Calendar calendar = Calendar.getInstance();
                                LocalDate date = LocalDate.now();
                                calendar.set(date.getYear(), date.getMonthValue()+x, d + 1, startHour, 0, 0);
                                shift.setStartDate(calendar.getTime());
                                if (shiftDuration == 8) {
                                    calendar.set(date.getYear(), date.getMonthValue()+x, d + 1, endHour, 0, 0);
                                } else {
                                    calendar.set(date.getYear(), date.getMonthValue()+x, d + 2, endHour, 0, 0);
                                }
                                shift.setEndDate(calendar.getTime());
                                shiftList.add(shift);
                            }
                        }
                    }
                }
                solutionCount++;
                if (solutionCount >= solutionLimit) {
                    System.out.printf("Stop search after %d solutions%n", solutionLimit);
                    stopSearch();
                }
                shiftService.saveAll(shiftList);
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

        VarArraySolutionPrinterWithLimit cb = new VarArraySolutionPrinterWithLimit(nurseList, allNurses, allDays, allShifts, shifts, solutionLimit);

        System.out.println("Solving model");
        CpSolverStatus status = solver.solve(model, cb);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
        System.out.println("Status: " + status);
        System.out.println(cb.getSolutionCount() + " solutions found.");

        System.out.println("Statistics");
        System.out.printf("  conflicts: %d%n", solver.numConflicts());
        System.out.printf("  branches : %d%n", solver.numBranches());
        System.out.printf("  wall time: %f s%n", solver.wallTime());
    }

    private int shiftDuration(int shiftIndex) {
        int[] durations = {8,16,24};
        return durations[shiftIndex];
    }
    private int calculateStartHour(int shiftIndex) {
        int[] startHours = {8, 16, 8};
        return startHours[shiftIndex];
    }

    private boolean isWeekend(LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;
    }
    private List<WorkDay> getWorkDayByMonthAndDateAndDepartment(LocalDate date,String departmentId) {
        return workDayService.findWorkDayByMonthAndYear(date.getMonthValue(),date.getYear(),departmentId);
    }
    private boolean checkWorkDateExists(Date date, String nurseId,List<WorkDay> workDays){
        return workDays.stream().anyMatch(workDay -> workDay.getWorkDate().contains(date) && workDay.getNurseId().equals(nurseId));
    }
    private Date convertDate(LocalDate date, int day) {
        return Date.from(date.withDayOfMonth(day + 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    private boolean checkWorkDayByListSize(List<Nurse> nurses,Date date,Constraint constraint){
        if(nurses != null){
            if(isWeekend(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())){
                return nurses.size() < constraint.getMinimumNursesForEachShift().get(2);
            }
            else{
                return nurses.size() < constraint.getMinimumNursesForEachShift().get(0)+constraint.getMinimumNursesForEachShift().get(1);
            }
        }
        return true;
    }
    private int getMinimumWorkingHours(int year, Month month){
        int count = 0;
        LocalDate startDate = LocalDate.of(year,month,1);
        LocalDate endDate = startDate.plusMonths(1);
        while(startDate.isBefore(endDate)){
            if(!isWeekend(startDate)){
                count+=8;
            }
            startDate = startDate.plusDays(1);
        }
        return count;
    }
    private boolean checkPreviousMonthLastDay(List<ShiftDto> shiftDtos, Nurse nurse) {
        return shiftDtos.stream().anyMatch(shiftDto ->
                shiftDto.getNurseId().equals(nurse.getId()) && getShiftDurationHours(shiftDto) >= 16);
    }

    private boolean checkPreviousMonthSecondLastDay(List<ShiftDto> shiftDtos, Nurse nurse) {
        return shiftDtos.stream().anyMatch(shiftDto ->
                shiftDto.getNurseId().equals(nurse.getId()) && getShiftDurationHours(shiftDto) == 24);
    }

    private int getShiftDurationHours(ShiftDto shiftDto) {
        long durationMillis = shiftDto.getEndDate().getTime() - shiftDto.getStartDate().getTime();
        return (int) (durationMillis / (60 * 60 * 1000));
    }


    private Literal[][][] createShiftVariables(List<Nurse> nurseList,int[] allNurses, int[] allDays, int[] allShifts,
                                               LocalDate shiftDate,CpModel model,List<WorkDay> workDays,
                                               HashMap<String, List<Nurse>> existsWorKDaysForEachNurse,
                                               Constraint constraint,List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts) {
        Literal[][][] shifts = new Literal[allNurses.length][allDays.length][allShifts.length];
        for (int n : allNurses) {
            Nurse nurse = nurseList.get(n);
            WorkDay workDay = workDays.stream().filter(w -> Objects.equals(w.getNurseId(), nurse.getId())).findFirst().orElse(null);
            for (int d : allDays) {
                boolean previousMonthLastTwoDayShift = checkPreviousShifts(lastDayShifts, secondLastDayShifts, nurse, d);
                Date date = convertDate(shiftDate, d);
                List<Nurse> nurses = existsWorKDaysForEachNurse.get(date.toString());
                boolean checkWorkDay = checkWorkDayByListSize(nurses,date,constraint);
                for (int s : allShifts) {
                    if ((workDay == null || workDay.getWorkDate().contains(date) || checkWorkDay ) && !previousMonthLastTwoDayShift) {
                        shifts[n][d][s] = model.newBoolVar("shifts_n" + n + "d" + d + "s" + s);
                    }
                }
            }
        }
        return shifts;
    }

    private boolean checkPreviousShifts(List<ShiftDto> lastDayShifts, List<ShiftDto> secondLastDayShifts, Nurse nurse, int d) {
        boolean previousMonthLastTwoDayShift = false;
        if(d==0){
            previousMonthLastTwoDayShift = checkPreviousMonthSecondLastDay(secondLastDayShifts,nurse);
            if(!previousMonthLastTwoDayShift){
                previousMonthLastTwoDayShift = checkPreviousMonthLastDay(lastDayShifts,nurse);
            }
        }
        else if(d==1){
            previousMonthLastTwoDayShift = checkPreviousMonthSecondLastDay(lastDayShifts,nurse);
        }
        return previousMonthLastTwoDayShift;
    }

    private void implementMinimumWorkingHours(int[] allNurses, int[] allDays, int[] allShifts,
                                              Literal[][][] shifts, List<Nurse> nurseList,
                                              LocalDate shiftDate, CpModel model, List<WorkDay> workDays,
                                              HashMap<String, List<Nurse>> existsWorKDaysForEachNurse,
                                              Constraint constraint,List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts ) {
        int minimumWorkingHours = getMinimumWorkingHours(shiftDate.getYear(),shiftDate.getMonth());
        for (int n : allNurses) {
            LinearExprBuilder totalHoursWorked = LinearExpr.newBuilder();
            Nurse nurse = nurseList.get(n);
            WorkDay workDay = workDays.stream().filter(w -> Objects.equals(w.getNurseId(), nurse.getId())).findFirst().orElse(null);
            for (int d : allDays) {
                Date date = convertDate(shiftDate, d);
                List<Nurse> nurses = existsWorKDaysForEachNurse.get(date.toString());
                boolean previousMonthLastTwoDayShift = checkPreviousShifts(lastDayShifts, secondLastDayShifts, nurse, d);
                boolean checkWorkDay = checkWorkDayByListSize(nurses,date,constraint);
                for (int s : allShifts) {
                    if ((workDay == null || workDay.getWorkDate().contains(date) || checkWorkDay ) && !previousMonthLastTwoDayShift) {
                        totalHoursWorked.addTerm(shifts[n][d][s], shiftDuration(s));
                    }
                }
            }
            model.addGreaterOrEqual(totalHoursWorked.build(), minimumWorkingHours);
        }
    }

    private void implementConsecutiveShifts(List<Nurse> nurseList, int[] allNurses, int[] allDays,
                                            Literal[][][] shifts, CpModel model, LocalDate shiftDate, Constraint constraint,
                                            List<WorkDay> workDays, HashMap<String, List<Nurse>> existsWorKDaysForEachNurse,
                                            List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts) {
        List<Integer> minimumNursesNeeded = constraint.getMinimumNursesForEachShift();
        minimumNursesNeeded.set(0,minimumNursesNeeded.get(0)-1);
        int totalShifts = 3;

        for (int d : allDays) {
            LocalDate date = shiftDate.withDayOfMonth(d + 1);
            boolean isWeekend = isWeekend(date);
            List<LinearExprBuilder> totalNursesInShifts = new ArrayList<>(totalShifts);

            for (int s = 0; s < totalShifts; s++) {
                totalNursesInShifts.add(LinearExpr.newBuilder());
            }

            for (int n : allNurses) {
                Nurse nurse = nurseList.get(n);
                WorkDay workDay = workDays.stream().filter(w -> Objects.equals(w.getNurseId(), nurse.getId())).findFirst().orElse(null);
                Date workDate = convertDate(shiftDate, d);
                List<Nurse> nurses = existsWorKDaysForEachNurse.get(workDate.toString());
                boolean previousMonthLastTwoDayShift = checkPreviousShifts(lastDayShifts, secondLastDayShifts, nurse, d);
                boolean checkWorkDay = checkWorkDayByListSize(nurses,workDate,constraint);
                if ((workDay == null || (workDay.getWorkDate().contains(workDate)) || checkWorkDay) && !previousMonthLastTwoDayShift) {
                    if (!isWeekend) {
                        for (int s = 0; s < totalShifts - 1; s++) {
                            totalNursesInShifts.get(s).addTerm(shifts[n][d][s], 1);
                        }
                        model.addEquality(shifts[n][d][totalShifts - 1], 0);
                    } else {
                        totalNursesInShifts.get(totalShifts - 1).addTerm(shifts[n][d][totalShifts - 1], 1);
                        for (int s = 0; s < totalShifts - 1; s++) {
                            model.addEquality(shifts[n][d][s], 0);
                        }
                    }
                }
            }

            if (!isWeekend) {
                for (int s = 0; s < totalShifts - 1; s++) {
                    if(s == 0) {
                        model.addGreaterOrEqual(totalNursesInShifts.get(s).build(), minimumNursesNeeded.get(s));
                    }else {
                        model.addEquality(totalNursesInShifts.get(s).build(), minimumNursesNeeded.get(s));
                    }
                }
            } else {
                model.addEquality(totalNursesInShifts.get(totalShifts - 1).build(), minimumNursesNeeded.get(totalShifts - 1));
            }
        }
    }

    private void implementNotWorkingShifts(List<Nurse> nurseList, int[] allNurses, int[] allDays, int[] allShifts,
                                           Literal[][][] shifts, CpModel model, LocalDate shiftDate, List<WorkDay> workDays,
                                           HashMap<String, List<Nurse>> existsWorKDaysForEachNurse,Constraint constraint,
                                           List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts) {
        for (int n : allNurses) {
            Nurse nurse = nurseList.get(n);
            WorkDay workDay = workDays.stream().filter(w -> Objects.equals(w.getNurseId(), nurse.getId())).findFirst().orElse(null);
            for (int d = 0; d < allDays.length; d++) {
                Date date = convertDate(shiftDate, d);
                List<Nurse> nurses = existsWorKDaysForEachNurse.get(date.toString());
                boolean previousMonthLastTwoDayShift = checkPreviousShifts(lastDayShifts, secondLastDayShifts, nurse, d);
                boolean checkWorkDay = checkWorkDayByListSize(nurses,date,constraint);
                if ((workDay == null || workDay.getWorkDate().contains(date) ||checkWorkDay) && !previousMonthLastTwoDayShift) {
                    for (int s = 0; s < allShifts.length; s++) {
                        if (shifts[n][d][s] != null) {
                            for (int otherS = s + 1; otherS < allShifts.length; otherS++) {
                                if (shifts[n][d][otherS] != null) {
                                    model.addImplication(shifts[n][d][s], shifts[n][d][otherS].not());
                                    model.addImplication(shifts[n][d][otherS], shifts[n][d][s].not());
                                }
                            }

                            if (s == 1 && d < allDays.length - 1) {
                                for (int nextS = 0; nextS < allShifts.length; nextS++) {
                                    if (shifts[n][d + 1][nextS] != null) {
                                        model.addImplication(shifts[n][d][1], shifts[n][d + 1][nextS].not());
                                    }
                                }
                            }
                            if (s == 2) {
                                for (int nextD = 1; nextD <= 2; nextD++) {
                                    if (d + nextD < allDays.length) {
                                        for (int nextS = 0; nextS < allShifts.length; nextS++) {
                                            if (shifts[n][d + nextD][nextS] != null) {
                                                model.addImplication(shifts[n][d][2], shifts[n][d + nextD][nextS].not());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
