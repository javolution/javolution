/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.util.Vector;
import javolution.context.LogContext;

/**
 * <p> An initializer for all classes loaded by any specified class loader.</p>
 * 
 * <p> Initialization of loaded classes at startup (or during bundle activation)
 *     ensures that the run-time behavior is more time deterministic 
 *     (aka real-time).</p> 
 * 
 * <p> Class loading can be performed in a lazy manner and therefore some parts 
 *     of the class loading process may be done on first use rather than at 
 *     load time. Javolution bundle activator ensure that <b>all</b> its classes
 *     are initialized at start up.
 *     The following code illustrates how this can be done for any bundle.
 * [code]
 * public class MyActivator implements BundleActivator {
 *     public void start(BundleContext bc) throws Exception {
 *         Initializer initializer = new Initializer(MyActivator.class.getClassLoader());
 *         initializer.loadClass(com.foo.internal.UnreferencedClass.class);
 *             // Load explicitly classes not directly or indirectly referenced.
 *         ... 
 *         initializer.initializeLoadedClasses(); // Recursive loading/initialization.
 *         ... // Continue activation
 *     }
 * }[/code]</p>
 * 
 * <p> This utility use reflection to find the classes loaded and may not be
 *     supported on all platforms.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 26, 2007
 */
public class Initializer {

    /**
     * Indicates if the class being initialized should be logged as debug
     * messages (default {@code false}). 
     */
    public static final Configurable<Boolean> DEBUG = new Configurable<Boolean>(
            false) {
        @Override
        protected Boolean parse(String str) {
            return Boolean.parseBoolean(str);
        }
    };

    /** The class loader for this initializer */
    private final ClassLoader classLoader;

    /** 
     * Creates an initializer for the specified class loader.
     */
    public Initializer(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Returns the classes loaded by the class loader of this initializer or 
     * <code>null</code> if not supported by the platform.
     */
    @SuppressWarnings("unchecked")
    public Class<?>[] loadedClasses() {
        Class<?> cls = classLoader.getClass();
        while (cls != java.lang.ClassLoader.class) {
            cls = cls.getSuperclass();
        }
        try {
            java.lang.reflect.Field fldClasses = cls
                    .getDeclaredField("classes");
            fldClasses.setAccessible(true);
            Vector<Class<?>> list = (Vector<Class<?>>) fldClasses
                    .get(classLoader);
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
     * Loads the specified class (does not perform any initialization).
     * This method is typically used to load unreferenced classes.
     */
    public void loadClass(Class<?> cls) {
        try {
            classLoader.loadClass(cls.getName());
        } catch (ClassNotFoundException e) {
            LogContext.error("Class " + cls + " not found.");
        }
    }

    /**
     * Initializes all the loaded classes. If the initialization leads to more 
     * classes being loaded, these classes are initialized as well 
     * (recursive process).
     * 
     * @return {@code true} if initialization has been performed successfully;
     *         {@code false} otherwise.
     */
    public boolean initializeLoadedClasses() {
        boolean isInitializationSuccessful = true;
        int nbrClassesInitialized = 0;
        while (true) {
            Class<?>[] classes = loadedClasses();
            if (classes == null) {
                LogContext.warning("Automatic class initialization not supported.");
                return false;
            }
            if (nbrClassesInitialized >= classes.length)
                break; // Done.
            for (int i = nbrClassesInitialized; i < classes.length; i++) {
                Class<?> cls = classes[i];
                try {
                    if (DEBUG.get())
                        LogContext.debug("Initialize ", cls.getName());
                    Class.forName(cls.getName(), true, classLoader);
                } catch (ClassNotFoundException ex) {
                    isInitializationSuccessful = false;
                    LogContext.error(ex); // Should never happen.
                }
            }
            nbrClassesInitialized = classes.length;
        }
        LogContext.info("Initialization of ", nbrClassesInitialized,
                " classes loaded by ", classLoader);
        return isInitializationSuccessful;
    }

}