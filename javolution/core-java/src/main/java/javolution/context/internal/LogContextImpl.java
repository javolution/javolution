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
 * The default implementation of LogContext.
 */
public final class LogContextImpl extends LogContext {

    private static final Object[] NONE = new Object[0];
    private static final int[] TO_OSGI_LEVEL = { LogService.LOG_DEBUG,
            LogService.LOG_INFO, LogService.LOG_WARNING, LogService.LOG_ERROR };

    private Level level; // Null to use configurable LEVEL.
    private Object[] prefix = NONE;
    private Object[] suffix = NONE;

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
        LogContextImpl ctx = new LogContextImpl();
        ctx.prefix = prefix;
        ctx.suffix = suffix;
        ctx.level = level;
        return ctx;
    }

    @Override
    protected void log(Level level, Object... message) {
        if (level.compareTo(currentLevel()) < 0)
            return;
        TextBuilder tmp = new TextBuilder();
        Throwable exception = null;
        for (Object pfx : prefix) {
            tmp.append(pfx); // Uses TextContext for formatting.
        }
        for (Object obj : message) {
            if ((exception == null) && (obj instanceof Throwable)) {
                exception = (Throwable) obj;
            } else {
                tmp.append(obj); // Uses TextContext for formatting.
            }
        }
        for (Object sfx : suffix) {
            tmp.append(sfx); // Uses TextContext for formatting.
        }
        int osgiLevel = TO_OSGI_LEVEL[level.ordinal()];
        String msg = tmp.toString();
        Object[] logServices = OSGiServices.getLogServices();
        for (Object logService : logServices) {
            ((LogService)logService).log(osgiLevel, msg, exception);
        }
    }
    private Level currentLevel() {
        if (LEVEL == null) return Level.DEBUG; // Only during class initialization.
        if (level == null) return LEVEL.get();
        return level;
    }
}
