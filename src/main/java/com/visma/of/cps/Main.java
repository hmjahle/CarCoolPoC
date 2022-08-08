package com.visma.of.cps;

import java.util.List;
import java.util.Map;

import com.visma.of.cps.model.Shift;
import com.visma.of.cps.model.Visit;
import com.visma.of.cps.solution.Problem;
import com.visma.of.cps.util.CarPoolingTimeDependentPairsUtils;
import com.visma.of.cps.util.Constants;

public class Main {
    CarPoolingTimeDependentPairsUtils utilsCarcool;

    public static void main(String[] args) {
        SuperSolver solver = new SuperSolver();
        solver.initialize(4);
        solver.solve();
        Problem bestSolution = solver.getCurrentBestSolution();
        for (Shift shift : solver.getModel().getShifts()){
            System.out.println(String.format("Shift nr %d: Motorized %b", shift.getId(), shift.isMotorized()));
            List<Visit> route = bestSolution.getSolution().getRoute(shift);
            for (Visit visit: route ){
                String response = new Main().visitToString(visit, route, bestSolution.getSolution().getCarpoolSyncedTaskStartTimes(), shift);
                System.out.println(response);
            }
        }
        System.out.println(String.format("Objective: %d", bestSolution.getObjective().getTotalObjectiveValue()));
        System.out.println(String.format("Number of unallocated tasks %d", bestSolution.getSolution().getUnallocatedTasks().size()));
        
    }


    private String visitToString(Visit visit, List<Visit> route, Map<Visit, Integer> syncedVisitsStartTimes, Shift employeeShift){
        int startTime = new CarPoolingTimeDependentPairsUtils().getTimeWindowStart(route, visit, syncedVisitsStartTimes, employeeShift);
        String visitType ="";
        switch(visit.getVisitType()){
            case Constants.VisitType.COMPLETE_TASK:
                visitType = "CT";
                break;
            case Constants.VisitType.DROP_OF:
                visitType = "D";
                break;
            case Constants.VisitType.PICK_UP:
                visitType = "P";
                break;
            default:
                visitType = "JM";
                break;

        }
        return String.format("%s %d \nstart-end %d - %d \nCoCarpool %d \ntransport %d \ntravelTime %d",visitType, visit.getTask().getId(), startTime, startTime+visit.getVisitDuration(), visit.getCoCarPoolerShiftID(), visit.getTransportType(),  visit.getTravelTime());

    }
}
