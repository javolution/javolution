/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javolution.context.LogContext;
import javolution.internal.osgi.JavolutionActivator;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import org.osgi.service.log.LogService;

/**
 * Holds the default implementation of LogContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class LogContextImpl extends LogContext {

    private final FastMap properties = new FastMap();

    private Level suppressLevel;

    @Override
    protected LogContext inner() {
        LogContextImpl ctx = new LogContextImpl();
        ctx.properties.putAll(properties);
        ctx.suppressLevel = suppressLevel;
        return ctx;
    }

    @Override
    public void attach(Object property, Object propertyValue) {
        properties.put(property, propertyValue);
    }

    @Override
    public void suppress(Level level) {
        if ((suppressLevel != null) && (suppressLevel.compareTo(level) > 0))
            return; // No effect level already suppressed.
        suppressLevel = level;
    }

    @Override
    protected void log(Level level, Object... objs) {
        if (level.compareTo(suppressLevel) <= 0) return;
        final TextBuilder msg = new TextBuilder();
        for (Iterator<Entry> it = properties.entrySet().iterator(); it.hasNext();) {
            Entry e = it.next();
            msg.append('[');
            msg.append(e.getKey());
            msg.append(": ");
            msg.append(e.getValue());
            msg.append("] ");
        }
        Throwable error = null;
        for (Object obj : objs) {
            msg.append(obj);
            if (obj instanceof Throwable) {
                error = (Throwable) obj;
            }
        }
        LogService logService = JavolutionActivator.getLogService();
        if (logService != null) { // OSGi LogService.
            if (error != null) {
                logService.log(TO_OSGI_LEVEL[level.ordinal()], msg.toString(), error);
            } else {
                logService.log(TO_OSGI_LEVEL[level.ordinal()], msg.toString());
            }
        } else { // Standard java.util.logging.
            if (error != null) {
                GLOBAL_LOGGER.log(TO_UTIL_LEVEL[level.ordinal()], msg.toString(), error);
            } else {
                GLOBAL_LOGGER.log(TO_UTIL_LEVEL[level.ordinal()], msg.toString());
            }
        }
    }

    private static final int[] TO_OSGI_LEVEL = {LogService.LOG_DEBUG, LogService.LOG_INFO,
        LogService.LOG_WARNING, LogService.LOG_ERROR};

    private static final java.util.logging.Level[] TO_UTIL_LEVEL = {java.util.logging.Level.FINE, java.util.logging.Level.INFO,
        java.util.logging.Level.WARNING, java.util.logging.Level.SEVERE};

    private static final Logger GLOBAL_LOGGER = Logger.getGlobal();

}
