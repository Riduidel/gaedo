package com.dooapp.gaedo.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.time.StopWatch;

/**
 * A logging invocation handler allowing automatic unwrapping and a log for each test
 * @author ndx
 *
 */
public class LoggingHandler implements InvocationHandler {
	private static final Logger logger = Logger.getLogger(LoggingHandler.class.getName());
	
	private Object source;

	public LoggingHandler(Object source) {
		this.source = source;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		StopWatch watch = new StopWatch();
		watch.start();
		try {
			// Unwrap potentially wrapped objects
			Object returned = method.invoke(source, unwrap(args));
			// java.* methods aren't decorated
			if(returned!=null && method.getReturnType().isInterface() && !method.getDeclaringClass().getCanonicalName().startsWith("java")) {
				returned = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {method.getReturnType()}, new LoggingHandler(returned));
			}
			return returned;
		} finally {
			watch.stop();
			if (logger.isLoggable(Level.INFO)) {
				logger.info(new ToStringBuilder(source, ToStringStyle.MULTI_LINE_STYLE).append("method", method).append("arguments", args).append("duration", watch.toString()).toString());
			}
		}
	}

	
	private Object[] unwrap(Object[] args) {
		if(args==null)
			return args;
		Object[] returned = new Object[args.length];
		for (int index = 0; index < returned.length; index++) {
			if(args[index] instanceof Proxy) {
				InvocationHandler handler = Proxy.getInvocationHandler(args[index]);
				if(handler instanceof LoggingHandler) {
					returned[index] = ((LoggingHandler) handler).source;
				}
			} else {
				returned[index] = args[index];
			}
		}
		return returned;
	}

}
