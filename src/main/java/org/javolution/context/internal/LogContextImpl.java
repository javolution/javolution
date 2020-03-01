/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.context.internal;

import org.javolution.context.LogContext;
import org.javolution.text.TextBuilder;

import static org.javolution.context.LogContext.Level.DEBUG;

/**
 * Default implementation of LogContext.
 */
public final class LogContextImpl extends LogContext {

    private static final LoggingThread LOGGING_THREAD = new LoggingThread();        
    private Level actualLevel = DEBUG;
    private String actualPrefix = "";
    private String actualSuffix = "";

    public LogContextImpl() {
        if (!LOGGING_THREAD.isAlive()) LOGGING_THREAD.start();        
    }
    
    @Override
    public Level level() {
        return actualLevel;
    }

    @Override
    public String prefix() {
        return actualPrefix;
    }

    @Override
    public String suffix() {
        return actualSuffix;
    }

    @Override
    public void setLevel(Level level) {
        LogContext outer = getOuter(LogContext.class);
        Level currentLevel = (outer != null) ? outer.level() : DEFAULT_LEVEL.get();
        actualLevel = (currentLevel.compareTo(level) > 0) ? currentLevel : level;
    }

    @Override
    public void setPrefix(Object... prefixes) {
        TextBuilder tb = new TextBuilder();
        LogContext outer = getOuter(LogContext.class);
        if (outer != null) tb.append(outer.prefix());
        for (Object obj : prefixes)
            tb.append(obj);
        actualPrefix = tb.toString();
    }

    @Override
    public void setSuffix(Object... suffixes) {
        TextBuilder tb = new TextBuilder();
        for (Object obj : suffixes)
            tb.append(obj);
        LogContext outer = getOuter(LogContext.class);
        if (outer != null)
            tb.append(outer.suffix());
        actualSuffix = tb.toString();
    }

    public void put(String key, Object value) {
        // TODO
    }

    protected void log(Level level, Throwable error, Object... messages) {
        if (level.compareTo(actualLevel) < 0)
            return;
        LOGGING_THREAD.queueEvent(level, actualPrefix, actualSuffix, messages, error);
    }
    
    @Override
    protected LogContext inner() {
        LogContextImpl ctx = new LogContextImpl();
        ctx.actualLevel = level();
        ctx.actualPrefix = prefix();
        ctx.actualSuffix = suffix();
        return ctx;
    }

}