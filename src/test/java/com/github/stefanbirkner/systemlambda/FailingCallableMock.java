package com.github.stefanbirkner.systemlambda;

import java.util.concurrent.Callable;

class FailingCallableMock implements Callable<String> {
	boolean hasBeenEvaluated = false;
	Exception exception = new Exception("failing callable mock exception");

	@Override
	public String call() throws Exception {
		hasBeenEvaluated = true;
		throw exception;
	}
}
