package com.github.stefanbirkner.systemlambda;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

class SecurityManagerMock extends SecurityManager {
	Object securityContext = new Object();
	boolean inCheck = false;
	ThreadGroup threadGroup;
	boolean topLevelWindow = false;
	Object windowOfCheckTopLevelWindowCall;
	List<Invocation> invocations = new ArrayList<>();

	@Override
	public void checkCreateClassLoader() {
		logMethodCall("checkCreateClassLoader");
		super.checkCreateClassLoader();
	}

	@Override
	public void checkAccess(Thread thread) {
		logMethodCall("checkAccess", Thread.class, thread);
		super.checkAccess(thread);
	}

	@Override
	public void checkAccess(ThreadGroup threadGroup) {
		logMethodCall("checkAccess", ThreadGroup.class, threadGroup);
		super.checkAccess(threadGroup);
	}

	@Override
	public void checkExit(int i) {
		logMethodCall("checkExit", int.class, i);
		super.checkExit(i);
	}

	@Override
	public void checkExec(String s) {
		logMethodCall("checkExec", String.class, s);
		super.checkExec(s);
	}

	@Override
	public void checkLink(String s) {
		logMethodCall("checkLink", String.class, s);
		super.checkLink(s);
	}

	@Override
	public void checkRead(FileDescriptor fileDescriptor) {
		logMethodCall("checkRead", FileDescriptor.class, fileDescriptor);
		super.checkRead(fileDescriptor);
	}

	@Override
	public void checkRead(String s) {
		logMethodCall("checkRead", String.class, s);
		super.checkRead(s);
	}

	@Override
	public void checkRead(String s, Object o) {
		logMethodCall(
			"checkRead",
			new Class[] { String.class, Object.class },
			s, o);
		super.checkRead(s, o);
	}

	@Override
	public void checkWrite(FileDescriptor fileDescriptor) {
		logMethodCall("checkWrite", FileDescriptor.class, fileDescriptor);
		super.checkWrite(fileDescriptor);
	}

	@Override
	public void checkWrite(String s) {
		logMethodCall("checkWrite", String.class, s);
		super.checkWrite(s);
	}

	@Override
	public void checkDelete(String s) {
		logMethodCall("checkDelete", String.class, s);
		super.checkDelete(s);
	}

	@Override
	public void checkConnect(String s, int i) {
		logMethodCall(
			"checkConnect",
			new Class[] { String.class, int.class },
			s, i);
		super.checkConnect(s, i);
	}

	@Override
	public void checkConnect(String s, int i, Object o) {
		logMethodCall(
			"checkConnect",
			new Class[] { String.class, int.class, Object.class },
			s, i, o);
		super.checkConnect(s, i, o);
	}

	@Override
	public void checkListen(int i) {
		logMethodCall("checkListen", int.class, i);
		super.checkListen(i);
	}

	@Override
	public void checkAccept(String s, int i) {
		logMethodCall(
			"checkAccept",
			new Class[] { String.class, int.class },
			s, i);
		super.checkAccept(s, i);
	}

	@Override
	public void checkMulticast(InetAddress inetAddress) {
		logMethodCall("checkMulticast", InetAddress.class, inetAddress);
		super.checkMulticast(inetAddress);
	}

	@Override
	public void checkMulticast(InetAddress inetAddress, byte b) {
		logMethodCall(
			"checkMulticast",
			new Class[] { InetAddress.class, byte.class },
			inetAddress, b);
		super.checkMulticast(inetAddress, b);
	}

	@Override
	public void checkPropertiesAccess() {
		logMethodCall("checkPropertiesAccess");
		super.checkPropertiesAccess();
	}

	@Override
	public void checkPropertyAccess(String s) {
		logMethodCall("checkPropertyAccess", String.class, s);
		super.checkPropertyAccess(s);
	}

	@Override
	public void checkPrintJobAccess() {
		logMethodCall("checkPrintJobAccess");
		super.checkPrintJobAccess();
	}

	@Override
	public void checkSystemClipboardAccess() {
		logMethodCall("checkSystemClipboardAccess");
		super.checkSystemClipboardAccess();
	}

	@Override
	public void checkAwtEventQueueAccess() {
		logMethodCall("checkAwtEventQueueAccess");
		super.checkAwtEventQueueAccess();
	}

	@Override
	public void checkPackageAccess(String s) {
		logMethodCall("checkPackageAccess", String.class, s);
		super.checkPackageAccess(s);
	}

	@Override
	public void checkPackageDefinition(String s) {
		logMethodCall("checkPackageDefinition", String.class, s);
		super.checkPackageDefinition(s);
	}

	@Override
	public void checkPermission(Permission permission) {
		logMethodCall("checkPermission", Permission.class, permission);
		// everything is allowed
	}

	@Override
	public void checkPermission(Permission permission, Object o) {
		logMethodCall(
			"checkPermission",
			new Class[] { Permission.class, Object.class },
			permission, o);
		// everything is allowed
	}

	@Override
	public void checkSetFactory() {
		logMethodCall("checkSetFactory");
		super.checkSetFactory();
	}

	@Override
	public void checkMemberAccess(Class<?> aClass, int i) {
		logMethodCall(
			"checkMemberAccess",
			new Class[] { Class.class, int.class },
			aClass, i);
		super.checkMemberAccess(aClass, i);
	}

	@Override
	public void checkSecurityAccess(String s) {
		logMethodCall("checkSecurityAccess", String.class, s);
		super.checkSecurityAccess(s);
	}

	@Override
	public Object getSecurityContext() {
		logMethodCall("getSecurityContext");
		return securityContext;
	}

	@Override
	public boolean getInCheck() {
		logMethodCall("getInCheck");
		return inCheck;
	}

	@Override
	public ThreadGroup getThreadGroup() {
		logMethodCall("getThreadGroup");
		return threadGroup;
	}

	@Override
	public boolean checkTopLevelWindow(Object window) {
		logMethodCall("checkTopLevelWindow", Object.class, window);
		windowOfCheckTopLevelWindowCall = window;
		return topLevelWindow;
	}

	void logMethodCall(String name) {
		logMethodCall(name, new Class[0]);
	}

	void logMethodCall(String name, Class parameterType, Object argument) {
		logMethodCall(name, new Class[] { parameterType }, argument);
	}

	void logMethodCall(String name, Class[] parameterTypes, Object... arguments) {
		invocations.add(
			new Invocation(
				name,
				parameterTypes,
				arguments
			)
		);
	}

	static class Invocation {
		final String methodName;
		final Class[] parameterTypes;
		final Object[] arguments;

		Invocation(
			String methodName,
			Class[] parameterTypes,
			Object[] arguments
		) {
			this.methodName = methodName;
			this.parameterTypes = parameterTypes;
			this.arguments = arguments;
		}
	}
}
