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
import javolution.text.TypeFormat;

/**
 * <p> This class represents the logging context. Typically, logging contexts
 *     forward messages to the runtime OSGi {@link org.osgi.service.log.LogService 
 *     LogService}. Beside their ease of use, they also provide additional 
 *     capabilities such as custom attachments, log level filtering, etc.
 *     [code]
 *     void performTransaction(UserID userId) {
 *         LogContext ctx = LogContext.enter(); 
 *         try {
 *             ctx.attach("User ID", userId); // Attaches the specified properties.
 *             ... 
 *             LogContext.info("Overdraft of ", amount); 
 *                 // Message logged (default impl.) "[User ID: <userId>] Overdraft of <amount>"
 *             ...
 *         } finally {
 *             ctx.exit(); // Reverts to previous header.
 *         }
 *     }[/code]
 *     If logging is not performed, no string formatting is performed.
 *     [code]
 *     // Suppress Warnings (e.g. warnings have been identified as harmless)
 *     LogContext ctx = LogContext.enter();
 *     try {
 *          ctx.suppress(Level.WARNING); // Has no effect if WARNING/ERROR are already suppressed. 
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

        DEBUG, INFO, WARNING, ERROR
    }
    
    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     * This parameter cannot be locally overriden.
     */
    public static final LocalParameter<Boolean> WAIT_FOR_SERVICE = new LocalParameter(false) {
        @Override
        public void configure(CharSequence configuration) {
            setDefault(TypeFormat.parseBoolean(configuration));
        }

        @Override
        public void checkOverridePermission() throws SecurityException {
            throw new SecurityException(this + " cannot be overriden");
        }
    };
    
    /**
     * Holds the level for which log entries are not created (default 
     * <code>null</code> all entries are created). For example, running with 
     * the option <code>-Djavolution.context.LogContext#SUPPRESS=DEBUG</code>  
     * causes debug information not to be logged. 
     */
    public static final LocalParameter<Level> SUPPRESS = new LocalParameter(null) {
        @Override
        public void configure(CharSequence configuration) {
            String str = configuration.toString();
            setDefault((str.length() != 0) ? Level.valueOf(str) : null);
        }
    };

    /**
     * Default constructor.
     */
    protected LogContext() {
    }

    /**
     * Enters a new log context instance.
     * 
     * @return the new log context implementation entered.
     */
    public static LogContext enter() {
        LogContext ctx = AbstractContext.current(LogContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return LOG_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault()).inner().enterScope();
    }

    /**
     * Logs the specified debug message. 
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void debug(Object... objs) {
        LogContext ctx = AbstractContext.current(LogContext.class);
        if (ctx != null) {
            ctx = LOG_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault());
        }
        ctx.log(Level.DEBUG, objs);
    }

    /**
     * Logs the specified info message. 
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void info(Object... objs) {
        LogContext ctx = AbstractContext.current(LogContext.class);
        if (ctx != null) {
            ctx = LOG_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault());
        }
        ctx.log(Level.INFO, objs);
    }

    /**
     * Logs the specified warning message. 
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void warning(Object... objs) {
        LogContext ctx = AbstractContext.current(LogContext.class);
        if (ctx != null) {
            ctx = LOG_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault());
        }
        ctx.log(Level.WARNING, objs);
    }

    /**
     * Logs the specified error message (which may include {@link Throwable}
     * instances).
     *
     * @param objs the objects whose textual representation is the message logged.
     */
    public static void error(Object... objs) {
        LogContext ctx = AbstractContext.current(LogContext.class);
        if (ctx != null) {
            ctx = LOG_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefault());
        }
        ctx.log(Level.ERROR, objs);
    }

    /**
     * Attaches the specified property (inherited by inner log context).
     */
    public abstract void attach(Object property, Object propertyValue);

    /**
     * Don't create log entries for message of specified level or below. 
     * 
     * @param level the log context level being suppressed. 
     */
    public abstract void suppress(Level level);

    /**
     * Logs the specified entry.
     * 
     * @param level the log entry level.
     * @param objs the objects whose textual representation is the message 
     *        logged (may include exceptions).
     */
    protected abstract void log(Level level, Object... objs);
}
