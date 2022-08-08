package com.visma.of.cps;

public class Main {


    public static void main(String[] args) {
        SuperSolver solver = new SuperSolver();
        solver.initialize(4);
        solver.solve();
    }
}
