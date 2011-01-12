/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.java.util.logging;


public class Logger {

    private static final Logger INSTANCE = new Logger();
    
    public static Logger getLogger(String name) {
        return INSTANCE; 
    }

    private Logger() {        
    }
    
    public boolean isLoggable(Level level) {
        return (level == Level.SEVERE) || (level == Level.WARNING);
    }

    public void log(LogRecord record) {
    }

    public void severe(String msg) {
        System.out.println("[error] " + msg);               
    }

    public void warning(String msg) {
        System.out.println("[warning] " + msg);
    }

    public void info(String msg) {
        System.out.println("[info] " + msg);
    }

    public void config(String msg) {
        System.out.println("[config] " + msg);
    }

    public void fine(String msg) {
        System.out.println("[fine] " + msg);
    }

    public void finer(String msg) {
        System.out.println("[finer] " + msg);
    }

    public void finest(String msg) {
        System.out.println("[finest] " + msg);
    }

    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        System.out.println("[throwing] " + sourceClass + "." + sourceMethod + " throws " + thrown);
    }

    public void entering(String sourceClass, String sourceMethod) {
        System.out.println("[entering] " + sourceClass + "." + sourceMethod);
    }
    
    public void exiting(String sourceClass, String sourceMethod) {
        System.out.println("[exiting] " + sourceClass + "." + sourceMethod);
    }
    
    public void log(Level level,
            String msg) {        
        System.out.println("[log] " + msg);
    }
    
    public void log(Level level,
            String msg,
            Throwable thrown) {
        System.out.println("[log] Exception: " + thrown + ", " + msg);
    }
}
