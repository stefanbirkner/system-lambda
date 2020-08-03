package com.github.stefanbirkner.systemlambda;

import java.util.concurrent.Callable;

class CallableMock implements Callable<String> {
	boolean hasBeenEvaluated = false;

	@Override
	public String call() throws Exception {
		hasBeenEvaluated = true;
		return "dummy text";
	}
}
