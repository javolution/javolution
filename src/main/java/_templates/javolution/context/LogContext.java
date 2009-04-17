/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.context;

import _templates.java.lang.CharSequence;
import _templates.javolution.Javolution;
import _templates.javolution.lang.Configurable;
import _templates.javolution.text.TextBuilder;
import _templates.javolution.util.StandardLog;

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
 *     capabilities.</p>
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
    public static final Class/*<? extends LogContext>*/ STANDARD = StandardLog.class;
    /**
     * Holds a logging context implementation ignoring logging events.
     */
    public static final Class/*<? extends LogContext>*/ NULL = Null.class;
    /**
     * Holds a context logging debug/informative/warning/error messages
     * to <code>System.out</code>.
     */
    public static final Class/*<? extends LogContext>*/ SYSTEM_OUT = SystemOut.class;
    /**
     * Holds a context logging debug/informative/warnings/errors events to
     * the system console (JVM 1.6+).
     */
    public static final Class/*<? extends LogContext>*/ CONSOLE = Console.class;
    /**
     * Holds the logging context default implementation (configurable, 
     * default value {@link #STANDARD}).
     */
    public static final Configurable/*<Class<? extends LogContext>>*/ DEFAULT = new Configurable(STANDARD) {

        protected void notifyChange() {
            _Default = (LogContext) ObjectFactory.getInstance((Class) get()).object();
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
    public static/*LogContext*/ Context getCurrent() {
        for (Context ctx = Context.getCurrent(); ctx != null; ctx = ctx.getOuter()) {
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
        return ((LogContext) LogContext.getCurrent()).isLogged("debug");
    }

    /**
     * Logs the specified debug message if debug messages are logged.
     *
     * @param message the debug message being logged.
     * @see #logDebug(CharSequence)
     */
    public static void debug(CharSequence message) {
        ((LogContext) LogContext.getCurrent()).logDebug(message);
    }

    /**
     * Equivalent to {@link #debug(CharSequence)} (for J2ME compatibility).
     *
     * @param message the message to log.
     */
    public static void debug(String message) {
        ((LogContext) LogContext.getCurrent()).logDebug(
                Javolution.j2meToCharSeq(message));
    }

    /**
     * Equivalent to {@link #debug(CharSequence) debug(message + obj}}
     * except formatting is done only if {@link #isDebugLogged() debug is logged}.
     *
     * @param message the message to log.
     * @param obj the object whose string representation is logged.
     */
    public static void debug(String message, Object obj) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("debug")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(message);
                tmp.append(obj);
                logContext.logDebug(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #debug(CharSequence)
     * info(messagePart1 + obj + messagePart2}}
     * except formatting is done only if {@link #isDebugLogged() debug is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj the object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     */
    public static void debug(String messagePart1, Object obj, String messagePart2) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("debug")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj);
                tmp.append(messagePart2);
                logContext.logDebug(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #debug(CharSequence)
     * debug(messagePart1 + obj + messagePart2 + obj2}}
     * except formatting is done only if {@link #isDebugLogged() debug is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj1 the first object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     * @param obj2 the second object whose string representation is logged.
     */
    public static void debug(String messagePart1, Object obj1, String messagePart2, Object obj2) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("debug")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj1);
                tmp.append(messagePart2);
                tmp.append(obj2);
                logContext.logDebug(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #debug(CharSequence)
     * debug(messagePart1 + obj + messagePart2 + obj2 + messagePart3}}
     * except formatting is done only if {@link #isDebugLogged() debug is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj1 the first object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     * @param obj2 the second object whose string representation is logged.
     * @param messagePart3 the third part of the message to log.
     */
    public static void debug(String messagePart1, Object obj1, String messagePart2, Object obj2, String messagePart3) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("debug")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj1);
                tmp.append(messagePart2);
                tmp.append(obj2);
                tmp.append(messagePart3);
                logContext.logDebug(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Indicates if info messages are currently logged.
     *
     * @return <code>true</code> if info messages are logged;
     *         <code>false</code> otherwise.
     */
    public static boolean isInfoLogged() {
        return ((LogContext) LogContext.getCurrent()).isLogged("info");
    }

    /**
     * Logs the specified informative message.
     * 
     * @param message the informative message being logged.
     * @see #logInfo(CharSequence)
     */
    public static void info(CharSequence message) {
        ((LogContext) LogContext.getCurrent()).logInfo(message);
    }

    /**
     * Equivalent to {@link #info(CharSequence)} (for J2ME compatibility).
     *
     * @param message the message to log.
     */
    public static void info(String message) {
        ((LogContext) LogContext.getCurrent()).logInfo(
                Javolution.j2meToCharSeq(message));
    }

    /**
     * Equivalent to {@link #info(CharSequence) info(message + obj}}
     * except formatting is done only if {@link #isInfoLogged() info is logged}.
     *
     * @param message the message to log.
     * @param obj the object whose string representation is logged.
     */
    public static void info(String message, Object obj) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("info")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(message);
                tmp.append(obj);
                logContext.logInfo(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #info(CharSequence)
     * info(messagePart1 + obj + messagePart2}}
     * except formatting is done only if {@link #isInfoLogged() info is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj the object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     */
    public static void info(String messagePart1, Object obj, String messagePart2) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("info")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj);
                tmp.append(messagePart2);
                logContext.logInfo(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #info(CharSequence)
     * info(messagePart1 + obj + messagePart2 + obj2}}
     * except formatting is done only if {@link #isInfoLogged() info is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj1 the first object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     * @param obj2 the second object whose string representation is logged.
     */
    public static void info(String messagePart1, Object obj1, String messagePart2, Object obj2) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("info")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj1);
                tmp.append(messagePart2);
                tmp.append(obj2);
                logContext.logInfo(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #info(CharSequence)
     * info(messagePart1 + obj + messagePart2 + obj2 + messagePart3}}
     * except formatting is done only if {@link #isInfoLogged() info is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj1 the first object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     * @param obj2 the second object whose string representation is logged.
     * @param messagePart3 the third part of the message to log.
     */
    public static void info(String messagePart1, Object obj1, String messagePart2, Object obj2, String messagePart3) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("info")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj1);
                tmp.append(messagePart2);
                tmp.append(obj2);
                tmp.append(messagePart3);
                logContext.logInfo(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Indicates if warning messages are currently logged.
     *
     * @return <code>true</code> if warning messages are logged;
     *         <code>false</code> otherwise.
     */
    public static boolean isWarningLogged() {
        return ((LogContext) LogContext.getCurrent()).isLogged("warning");
    }

    /**
     * Logs the specified warning message.
     *
     * @param message the warning message being logged.
     * @see #logWarning(CharSequence)
     */
    public static void warning(CharSequence message) {
        ((LogContext) LogContext.getCurrent()).logWarning(message);
    }

    /**
     * Equivalent to {@link #warning(CharSequence)} (for J2ME compatibility).
     *
     * @param message the message to log.
     */
    public static void warning(String message) {
        ((LogContext) LogContext.getCurrent()).logWarning(
                _templates.javolution.Javolution.j2meToCharSeq(message));
    }

    /**
     * Equivalent to {@link #warning(CharSequence) warning(message + object}}
     * except formatting is done only if {@link #isWarningLogged() warnings
     * are logged}.
     */
    public static void warning(String message, Object obj) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("warning")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(message);
                tmp.append(obj);
                logContext.logWarning(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #warning(CharSequence)
     * warning(messagePart1 + object + messagePart2}}
     * except formatting is done only if {@link #isWarningLogged() warnings
     * are logged}.
     */
    public static void warning(String messagePart1, Object obj, String messagePart2) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("warning")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj);
                tmp.append(messagePart2);
                logContext.logWarning(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #warning(CharSequence)
     * warning(messagePart1 + obj + messagePart2 + obj2}}
     * except formatting is done only if {@link #isWarningLogged() warning is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj1 the first object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     * @param obj2 the second object whose string representation is logged.
     */
    public static void warning(String messagePart1, Object obj1, String messagePart2, Object obj2) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("warning")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj1);
                tmp.append(messagePart2);
                tmp.append(obj2);
                logContext.logWarning(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Equivalent to {@link #warning(CharSequence)
     * warning(messagePart1 + obj + messagePart2 + obj2 + messagePart3}}
     * except formatting is done only if {@link #isWarningLogged() warning is logged}.
     *
     * @param messagePart1 the first part of the message to log.
     * @param obj1 the first object whose string representation is logged.
     * @param messagePart2 the second part of the message to log.
     * @param obj2 the second object whose string representation is logged.
     * @param messagePart3 the third part of the message to log.
     */
    public static void warning(String messagePart1, Object obj1, String messagePart2, Object obj2, String messagePart3) {
        LogContext logContext = (LogContext) LogContext.getCurrent();
        if (logContext.isLogged("warning")) {
            TextBuilder tmp = TextBuilder.newInstance();
            try {
                tmp.append(messagePart1);
                tmp.append(obj1);
                tmp.append(messagePart2);
                tmp.append(obj2);
                tmp.append(messagePart3);
                logContext.logWarning(tmp);
            } finally {
                TextBuilder.recycle(tmp);
            }
        }
    }

    /**
     * Indicates if error messages are currently logged.
     *
     * @return <code>true</code> if error messages are logged;
     *         <code>false</code> otherwise.
     */
    public static boolean isErrorLogged() {
        return ((LogContext) LogContext.getCurrent()).isLogged("error");
    }

    /**
     * Logs the specified error to the current logging context.
     * 
     * @param error the error being logged.
     */
    public static void error(Throwable error) {
        ((LogContext) LogContext.getCurrent()).logError(error, null);
    }

    /**
     * Logs the specified error and error message to the current logging
     * context. 
     * 
     * @param error the error being logged.
     * @param message the supplementary message.
     */
    public static void error(Throwable error, CharSequence message) {
        ((LogContext) LogContext.getCurrent()).logError(error, message);
    }

    /**
     * Equivalent to {@link #error(Throwable, CharSequence)}
     * (for J2ME compatibility).
     */
    public static void error(Throwable error, String message) {
        ((LogContext) LogContext.getCurrent()).logError(
                error, _templates.javolution.Javolution.j2meToCharSeq(message));
    }

    /**
     * Logs the specified error message to the current logging
     * context. 
     * 
     * @param message the error message being logged.
     */
    public static void error(CharSequence message) {
        ((LogContext) LogContext.getCurrent()).logError(null, message);
    }

    /**
     * Equivalent to {@link #error(CharSequence)} (for J2ME compatibility).
     */
    public static final void error(String message) {
        ((LogContext) LogContext.getCurrent()).logError(
                null, _templates.javolution.Javolution.j2meToCharSeq(message));
    }

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
                /*@JVM-1.4+@
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