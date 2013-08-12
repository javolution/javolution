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
import javolution.osgi.internal.OSGiServices;
import javolution.text.TextContext;

/**
 * <p> Asynchronous logging context integrated with the OSGi logging framework.
 *     The logging back-end, or how the log entries are displayed, stored, or
 *     processed is unspecified but always performed asynchronously. 
 *     When running outside OSGi, log messages are sent to {@link System#out}. 
 *     Message formatting itself is always performed synchronously using the 
 *     current {@link TextContext}.</p> 
 *     
 * <p> Logging contexts support automatic prefixing/suffixing of any information 
 *     relevant to the user/developer (thread info, user id, and so on). 
 * [code]
 * void run() {
 *     LogContext ctx = LogContext.enter(); 
 *     try {
 *         // Prefix the executing thread to any message being logged.
 *         ctx.prefix("[Thread: ", Thead.currentThread(), "] "); 
 *         ... 
 *      } finally {
 *         ctx.exit();
 *      }
 *  }[/code]</p>
 *  
 *  <p> Applications should separate messages elements by commas and not 
 *      use {@link String} concatenations when calling log methods otherwise 
 *      the concatenation is performed even when log events are filtered out.
 * [code]
 * LogContext ctx = LogContext.enter();
 * try {
 *     ctx.setLevel(Level.INFO); // Does not log debug messages. 
 *     ... 
 *     LogContext.debug("Index: ", index, " at maximum value"); // GOOD, no formatting performed !
 *     LogContext.debug("Index: " + index + " at maximum value"); // BAD, formatting performed even though nothing is logged !
 *     ...
 * } finally {
 *     ctx.exit();
 * }[/code]</p>
 *  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see <a href="https://code.google.com/p/osgi-logging/wiki/UnderstandingTheOSGiLogging">Understanding OSGi Logging</a>
 */
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
    public static final Configurable<Level> LEVEL = new Configurable<Level>() {
        @Override
        protected Level getDefault() {
            return Level.DEBUG;
        }
        @Override
        public Level parse(String str) {
            return Level.valueOf(str);
        }
    };

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
        LogContext ctx = current(LogContext.class);
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