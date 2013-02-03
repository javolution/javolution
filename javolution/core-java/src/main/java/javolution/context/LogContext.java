/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import static javolution.internal.osgi.JavolutionActivator.LOG_CONTEXT_TRACKER;
import javolution.lang.Configurable;
import javolution.text.TypeFormat;

/**
 * <p> A logging context integrated with OSGi {@link org.osgi.service.log.LogService 
 *     LogService}. Beside their ease of use, this context provides additional 
 *     capabilities such as custom attachments, log level filtering, etc.
 *     [code]
 *     void performTransaction(UserID userId) {
 *         LogContext ctx = LogContext.enter(); 
 *         try {
 *             ctx.attach("User ID", userId); // Attaches the specified properties.
 *             ... 
 *             LogContext.info("Overdraft of ", amount); 
 *                 // Message logged (default impl.): "[User ID: <userId>] Overdraft of <amount>"
 *             ...
 *         } finally {
 *             ctx.exit(); // Reverts to previous header.
 *         }
 *     }[/code]
 *     If logging is not performed, no message formatting is performed.
 *     [code]
 *     // Suppress Warnings (e.g. warnings have been identified as harmless)
 *     LogContext ctx = LogContext.enter();
 *     try {
 *          ctx.setLevel(Level.ERROR); 
 *          ... 
 *          LogContext.warning(myObject, " is not initialized");
 *              // No log entries created and no message formatting. 
 *          ...
 *     } finally {
 *          ctx.exit(); // Reverts to previous settings.
 *     }[/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public abstract class LogContext extends AbstractContext<LogContext> {

    /**
     * Defines the logging levels.
     */
    public enum Level {

        DEBUG, INFO, WARNING, ERROR, FATAL

    }

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable<Boolean>(
            false) {

        @Override
        public void configure(CharSequence configuration) {
            setDefaultValue(TypeFormat.parseBoolean(configuration));
        }

    };

    /**
     * Holds the logging level (default <code>DEBUG</code>).
     * This level is configurable. For example, running with 
     * the option <code>-Djavolution.context.LogContext#LEVEL=WARNING</code>  
     * causes the debug/info not to be logged. 
     */
    public static final Configurable<Level> LEVEL = new Configurable<Level>(
            Level.DEBUG) {

        @Override
        public void configure(CharSequence configuration) {
            String str = configuration.toString();
            setDefaultValue(Level.valueOf(str));
        }

    };

    /**
     * Default constructor.
     */
    protected LogContext() {}

    /**
     * Enters a new log context instance.
     * 
     * @return the new log context implementation entered.
     */
    public static LogContext enter() {
        return LogContext.current().inner().enterScope();
    }

    /**
     * Logs the specified debug message. 
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void debug(Object... objs) {
        LogContext.current().log(Level.DEBUG, objs);
    }

    /**
     * Logs the specified info message. 
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void info(Object... objs) {
        LogContext.current().log(Level.INFO, objs);
    }

    /**
     * Logs the specified warning message. 
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void warning(Object... objs) {
        LogContext.current().log(Level.WARNING, objs);
    }

    /**
     * Logs the specified error message (which may include {@link Throwable}
     * instances).
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void error(Object... objs) {
        LogContext.current().log(Level.ERROR, objs);
    }

    /**
     * Attaches the specified property (inherited by inner log context).
     */
    public abstract void attach(Object property, Object propertyValue);

    /**
     * Set the logging level, messages below that level are not logged.
     * Setting the level may decrease or increase the logging level.
     * 
     * @param level the log context level. 
     */
    public abstract void setLevel(Level level);

    /**
     * Logs the specified entry.
     * 
     * @param level the log entry level.
     * @param objs the objects whose textual representation is the message 
     *        logged (may include exceptions).
     */
    protected abstract void log(Level level, Object... objs);

    /**
     * Returns the current log context.
     */
    protected static LogContext current() {
        LogContext ctx = AbstractContext.current(LogContext.class);
        if (ctx != null)
            return ctx;
        return LOG_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE
                .getDefaultValue());
    }

}
