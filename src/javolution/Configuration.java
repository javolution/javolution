/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.util.Reflection;

/**
 *  This class centralizes <i><b>J</b>avolution</i> configuration parameters.
 *  Applications may change the default values by providing their own 
 *  <code>javolution.Configuration</code> class to be loaded in place
 *  of this one (first in classpath) or by modifying this class directly.
 *  
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.2, January 8, 2004
 */
public final class Configuration {

    /**
     * Default constructor. 
     */
    private Configuration() {
    }

    /**
     * Returns the maximum number of concurrent thread.
     * 
     * @return <code>(Number of Processors) - 1</code>
     * @see javolution.realtime.ConcurrentThread
     */
    public static int concurrency() {
        Reflection.Method availableProcessors = Reflection.getMethod(
            "java.lang.Runtime.availableProcessors()");
        if (availableProcessors != null) {
            Integer processors = 
                (Integer) availableProcessors.invoke(Runtime.getRuntime());
            return processors.intValue() - 1;
        } else {
            return 0;
        }
    }

    /**
     * Returns the maximum number of object factories.
     * 
     * @return <code>1024</code>
     * @see javolution.realtime.ObjectFactory
     */
    public static int factories() {
        return 1024;
    }

    /**
     * Returns the maximum number of local context variables.
     * 
     * @return <code>1024</code>
     * @see javolution.realtime.LocalContext.Variable
     */
    public static int variables() {
        return 1024;
    }

    /**
     * Indicates if the system hash code is well distributed
     * (default hashcodes for <code>Object</code> instances).
     * 
     * @return <code>true</code> if the default system hashcode is not evenly
     *         distributed; <code>false</code> otherwise (small test 
     *         performed at start-up).
     */
    public static boolean isPoorSystemHash() {
        return IS_POOR_SYSTEM_HASH;
    }
    private static final boolean IS_POOR_SYSTEM_HASH;
    static {
        boolean[] dist = new boolean[32]; // Length power of 2.
        for (int i=0; i < dist.length; i++) {
            dist[new Object().hashCode() & (dist.length - 1)] = true;
        }
        int holes = 0;
        for (int i=0; i < dist.length; i++) {
            if (!dist[i]) holes++; // Count holes.
        }
        IS_POOR_SYSTEM_HASH = holes > (dist.length >> 1);
    }    

}