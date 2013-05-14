/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.context;

import java.util.Map.Entry;
import javolution.context.LogContext;
import javolution.internal.osgi.JavolutionActivator;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import javolution.util.function.Predicate;

import org.osgi.service.log.LogService;

/**
 * Holds the default implementation of LogContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class LogContextImpl extends LogContext {

    private final FastMap<Object,Object> properties = new FastMap<Object, Object>();

    private Level level; // Null to use configurable LEVEL.

    @Override
    protected LogContext inner() {
        LogContextImpl ctx = new LogContextImpl();
        ctx.properties.putAll(properties);
        ctx.level = level;
        return ctx;
    }

    @Override
    public void attach(Object property, Object propertyValue) {
        properties.put(property, propertyValue);
    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    protected void log(Level level, Object... objs) {
        Level thisLevel = (this.level != null) ? this.level : LEVEL.getDefaultValue();
        if (level.compareTo(thisLevel) < 0) return;
        final TextBuilder msg = new TextBuilder();
        properties.entrySet().doWhile(new Predicate<Entry<Object, Object>>() {

            public Boolean apply(Entry<Object, Object> e) {
                msg.append('[');
                msg.append(e.getKey());
                msg.append(": ");
                msg.append(e.getValue());
                msg.append("] ");
                return true;
            }

        });
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
        } else { // Default.
            synchronized (this) {
                System.out.print(level);
                System.out.print(' ');
                System.out.print(msg);
                if (error != null) {
                   error.printStackTrace(System.out); 
                } else {
                    System.out.println();
                }
            }
        }
    }

    private static final int[] TO_OSGI_LEVEL = {LogService.LOG_DEBUG, LogService.LOG_INFO,
        LogService.LOG_WARNING, LogService.LOG_ERROR};

}
