/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Holds the default implementation of LogService to be used when running 
 * outside OSGi.
 */
public final class LogServiceImpl implements LogService {

    @Override
    public void log(int level, String message) {
        log(level, message, null);
    }

    @Override
    public void log(int level, String message, Throwable exception) {
        synchronized (this) { // One message at a time.
            switch (level) {
                case LogService.LOG_DEBUG:
                    System.out.print("[DEBUG] ");
                    break;
                case LogService.LOG_INFO:
                    System.out.print("[INFO] ");
                    break;
                case LogService.LOG_WARNING:
                    System.out.print("[WARNING] ");
                    break;
                case LogService.LOG_ERROR:
                    System.out.print("[ERROR] ");
                    break;
                default:
                    System.out.print("[UNKNOWN] ");
                    break;
            }
            System.out.println(message);
            if (exception != null) {
                exception.printStackTrace(System.out);
            } 
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void log(ServiceReference sr, int level, String message) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void log(ServiceReference sr, int level, String message,
            Throwable exception) {
        throw new UnsupportedOperationException();
    }

}
