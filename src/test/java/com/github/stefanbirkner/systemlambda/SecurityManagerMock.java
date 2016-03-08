package com.github.stefanbirkner.systemlambda;

import java.security.Permission;

class SecurityManagerMock extends SecurityManager {
	@Override
	public void checkPermission(Permission perm) {
		// everything is allowed
	}
}
