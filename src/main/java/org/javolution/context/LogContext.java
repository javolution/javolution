/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context;

import java.lang.CharSequence;

import org.javolution.lang.Configurable;
import org.javolution.text.Text;
import org.javolution.text.TextBuilder;
import org.javolution.util.StandardLog;

/**
 * <p> This class represents a context for object-based/thread-based logging
 *     capabilities.</p>
 *
 * <p> LogContext removes low level code dependency with the logging framework.
 *     The same code can run using system out/err, standard logging
 *     (<code>java.util.logging</code>), Log4J or even OSGI Log services.
 *     Selection can be done at run-time through {@link #DEFAULT
 *     configuration}).</p>
 *
 * <p> The {@link #DEFAULT default} logging context is {@link
 *     StandardLog StandardLog} to leverage <code>java.util.logging</code>
 *     capabilities unless an OSGi LogService instance is found in which
 *     case it is being used (configuration done during Javolution bundle
 *     activation).</p>
 *
 * <p> Logging a message is quite simple:(code)
 *         LogContext.info("my message");(/code]
 *     Because string formatting can be slow, we also find:[code]
 *         if (LogContext.isInfoLogged())
 *             LogContext.info("message part 1" + aVar + "message part 2");[/code]
 *     Or equivalent but simpler:[code]
 *         LogContext.info("message part 1", aVar, "message part 2");[/code]</p>
 *
 * <p> Logging can be temporarily altered on a thread or object basis.
 *     For example:[code]
 *     public static main(String[] args) {
 *         LogContext.enter(LogContext.NULL); // Temporarily disables logging.
 *         try { 
 *             ClassInitializer.initializeAll();  // Initializes bootstrap, extensions and classpath classes.
 *         } finally {
 *             LogContext.exit(LogContext.NULL); // Goes back to default logging.
 *         }
 *         ...
 *     }[/code]</p>
 *
 * <p> Applications may extend this base class to address specific logging
 *     requirements. For example:[code]
 *     // This class allows for custom logging of session events. 
 *     public abstract class SessionLog extends LogContext  {
 *         public static void start(Session session) {
 *             LogContext log = LogContext.current();
 *             if (log instanceof SessionLog.Loggable) { 
 *                 ((SessionLog.Loggable)log).logStart(session);
 *             } else if (log.infoLogged()){
 *                 log.logInfo("Session " + session.id() + " started");
 *             }
 *         }
 *         public static void end(Session session) { ... }
 *         public interface Loggable { 
 *             void logStart(Session session);
 *             void logEnd(Session session);
 *         }
 *     }[/code]</p>
 *     
 * <p> The use of interfaces (such as <code>Loggable</code> above) makes it easy
 *     for any context to support customs logging events.
 *     For example:[code]
 *     class MyLog extends StandardLog implements SessionLog.Loggable, DatabaseLog.Loggable { 
 *         ...   // Specialized logging for session and database events. 
 *     }
 *     MyLog myLog = new MyLog();
 *     LogContext.enter(myLog);
 *     try {
 *         ...
 *         LogContext.info("Informative message"); // Standard logging.   
 *         ...
 *         DatabaseLog.fail(transaction); // Database custom logging.
 *         ... 
 *         SessionLog.start(session); // Session custom logging.
 *         ...
 *     } finally {
 *         LogContext.exit(myLog);
 *     }[/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 5, 2009
 */
public abstract class LogContext extends Context {

    /**
     * Holds the default logging context instance.
     */
    private static volatile LogContext _Default = new StandardLog();

    /**
     * Holds the logging context implementation forwarding log events to the 
     * root <code>java.util.logging.Logger</code> (default logging context).
     * The debug/info/warning/error events are mapped to the
     * debug/info/warning/severe log levels respectively.
     */
    public static final Class <? extends LogContext>  STANDARD = StandardLog.class;

    /**
     * Holds a logging context implementation ignoring logging events.
     */
    public static final Class <? extends LogContext>  NULL = Null.class;

    /**
     * Holds a context logging debug/informative/warning/error messages
     * to <code>System.out</code>.
     */
    public static final Class <? extends LogContext>  SYSTEM_OUT = SystemOut.class;

    /**
     * Holds a context logging debug/informative/warnings/errors events to
     * the system console (JVM 1.6+).
     */
    public static final Class <? extends LogContext>  CONSOLE = Console.class;

    /**
     * Holds the logging context default implementation (configurable, 
     * default value {@link #STANDARD}).
     */
    public static final Configurable <Class<? extends LogContext>>  DEFAULT = new Configurable(STANDARD) {

        protected void notifyChange(Object oldValue, Object newValue) {
            _Default = (LogContext) ObjectFactory.getInstance((Class) newValue).object();
        }
    };

    /**
     * Default constructor.
     */
    protected LogContext() {
    }

    /**
     * Returns the current logging context. If the current thread has not 
     * entered any logging context the {@link #getDefault()} is returned.
     *
     * @return the current logging context.
     */
    public static LogContext getCurrentLogContext() {
        for (Context ctx = Context.getCurrentContext(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof LogContext)
                return (LogContext) ctx;
        }
        return LogContext._Default;
    }

    /**
     * Returns the default instance ({@link #DEFAULT} implementation).
     *
     * @return the default instance.
     */
    public static LogContext getDefault() {
        return LogContext._Default;
    }

    /**
     * Indicates if debug messages are currently logged.
     *
     * @return <code>true</code> if debug messages are logged;
     *         <code>false</code> otherwise.
     */
    public static boolean isDebugLogged() {
        return ((LogContext) LogContext.getCurrentLogContext()).isLogged("debug");
    }

    /**
     * Logs the specified debug message if debug messages are logged.
     *
     * @param message the debug message being logged.
     * @see #logDebug(CharSequence)
     */
    public static void debug(CharSequence message) {
        ((LogContext) LogContext.getCurrentLogContext()).logDebug(message);
    }

    /**
     * Equivalent to {@link #debug(CharSequence)} except that formatting
     * is done only if debug is logged.
     *
     * @param message the message to log.
     */
    public static void debug(Object message) {
        LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
        if (!logContext.isLogged("debug"))
            return;
        logContext.logDebug(Text.valueOf(message));
    }

    /**
     * Equivalent to {@link #debug(CharSequence)} except that formatting
     * is done only if debug is logged.
     *
     * @param messages the messages to log.
    */
    public static void debug(Object... messages) {
    LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
    if (!logContext.isLogged("debug"))
    return;
    Text tmp = Text.valueOf(messages[0]);
    for (int i=1; i < messages.length; i++) {
    tmp = tmp.plus(messages[i]);
    }
    logContext.logDebug(tmp);
    }
    /**/
    /**
     * Indicates if info messages are currently logged.
     *
     * @return <code>true</code> if info messages are logged;
     *         <code>false</code> otherwise.
     */
    public static boolean isInfoLogged() {
        return ((LogContext) LogContext.getCurrentLogContext()).isLogged("info");
    }

    /**
     * Logs the specified informative message.
     * 
     * @param message the informative message being logged.
     * @see #logInfo(CharSequence)
     */
    public static void info(CharSequence message) {
        ((LogContext) LogContext.getCurrentLogContext()).logInfo(message);
    }

    /**
     * Equivalent to {@link #info(CharSequence)} except that formatting
     * is done only if info is logged.
     *
     * @param message the message to log.
     */
    public static void info(Object message) {
        LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
        if (!logContext.isLogged("info"))
            return;
        logContext.logInfo(Text.valueOf(message));
    }

    /**
     * Equivalent to {@link #info(CharSequence)} except that formatting
     * is done only if info is logged.
     *
     * @param messages the messages to log.
    */
    public static void info(Object... messages) {
    LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
    if (!logContext.isLogged("info"))
    return;
    Text tmp = Text.valueOf(messages[0]);
    for (int i = 1; i < messages.length; i++) {
    tmp = tmp.plus(messages[i]);
    }
    logContext.logInfo(tmp);
    }
    /**/
    /**
     * Indicates if warning messages are currently logged.
     *
     * @return <code>true</code> if warning messages are logged;
     *         <code>false</code> otherwise.
     */
    public static boolean isWarningLogged() {
        return ((LogContext) LogContext.getCurrentLogContext()).isLogged("warning");
    }

    /**
     * Logs the specified warning message.
     *
     * @param message the warning message being logged.
     * @see #logWarning(CharSequence)
     */
    public static void warning(CharSequence message) {
        ((LogContext) LogContext.getCurrentLogContext()).logWarning(message);
    }

    /**
     * Equivalent to {@link #warning(CharSequence)} except that formatting
     * is done only if warning is logged.
     *
     * @param message the message to log.
     */
    public static void warning(Object message) {
        LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
        if (!logContext.isLogged("warning"))
            return;
        logContext.logWarning(Text.valueOf(message));
    }

    /**
     * Equivalent to {@link #warning(CharSequence)} except that formatting
     * is done only if warning is logged.
     *
     * @param messages the messages to log.
    */
    public static void warning(Object... messages) {
    LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
    if (!logContext.isLogged("warning"))
    return;
    Text tmp = Text.valueOf(messages[0]);
    for (int i = 1; i < messages.length; i++) {
    tmp = tmp.plus(messages[i]);
    }
    logContext.logWarning(tmp);
    }
    /**/
    /**
     * Indicates if error messages are currently logged.
     *
     * @return <code>true</code> if error messages are logged;
     *         <code>false</code> otherwise.
     */
    public static boolean isErrorLogged() {
        return ((LogContext) LogContext.getCurrentLogContext()).isLogged("error");
    }

    /**
     * Logs the specified error to the current logging context.
     * 
     * @param error the error being logged.
     */
    public static void error(Throwable error) {
        ((LogContext) LogContext.getCurrentLogContext()).logError(error, null);
    }

    /**
     * Logs the specified error and error message to the current logging
     * context. 
     * 
     * @param error the error being logged.
     * @param message the supplementary message.
     */
    public static void error(Throwable error, CharSequence message) {
        ((LogContext) LogContext.getCurrentLogContext()).logError(error, message);
    }

    /**
     * Equivalent to {@link #error(Throwable, CharSequence)} except that
     * formatting is done only if error is logged.
     *
     * @param error the error being logged.
     * @param message the supplementary message.
     */
    public static void error(Throwable error, Object message) {
        LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
        if (!logContext.isLogged("error"))
            return;
        logContext.logError(error, Text.valueOf(message));
    }

    /**
     * Equivalent to {@link #error(Throwable, CharSequence)}
     * except that formatting is done only if error is logged.
     *
     * @param error the error being logged.
     * @param messages the supplementary messages.
    */
    public static void error(Throwable error, Object... messages) {
    LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
    if (!logContext.isLogged("error"))
    return;
    Text tmp = Text.valueOf(messages[0]);
    for (int i = 1; i < messages.length; i++) {
    tmp = tmp.plus(messages[i]);
    }
    logContext.logError(error, tmp);
    }
    /**/
    /**
     * Logs the specified error message to the current logging
     * context. 
     * 
     * @param message the error message being logged.
     */
    public static void error(CharSequence message) {
        ((LogContext) LogContext.getCurrentLogContext()).logError(null, message);
    }

    /**
     * Equivalent to {@link #error(CharSequence)} except that formatting
     * is done only if error is logged.
     *
     * @param message the message to log.
     */
    public static void error(Object message) {
        LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
        if (!logContext.isLogged("error"))
            return;
        logContext.logError(null, Text.valueOf(message));
    }

    /**
     * Equivalent to {@link #error(CharSequence)}
     * except that formatting is done only if error is logged.
     *
     * @param messages the messages to log.
    */
    public static void error(Object... messages) {
    LogContext logContext = (LogContext) LogContext.getCurrentLogContext();
    if (!logContext.isLogged("error"))
    return;
    Text tmp = Text.valueOf(messages[0]);
    for (int i = 1; i < messages.length; i++) {
    tmp = tmp.plus(messages[i]);
    }
    logContext.logError(null, tmp);
    }
    /**/
    /**
     * Logs the message of specified category (examples of category are
     * "debug", "info", "warning", "error").
     *
     * @param category an identifier of the category of the messages logged.
     * @param message the message itself.
     */
    protected abstract void logMessage(String category, CharSequence message);

    /**
     * Indicates if the messages of the specified category are being logged
     * (default <code>true</code> all messages are being logged).
     * 
     * <p>Note: This method is an indicator only, not a directive.
     *          It allows users to bypass the logging processing if no
     *          actual logging is performed. If the category is not
     *          known then this method should return <code>true</code>
     *          (no optimization performed).</p>
     * 
     * @param category an identifier of the category for the messages logged.
     * @return <code>true</code> if the messages of the specified category
     *         are being logged; <code>false</code> otherwise.
     */
    protected boolean isLogged(String category) {
        return true;
    }

    /**
     * Logs the specified debug message.
     * 
     * @param message the debug message to be logged.
     * @see #logMessage
     */
    protected void logDebug(CharSequence message) {
        logMessage("debug", message);
    }

    /**
     * Logs the specified informative message.
     *
     * @param message the informative message to be logged.
     */
    protected void logInfo(CharSequence message) {
        logMessage("info", message);
    }

    /**
     * Logs the specified warning message.
     *
     * @param message the warning message to be logged.
     */
    protected void logWarning(CharSequence message) {
        logMessage("warning", message);
    }

    /**
     * Logs the specified error.
     * The default implementation logs the message and the error stack trace
     * (calls <code>logMessage("", message + stackTrace)</code>.
     *
     * @param error the error being logged or <code>null</code> if none.
     * @param message the associated message or <code>null</code> if none.
     */
    protected void logError(Throwable error, CharSequence message) {
        TextBuilder tmp = TextBuilder.newInstance();
        try {
            if (error != null) {
                tmp.append(error.getClass().getName());
                tmp.append(" - ");
            }
            if (message != null)
                tmp.append(message);
            else if (error != null) // Use the error message if no message specified.
                tmp.append(error.getMessage());
            if (error != null) { // Outputs error stack trace.
                /**/
                StackTraceElement[] trace = error.getStackTrace();
                for (int i = 0; i < trace.length; i++) {
                tmp.append("\n\tat ");
                tmp.append(trace[i]);
                }
                /**/
            }
            logMessage("error", tmp);
        } finally {
            TextBuilder.recycle(tmp);
        }
    }

    // Implements Context abstract method.
    protected void enterAction() {
        // Do nothing.
    }

    // Implements Context abstract method.
    protected void exitAction() {
        // Do nothing.
    }

    /**
     * This class represents the system logging context.
     */
    private static class SystemOut extends LogContext {

        protected void logMessage(String category, CharSequence message) {
            System.out.print("[");
            System.out.print(category);
            System.out.print("] ");
            System.out.println(message);
        }
    }

    /**
     * This class represents a non-logging context.
     */
    private static final class Null extends SystemOut {

        protected boolean isLogged(String category) {
            return false;
        }

        protected void logMessage(String category, CharSequence message) {
            // Do nothing.
        }
    }

    /**
     * This class represents the console logging context.
     */
    private static class Console extends SystemOut {
        /*@JVM-1.6+@
        final java.io.PrintWriter writer;
        Console() {
        java.io.Console console = System.console();
        writer = console != null ? console.writer() : null;
        }

        @Override
        protected void logMessage(String category, CharSequence message) {
        if (writer == null) {
        super.logMessage(category, message);
        } else {
        writer.print("[");
        writer.print(category);
        writer.print("] ");
        writer.println(message);
        }
        }
        /**/
    }

    // Allows instances of private classes to be factory produced. 
    static {
        ObjectFactory.setInstance(new ObjectFactory() {

            protected Object create() {
                return new Console();
            }
        }, CONSOLE);
        ObjectFactory.setInstance(new ObjectFactory() {

            protected Object create() {
                return new Null();
            }
        }, NULL);
        ObjectFactory.setInstance(new ObjectFactory() {

            protected Object create() {
                return new SystemOut();
            }
        }, SYSTEM_OUT);
    }
}
