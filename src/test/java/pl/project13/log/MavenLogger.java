package pl.project13.log;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;

import org.slf4j.impl.MavenSimpleLogger;
import org.slf4j.impl.MavenSimpleLoggerFactory;
import org.slf4j.impl.SimpleLoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Attempts to route logging to Maven Plugin / Mojo output during Maven builds.
 * Falls back to System.out if routing fails.
 */
public class MavenLogger implements ILoggerFactory, InvocationHandler {
	private static final CombinedLogger PROXY = (CombinedLogger) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { CombinedLogger.class }, INSTANCE);

	private static final Level OUTPUT_LEVEL = Level.INFO;

	public static CombinedLogger getLogger() {
		return PROXY;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.getName().equals("getName")) {
			return MavenLogger.class.getName();
		}

		// reroute level checks to mojo logger
		if(method.getName().matches("is(Trace|Debug|Info|Warn|Error)Enabled")) {
			final String levelName = method.getName().substring(2).toUpperCase();
			final int levelOrdinal = Level.valueOf(levelName).ordinal();
			return OUTPUT_LEVEL.ordinal() >= levelOrdinal;
		}

		if(method.getName().matches("trace|debug|info|warn|error")) {
			final String methodName = method.getName().replace("trace", "debug").toUpperCase();
			System.out.println("[" + methodName + "]" + formatIntercepted(method, args));
			return null;
		}

		System.out.println("Unhandled method: " + method.getName());
		return null;
	}

	public String formatIntercepted(Method method, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if(args.length == 1)
			return args[0].toString();
		args[0] = args[0].toString();
		if(args.length > 1 && args[1] instanceof Object[]) {
			return MessageFormatter.class.getMethod("arrayFormat", method.getParameterTypes()).invoke(null, args).toString();
		} else {
			return MessageFormatter.class.getMethod("format", method.getParameterTypes()).invoke(null, args).toString();
		}
	}

	@Override
	public Logger getLogger(String name) {
		return PROXY;
	}

}
