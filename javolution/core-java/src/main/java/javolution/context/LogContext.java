/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.lang.Configurable;
import javolution.lang.RealTime;
import javolution.osgi.internal.OSGiServices;

/**
 * <p> Asynchronous logging context adapting to the runtime environment.
 *     Log events are delivered asynchronously either to the OSGi 
 *     {@code org.osgi.service.log.LogService LogService} or to the standard 
 *     output stream when running outside OSGi.</p>
 *     
 * <p> Logging contexts support automatic prefixing/suffixing of any information 
 *     relevant to the user/developer (thread id, user id, and so on). 
 * [code]
 * void performTransaction(UserID userId) {
 *     LogContext ctx = LogContext.enter(); 
 *     try {
 *         ctx.prefix("[ID: ", userId, "] "); 
 *         ... 
 *             // Somewhere in an a function being called.
 *             LogContext.info("Overdraft of ", amount); // Asynchronously logs "[ID: <userId>] Overdraft of <amount>"
 *         ...
 *      } finally {
 *         ctx.exit(); // Reverts to previous log settings.
 *      }
 *  }[/code]</p>
 *  
 *  <p> Application should separate messages elements by commas and not 
 *      use {@link String} concatenations when calling log methods 
 *      (otherwise the concatenation is performed by the current thread  
 *       even when log events are filtered out).
 *  [code]
 *  LogContext ctx = LogContext.enter();
 *  try {
 *      ctx.setLevel(Level.WARNING); // Logs only error/warnings. 
 *      ... 
 *          LogContext.info("Index: ", index, " at maximum value"); // GOOD (always fast)!
 *          LogContext.info("Index: " + index + " at maximum value"); // BAD (slow down current thread)!
 *      ...
 *  } finally {
 *      ctx.exit(); // Reverts to previous settings.
 *  }[/code]</p>
 *  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see javolution.text.TextContext
 */
@RealTime(stackSafe = false, comment = "Message formatting is perormed asynchronously, stack objects cannot be logged directly")
public abstract class LogContext extends AbstractContext {

    /**
     * Defines the logging levels.
     */
    public enum Level {

        DEBUG, INFO, WARNING, ERROR, FATAL

    }

    /**
     * Holds the default logging level (<code>DEBUG</code>).
     * This level is configurable. For example, running with 
     * the option <code>-Djavolution.context.LogContext#LEVEL=WARNING</code>  
     * causes the debug/info not to be logged. 
     */
    public static final Configurable<Level> LEVEL = new Configurable<Level>(
            Level.DEBUG);

    /**
     * Logs the specified debug message. 
     */
    public static void debug(Object... message) {
        currentLogContext().log(Level.DEBUG, message);
    }

    /**
     * Enters and returns a new log context instance.
     */
    public static LogContext enter() {
        return (LogContext) currentLogContext().enterInner();
    }

    /**
     * Logs the specified error message (which may include any {@link Throwable}
     * instance).
     */
    public static void error(Object... message) {
        currentLogContext().log(Level.ERROR, message);
    }

    /**
     * Logs the specified info message. 
     */
    public static void info(Object... message) {
        currentLogContext().log(Level.INFO, message);
    }

    /**
     * Logs the specified warning message. 
     */
    public static void warning(Object... message) {
        currentLogContext().log(Level.WARNING, message);
    }

    private static LogContext currentLogContext() {
        LogContext ctx = AbstractContext.current(LogContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getLogContext();
    }

    /**
     * Default constructor.
     */
    protected LogContext() {}

    /**
     * Prefixes all messages being logged by the specified prefixes 
     * (prefixing existing prefixes if any).
     */
    public abstract void prefix(Object... prefixes);

    /**
     * Set the logging level, messages below that level are not logged.
     */
    public abstract void setLevel(Level level);

    /**
     * Suffixes all messages being logged by the specified suffixes
     * (suffixing existing suffixes if any).
     */
    public abstract void suffix(Object... suffixes);

    /**
     * Logs the specified message at the specified level.
     */
    protected abstract void log(Level level, Object... message);

}