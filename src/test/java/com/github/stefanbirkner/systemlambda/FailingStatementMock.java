package com.github.stefanbirkner.systemlambda;

class FailingStatementMock implements Statement {
	boolean hasBeenEvaluated = false;
	Exception exception = new Exception("failing statement mock exception");

	@Override
	public void execute() throws Exception {
		hasBeenEvaluated = true;
		throw exception;
	}
}
