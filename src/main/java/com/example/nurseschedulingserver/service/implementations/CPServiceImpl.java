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
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
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
                LocalDate targetMonth = LocalDate.now().plusMonths(i + 1).withDayOfMonth(1);
                constraint.ifPresent(value -> createShifts(nurses, value, targetMonth, chargeNurse.isPresent()));
                chargeNurse.ifPresent(nurse -> createShiftForChargeNurse(nurse, targetMonth));
            }
        }
    }
    private void createShiftForChargeNurse(Nurse nurse,LocalDate targetMonth){
        int nextMonthDays = targetMonth.lengthOfMonth();
        List<Shift> shifts = new ArrayList<>();
        for (int i = 0; i < nextMonthDays; i++) {
            Date date = convertDate(targetMonth, i);
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if(!isWeekend(localDate)){
                Shift shift = new Shift();
                shift.setNurseId(nurse.getId());
                Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(targetMonth.getYear(), targetMonth.getMonthValue()-1, i + 1, 8, 0, 0);
                shift.setStartDate(calendar.getTime());
                calendar.set(targetMonth.getYear(), targetMonth.getMonthValue()-1, i + 1, 16, 0, 0);
                shift.setEndDate(calendar.getTime());
                shifts.add(shift);
            }
        }
        shiftService.saveAll(shifts);

    }

    private void createShifts(List<Nurse> nurseList, Constraint constraint,LocalDate targetMonth, boolean hasChargeNurse) {
        List<Nurse> schedulableNurses = nurseList.stream()
                .filter(nurse -> !nurse.getRole().equals(Role.CHARGE))
                .collect(Collectors.toList());
        long startTime = System.currentTimeMillis();
        Loader.loadNativeLibraries();
        LocalDate previousMonth = targetMonth.minusMonths(1);
        int lastDay = previousMonth.lengthOfMonth();
        int secondLastDay = Math.max(1, previousMonth.lengthOfMonth()-1);
        List<ShiftDto> lastDayShifts = shiftService.getShiftsByDepartmentAndDate(constraint.getDepartmentId(),previousMonth.getMonthValue(),previousMonth.getYear(),lastDay);
        List<ShiftDto> secondLastDayShifts = shiftService.getShiftsByDepartmentAndDate(constraint.getDepartmentId(),previousMonth.getMonthValue(),previousMonth.getYear(),secondLastDay);
        int nextMonthDays = targetMonth.lengthOfMonth();
        final int numNurses = schedulableNurses.size();
        if (numNurses == 0) {
            return;
        }
        final int[] allNurses = IntStream.range(0, numNurses).toArray();
        final int[] allDays = IntStream.range(0, nextMonthDays).toArray();
        final int[] allShifts = IntStream.range(0, 3).toArray();

        shiftService.deleteShiftsByDepartmentAndMonth(constraint.getDepartmentId(), targetMonth.getMonthValue(), targetMonth.getYear());

        int minDayShift = constraint.getMinimumNursesForEachShift().get(0);
        int minNightShift = constraint.getMinimumNursesForEachShift().get(1);
        int minFullShift = constraint.getMinimumNursesForEachShift().get(2);

        List<WorkDay> workDays = getWorkDayByMonthAndDateAndDepartment(targetMonth,constraint.getDepartmentId());
        HashMap<String, List<Nurse>> workDaysForNurses = new HashMap<>();
        for (int i = 0; i < nextMonthDays; i++) {
            Date date = convertDate(targetMonth, i);
            String dateString = date.toString();
            workDaysForNurses.put(dateString, new ArrayList<>());
            for (Nurse nurse : schedulableNurses) {
                boolean exists = checkWorkDateExists(date, nurse.getId(),workDays);
                if (exists) {
                    workDaysForNurses.get(dateString).add(nurse);
                }
            }
        }

        CpModel model = new CpModel();
        Literal[][][] shifts = createShiftVariables(schedulableNurses,allNurses, allDays, allShifts, targetMonth,model,workDays,workDaysForNurses,minDayShift,minNightShift,minFullShift,hasChargeNurse,lastDayShifts,secondLastDayShifts);
        applyCoverageConstraints(schedulableNurses,allNurses,allDays,shifts,model,targetMonth,minDayShift,minNightShift,minFullShift,hasChargeNurse,workDays,workDaysForNurses,lastDayShifts,secondLastDayShifts);
        applyRestAndExclusivityConstraints(schedulableNurses,allNurses,allDays,allShifts,shifts,model,targetMonth,workDays,workDaysForNurses,minDayShift,minNightShift,minFullShift,hasChargeNurse,lastDayShifts,secondLastDayShifts);
        List<IntVar> deviations = buildFairnessObjective(schedulableNurses, allNurses, allDays, allShifts, shifts, model, targetMonth, workDays, workDaysForNurses, minDayShift, minNightShift, minFullShift, hasChargeNurse, lastDayShifts, secondLastDayShifts);
        addObjective(model, deviations, targetMonth.lengthOfMonth());

        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(30.0);

        System.out.println("Solving model for " + targetMonth);
        CpSolverStatus status = solver.solve(model);
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
        System.out.println("Status: " + status);
        if (status != CpSolverStatus.OPTIMAL && status != CpSolverStatus.FEASIBLE) {
            return;
        }
        List<Shift> shiftList = new ArrayList<>();
        for (int d : allDays) {
            for (int s : allShifts) {
                for (int n : allNurses) {
                    if (shifts[n][d][s] != null && solver.booleanValue(shifts[n][d][s])) {
                        Nurse nurse = schedulableNurses.get(n);
                        int shiftDuration = shiftDuration(s);
                        int startHour = calculateStartHour(s);
                        int endHour = (startHour + shiftDuration) % 24;
                        Shift shift = new Shift();
                        shift.setNurseId(nurse.getId());
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.set(targetMonth.getYear(), targetMonth.getMonthValue()-1, d + 1, startHour, 0, 0);
                        shift.setStartDate(calendar.getTime());
                        if (shiftDuration == 8) {
                            calendar.set(targetMonth.getYear(), targetMonth.getMonthValue()-1, d + 1, endHour, 0, 0);
                        } else {
                            calendar.set(targetMonth.getYear(), targetMonth.getMonthValue()-1, d + 2, endHour, 0, 0);
                        }
                        shift.setEndDate(calendar.getTime());
                        shiftList.add(shift);
                    }
                }
            }
        }
        shiftService.saveAll(shiftList);

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

    private boolean checkWorkDayByListSize(List<Nurse> nurses,Date date,int dayShiftNeed,int nightShiftNeed,int fullShiftNeed,boolean hasChargeNurse){
        if(nurses != null){
            if(isWeekend(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())){
                return nurses.size() < fullShiftNeed;
            }
            else{
                int effectiveDayNeed = hasChargeNurse ? Math.max(0, dayShiftNeed - 1) : dayShiftNeed;
                return nurses.size() < effectiveDayNeed+nightShiftNeed;
            }
        }
        return true;
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
                                               int dayShiftNeed,int nightShiftNeed,int fullShiftNeed,boolean hasChargeNurse,
                                               List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts) {
        Literal[][][] shifts = new Literal[allNurses.length][allDays.length][allShifts.length];
        for (int n : allNurses) {
            Nurse nurse = nurseList.get(n);
            WorkDay workDay = workDays.stream().filter(w -> Objects.equals(w.getNurseId(), nurse.getId())).findFirst().orElse(null);
            for (int d : allDays) {
                boolean previousMonthLastTwoDayShift = checkPreviousShifts(lastDayShifts, secondLastDayShifts, nurse, d);
                Date date = convertDate(shiftDate, d);
                List<Nurse> nurses = existsWorKDaysForEachNurse.get(date.toString());
                boolean checkWorkDay = checkWorkDayByListSize(nurses,date,dayShiftNeed,nightShiftNeed,fullShiftNeed,hasChargeNurse);
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

    private void applyCoverageConstraints(List<Nurse> nurseList, int[] allNurses, int[] allDays,
                                            Literal[][][] shifts, CpModel model, LocalDate shiftDate,
                                            int dayShiftNeed,int nightShiftNeed,int fullShiftNeed,boolean hasChargeNurse,
                                            List<WorkDay> workDays, HashMap<String, List<Nurse>> existsWorKDaysForEachNurse,
                                            List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts) {
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
                boolean checkWorkDay = checkWorkDayByListSize(nurses,workDate,dayShiftNeed,nightShiftNeed,fullShiftNeed,hasChargeNurse);
                if ((workDay == null || (workDay.getWorkDate().contains(workDate)) || checkWorkDay) && !previousMonthLastTwoDayShift) {
                    if (!isWeekend) {
                        for (int s = 0; s < totalShifts - 1; s++) {
                            if (shifts[n][d][s] != null) {
                                totalNursesInShifts.get(s).addTerm(shifts[n][d][s], 1);
                            }
                        }
                        if (shifts[n][d][totalShifts - 1] != null) {
                            model.addEquality(shifts[n][d][totalShifts - 1], 0);
                        }
                    } else {
                        if (shifts[n][d][totalShifts - 1] != null) {
                            totalNursesInShifts.get(totalShifts - 1).addTerm(shifts[n][d][totalShifts - 1], 1);
                        }
                        for (int s = 0; s < totalShifts - 1; s++) {
                            if (shifts[n][d][s] != null) {
                                model.addEquality(shifts[n][d][s], 0);
                            }
                        }
                    }
                }
            }

            if (!isWeekend) {
                int effectiveDayNeed = hasChargeNurse ? Math.max(0, dayShiftNeed - 1) : dayShiftNeed;
                for (int s = 0; s < totalShifts - 1; s++) {
                    if(s == 0) {
                        model.addEquality(totalNursesInShifts.get(s).build(), effectiveDayNeed);
                    }else {
                        model.addEquality(totalNursesInShifts.get(s).build(), nightShiftNeed);
                    }
                }
            } else {
                model.addEquality(totalNursesInShifts.get(totalShifts - 1).build(), fullShiftNeed);
            }
        }
    }

    private void applyRestAndExclusivityConstraints(List<Nurse> nurseList, int[] allNurses, int[] allDays, int[] allShifts,
                                           Literal[][][] shifts, CpModel model, LocalDate shiftDate, List<WorkDay> workDays,
                                           HashMap<String, List<Nurse>> existsWorKDaysForEachNurse,int dayShiftNeed,int nightShiftNeed,int fullShiftNeed,boolean hasChargeNurse,
                                           List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts) {
        for (int n : allNurses) {
            Nurse nurse = nurseList.get(n);
            WorkDay workDay = workDays.stream().filter(w -> Objects.equals(w.getNurseId(), nurse.getId())).findFirst().orElse(null);
            for (int d = 0; d < allDays.length; d++) {
                Date date = convertDate(shiftDate, d);
                List<Nurse> nurses = existsWorKDaysForEachNurse.get(date.toString());
                boolean previousMonthLastTwoDayShift = checkPreviousShifts(lastDayShifts, secondLastDayShifts, nurse, d);
                boolean checkWorkDay = checkWorkDayByListSize(nurses,date,dayShiftNeed,nightShiftNeed,fullShiftNeed,hasChargeNurse);
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

    private List<IntVar> buildFairnessObjective(List<Nurse> nurseList, int[] allNurses, int[] allDays, int[] allShifts,
                                                Literal[][][] shifts, CpModel model, LocalDate shiftDate, List<WorkDay> workDays,
                                                HashMap<String, List<Nurse>> existsWorKDaysForEachNurse,int dayShiftNeed,int nightShiftNeed,int fullShiftNeed,boolean hasChargeNurse,
                                                List<ShiftDto> lastDayShifts,List<ShiftDto> secondLastDayShifts){
        List<IntVar> deviations = new ArrayList<>();
        int maxHours = shiftDate.lengthOfMonth() * 24;
        int targetHours = computeTargetHours(shiftDate, dayShiftNeed, nightShiftNeed, fullShiftNeed, hasChargeNurse, nurseList.size());
        for (int n : allNurses) {
            LinearExprBuilder totalHoursWorked = LinearExpr.newBuilder();
            Nurse nurse = nurseList.get(n);
            WorkDay workDay = workDays.stream().filter(w -> Objects.equals(w.getNurseId(), nurse.getId())).findFirst().orElse(null);
            for (int d : allDays) {
                Date date = convertDate(shiftDate, d);
                List<Nurse> nurses = existsWorKDaysForEachNurse.get(date.toString());
                boolean previousMonthLastTwoDayShift = checkPreviousShifts(lastDayShifts, secondLastDayShifts, nurse, d);
                boolean checkWorkDay = checkWorkDayByListSize(nurses,date,dayShiftNeed,nightShiftNeed,fullShiftNeed,hasChargeNurse);
                for (int s : allShifts) {
                    if ((workDay == null || workDay.getWorkDate().contains(date) || checkWorkDay ) && !previousMonthLastTwoDayShift && shifts[n][d][s] != null) {
                        totalHoursWorked.addTerm(shifts[n][d][s], shiftDuration(s));
                    }
                }
            }
            IntVar totalHoursVar = model.newIntVar(0, maxHours, "totalHours_n" + n);
            model.addEquality(totalHoursVar, totalHoursWorked.build());
            IntVar deviation = model.newIntVar(0, maxHours, "deviation_n" + n);
            LinearExprBuilder deviationExpr = LinearExpr.newBuilder();
            deviationExpr.add(totalHoursVar);
            deviationExpr.add(-targetHours);
            model.addAbsEquality(deviation, deviationExpr);
            deviations.add(deviation);
        }
        return deviations;
    }

    private void addObjective(CpModel model, List<IntVar> deviations, int daysInMonth){
        if (deviations.isEmpty()) {
            return;
        }
        int maxHours = daysInMonth * 24;
        IntVar maxDeviation = model.newIntVar(0, maxHours, "maxDeviation");
        for (IntVar deviation : deviations) {
            model.addLessOrEqual(deviation, maxDeviation);
        }
        LinearExprBuilder objective = LinearExpr.newBuilder();
        deviations.forEach(objective::add);
        objective.addTerm(maxDeviation, 100);
        model.minimize(objective);
    }

    private int computeTargetHours(LocalDate targetMonth,int dayShiftNeed,int nightShiftNeed,int fullShiftNeed,boolean hasChargeNurse,int nurseCount){
        if (nurseCount == 0) {
            return 0;
        }
        LocalDate cursor = targetMonth.withDayOfMonth(1);
        LocalDate end = cursor.plusMonths(1);
        int total = 0;
        while(cursor.isBefore(end)){
            if(isWeekend(cursor)){
                total += fullShiftNeed * 24;
            } else {
                int effectiveDayNeed = hasChargeNurse ? Math.max(0, dayShiftNeed - 1) : dayShiftNeed;
                total += effectiveDayNeed * 8 + nightShiftNeed * 16;
            }
            cursor = cursor.plusDays(1);
        }
        return (int) Math.round((double) total / nurseCount);
    }
}
