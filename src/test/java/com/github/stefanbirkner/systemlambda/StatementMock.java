package com.github.stefanbirkner.systemlambda;

class StatementMock implements Statement {
    boolean hasBeenEvaluated = false;

    @Override
    public void execute() throws Exception {
        hasBeenEvaluated = true;
    }
}
