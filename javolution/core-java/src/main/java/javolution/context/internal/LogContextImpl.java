/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import javolution.context.LogContext;
import javolution.osgi.internal.OSGiServices;
import javolution.text.TextBuilder;

import org.osgi.service.log.LogService;

/**
 * Holds the default implementation of LogContext.
 */
public final class LogContextImpl extends LogContext {

    /** LogEvent handled to the logging thread. */
    private static class LogEvent {
        Level level;
        Object[] message;
        LogEvent next;
        Object[] prefix;
        Object[] suffix;
    }
    
    /** LoggingThread */
    private static class LoggingThread extends Thread {
        private LogEvent last;
        private TextBuilder message = new TextBuilder();
        private LogEvent next;

        public LoggingThread() {
            super("Javolution Logging");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    log(next());
                } catch (Throwable error) {
                    error.printStackTrace();
                }
            }
        }

        synchronized void add(LogEvent event) {
            if (last == null) {
                last = next = event;
                this.notify();
            } else {
                last.next = event;
                last = event;
            }
        }

        synchronized LogEvent next() throws InterruptedException {
            while (next == null)
                this.wait();
            LogEvent event = next;
            next = next.next;
            if (next == null) {
                last = null;
            }
            return event;
        }

        private void log(LogEvent event) {
            message.clear();
            Throwable exception = null;
            for (Object pfx : event.prefix) {
                message.append(pfx);
            }
            for (Object obj : event.message) {
                if ((exception == null) && (obj instanceof Throwable)) {
                    exception = (Throwable) obj;
                } else {
                    message.append(obj);
                }
            }
            for (Object sfx : event.suffix) {
                message.append(sfx);
            }
            OSGiServices.getLogService().log(
                    TO_OSGI_LEVEL[event.level.ordinal()], message.toString(),
                    exception);
        }
    }
    private static final int[] TO_OSGI_LEVEL = { LogService.LOG_DEBUG,
            LogService.LOG_INFO, LogService.LOG_WARNING, LogService.LOG_ERROR };
    private Level level; // Null to use configurable LEVEL.
    private final LoggingThread loggingThread;

    private Object[] prefix = new Object[0];

    private Object[] suffix = new Object[0];

    /** Default constructor. */
    public LogContextImpl() {
        loggingThread = new LoggingThread();
        loggingThread.start();
    }

    /** Creates an inner context using the specified logging thread. */
    public LogContextImpl(LoggingThread loggingThread) {
        this.loggingThread = loggingThread;
    }

    @Override
    public void prefix(Object... pfx) {
        Object[] tmp = new Object[prefix.length + pfx.length];
        System.arraycopy(pfx, 0, tmp, 0, pfx.length);
        System.arraycopy(prefix, 0, tmp, pfx.length, prefix.length);
        prefix = tmp;
    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void suffix(Object... sfx) {
        Object[] tmp = new Object[suffix.length + sfx.length];
        System.arraycopy(suffix, 0, tmp, 0, suffix.length);
        System.arraycopy(sfx, 0, tmp, suffix.length, sfx.length);
        suffix = tmp;
    }

    @Override
    protected LogContext inner() {
        LogContextImpl ctx = new LogContextImpl(loggingThread);
        ctx.prefix = prefix;
        ctx.suffix = suffix;
        ctx.level = level;
        return ctx;
    }

    @Override
    protected void log(Level level, Object... message) {
        Level thisLevel = (this.level != null) ? this.level : LEVEL.get();
        if (level.compareTo(thisLevel) < 0)
            return;
        LogEvent logEvent = new LogEvent();
        logEvent.level = level;
        logEvent.prefix = prefix;
        logEvent.message = message;
        logEvent.suffix = suffix;
        loggingThread.add(logEvent);
    }
}
