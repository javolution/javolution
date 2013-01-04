/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Vector;
import javolution.context.LogContext;

/**
 * <p> An initializer for all classes loaded by any custom class loader.</p>
 * 
 * <p> Initialization of loaded classes at startup (or during bundle activation)
 *     ensures that not only all the static members are allocated on the stack 
 *     (see {@link javolution.annotation.StackSafe StackSafe} annotation)
 *     but also that the run-time behavior is more time deterministic 
 *     (aka real-time).</p>
 * 
 * <p> Javolution automatically initializes all its loaded classes during OSGi
 *     bundle activation. The following code illustrates how this can be done 
 *     for any bundle.
 *     [code]
 *     public class MyActivator implements BundleActivator {
 *         public void start(BundleContext bc) throws Exception {
 *             // Initialize all classes during bundle activation.
 *             Initializer.initializeLoadedClasses(MyActivator.class.getClassLoader());
 *             ...
 *         }
 *         ...
 *     }
 *     [/code]</p>
 * 
 * <p> This utility use reflection to find the classes loaded and may not be
 *     supported on all platforms.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 26, 2007
 */
public final class Initializer {

    /**
     * Returns the classes loaded by the specified class loader or 
     * <code>null</code> if not supported by the platform.
     */
    public static Class<?>[] loadedClasses(ClassLoader byClassLoader) {
        Class cls = byClassLoader.getClass();
        while (cls != java.lang.ClassLoader.class) {
            cls = cls.getSuperclass();
        }
        try {
            java.lang.reflect.Field fldClasses = cls.getDeclaredField("classes");
            fldClasses.setAccessible(true);
            Vector<Class<?>> list = (Vector) fldClasses.get(byClassLoader);
            Class<?>[] classes = new Class<?>[list.size()];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = list.get(i);
            }
            return classes;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Initializes all the classes loaded by the specified class loader.
     */
    public static void initializeLoadedClasses(ClassLoader byClassLoader) {
        Class<?>[] classes = Initializer.loadedClasses(byClassLoader);
        if (classes == null) {
            LogContext.warning(
                    "Initializations of loaded classes not supported by the platform.");
        }
        LogContext.info("Initialization of ", classes.length, " loaded by ", byClassLoader);
        for (Class<?> cls : classes) {
            try {
                Class.forName(cls.getName(), true, byClassLoader);
            } catch (ClassNotFoundException ex) {
                LogContext.error(ex); // Should never happen.
            }
        }
    }


}