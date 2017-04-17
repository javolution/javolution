/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context;

import org.javolution.lang.Configurable;
import org.javolution.osgi.internal.OSGiServices;

/**
 * Context sensitive logging supporting various logging frameworks.
 * 
 * `LogContext` may perform different operations or even use different logging frameworks based upon the current thread 
 * execution context. For example, a server processing multiple users simultaneously may log information 
 * differently for each user.  
 * 
 * The logging back-end, or how the log entries are displayed, stored, or processed is unspecified at low-level source 
 * code (it is context dependant) but always performed asynchronously (in a separate thread). 
 * 
 * The default backend is either [OSGi LogService] or [SLF4J Logging Facade] when running outside of an OSGi container.
 * 
 * Custom contexts may superceed the default context either through OSGi publication 
 * (`org.javolution.context.LogContext`) or by entering a local context instance.
 * 
 * ```java
 * public static void main(String... args) {
 *     MyContext ctx = new MyContext(); // Custom context.
 *     ctx.enter(); 
 *     try {
 *         ...; // MyContext for current thread and inner concurrent threads (see ConcurrentContext).
 *     } finally {
 *         ctx.exit(); 
 *     }
 * }
 * ```
 *  
 * Applications should separate messages elements by commas and not use {@link String} concatenations when calling log 
 * methods to avoid loading the current thread.
 * 
 * ```java
 * LogContext ctx = LogContext.enter(); // Enter local inner context.
 * try {
 *     // Local configuration.
 *     ctx.setPrefix("[Thread: " + Thread.currentThread().getName() + "] "); // Prefix for all messages.
 *     ctx.setLevel(Level.INFO); // Does not log debug messages.
 *     ...
 *     LogContext.debug("Index: " + index + " at maximum value"); // BAD - Formatting performed but nothing is logged.
 *     LogContext.debug("Index: ", index, " at maximum value"); // OK - No formatting performed when debug disabled.
 * } finally {
 *     ctx.exit(); // Exit local settings.
 * }
 * ```
 * 
 * LogContext supports [Mapped Diagnostic Context] to debug complex distributed applications. Typically,
 * while starting to service a new client request, the developpper inserts pertinent contextual information
 * such as the client IP address or the client ID. This mapping gets automatically cleared upon context exit.  
 * 
 * ```java
 * LogContext ctx = LogContext.enter(); // Enter local inner context.
 * try {
 *     ctx.put("Host", Server.getClientHost()); // org.slf4j.MDC.put(...) when using SLF4J
 *     ctx.put("Client", clientID);
 *     ...
 * } finally {
 *     ctx.exit(); // Removes "Host" and "Client" mapping.
 * }
 * ```
 *  
 * [OSGi LogService]: https://osgi.org/javadoc/r4v42/org/osgi/service/log/LogService.html
 * [[SLF4J Logging Facade]: https://www.slf4j.org/
 * [Mapped Diagnostic Context]: https://logback.qos.ch/manual/mdc.html 
 * 
 * @author  <jean-marie@dautelle.com>
 * @version 7.0, March 31, 2017
 * 
 */
public abstract class LogContext extends AbstractContext {

    /**
     * Defines the logging levels.
     */
    public enum Level {
        DEBUG, INFO, WARNING, ERROR, FATAL
    }

    /**
     * Holds the default logging level (<code>INFO</code>). This level is configurable. For example, running with 
     * the option `-Djavolution.context.LogContext#DEFAULT_LEVEL=WARNING` causes the debug/info not to be logged. 
     */
    public static final Configurable<Level> DEFAULT_LEVEL = new Configurable<Level>() {
        @Override
        protected Level getDefault() {
            return Level.INFO;
        }
        @Override
        public Level parse(String str) {
            return Level.valueOf(str);
        }
    };
    
    /**
     * Default constructor.
     */
    protected LogContext() {}

    /**
     * Enters an inner log context instance.
     * 
     * @return the new context entered.
     */
    public static LogContext enter() {
        return (LogContext) currentLogContext().enterInner();
    }

    /**
     * Logs the specified debug messages.
     * 
     * @param message 
     */
    public static void debug(Object... messages) {
        currentLogContext().log(Level.DEBUG, null, messages);
    }

    /**
     * Logs the specified info messages.
     * 
     * @param message 
     */
    public static void info(Object... messages) {
        currentLogContext().log(Level.INFO, null, messages);
    }

    /**
     * Logs the specified warning messages.
     * 
     * @param message 
     */
    public static void warning(Object... messages) {
        currentLogContext().log(Level.WARNING, null, messages);
    }

    /**
     * Logs the specified error messages.
     * 
     * @param message
     */
    public static void error(Object... messages) {
       currentLogContext().log(Level.ERROR, null, messages);
    }

    /**
     * Logs the specified error and error messages.
     * 
     * @param message
     */
    public static void error(Throwable error, Object... messages) {
       currentLogContext().log(Level.ERROR, error, messages);
    }

    /**
     * Set the logging level, messages below that level are ignored.
     * 
     * @param level
     */
    public abstract void setLevel(Level level);

    /**
     * Returns the actual logging level of this context (taking into account outer logging contexts levels if any).
     * 
     * @return the logging context
     */
    public abstract Level level();


    /**
     * Prepends the output messages with the specified prefixes (they are appended to outer prefixes if any).
     * 
     * @param prefixes 
     */
    public abstract void setPrefix(Object... prefixes);

    /**
     * Returns the actual prefix for the messages logged by this context (taking into account outer logging 
     * contexts prefixes if any)
     * 
     * @return the messages prefix.
     */
    public abstract String prefix();

    /**
     * Appends the output messages with the specified suffixes (they are prepended to outer suffixes if any).
     * 
     * @param suffixes
     */
    public abstract void setSuffix(Object... suffixes);

    /**
     * Returns the actual suffix for the messages logged by this context (taking into account outer logging 
     * contexts suffixes if any)
     * 
     * @return the messages suffix.
     */
    public abstract String suffix();

    /**
     * Put a diagnostic context value as identified with the key parameter. The mapping is automatically 
     * removed upon context exit.
     * 
     * @param key
     * @param value 
     */
    public abstract void put(String key, Object value);

    /**
     * Logs the specified message at the specified level.
     * 
     * @param level
     * @param error (can be `null`)
     * @param message
     */
    protected abstract void log(Level level, Throwable error, Object... message);

    /** Returns the current LogContext. */
    private static LogContext currentLogContext() {
        LogContext ctx = current(LogContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getLogContext();
    }

}