package com.visma.of.cps.solver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visma.of.api.model.Request;
import com.visma.of.api.model.SolverStatus;
import com.visma.of.cps.SuperSolver;
import com.visma.of.cps.model.Model;
import com.visma.of.cps.solution.Problem;
import com.visma.of.solverapi.Solver;
import com.visma.of.solverapi.SolverListener;
import com.visma.of.solverapi.SolverProvider;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class CpsSolver extends Solver {

    private Model model;
    private SuperSolver superSolver;
    private boolean hasLikelyConverged = false;

    static {
        SolverProvider.registerSolver(CpsSolver.class);
    }

    @Override
    public void initializeSolver() throws Exception {
        JSONObject jsonObject = getJsonPayload();
        Request dataProvider = Solver.readFromJsonObjectMapper(Request.class, jsonObject.toJSONString());
        // model = ModelFactory.generateModelFromDataProvider(dataProvider);
        model = new Model(4);
        SuperSolver superSolver = new SuperSolver();
        superSolver.intialize(model);
    }

    @Override
    public void solve() throws Exception {
        superSolver.solve();
        Problem solution = superSolver.getCurrentBestSolution();
        JSONObject jsonSolution = Solver.objectToJsonObject(solution);
        hasLikelyConverged = true;


        for (SolverListener listener : getListeners()) {
            listener.newBestSolutionFound(jsonSolution);
        }
    }

    @Override
    public Map<String, Double> getPayloadStatisticsAsNumbers() {
        Map<String, Double> payloadStats = new HashMap<>();
        return payloadStats;
    }

    @Override
    public JSONObject getSolverStatus(JSONParser parser, ObjectMapper objectMapper) throws JsonProcessingException, ParseException {
        SolverStatus solverStatus = new SolverStatus().hasLikelyConverged(hasLikelyConverged);
        return Solver.objectToJsonObject(solverStatus, parser, objectMapper);
    }

    @Override
    public Map<String, Boolean> getSolverFeatureFlagDefaultValues() {
        Map<String, Boolean> featureFlags = new HashMap<>();
        return featureFlags;
    }
}
