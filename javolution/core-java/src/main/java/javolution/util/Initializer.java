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
 *     ensures that not only all the static members are allocated on the heap 
 *     (see {@link javolution.annotation.StackSafe StackSafe} annotation)
 *     but also that the run-time behavior is more time deterministic 
 *     (aka real-time).</p> 
 * 
 * <p> Class loading can be performed in a lazy manner and therefore some parts 
 *     of the class loading process may be done on first use rather than at 
 *     load time. Javolution activator loads unreferenced classes to ensure that
 *     <b>all</b> classes can be initialized during bundle activation.
 *     The following code illustrates how this can be done for any bundle.
 *     [code]
 *     public class MyActivator implements BundleActivator {
 *         public void start(BundleContext bc) throws Exception {
 *             initializeAll();
 *             ...
 *         }
 *         public static boolean initializeAll()  {
 *             Initializer initializer = new Initializer(MyActivator.class.getClassLoader());
 *             // Loads classes not yet referenced (directly or indirectly).
 *             initializer.loadClass("com.foo.internal.UnreferencedClass");
 *             ... 
 *             initializer.initializeLoadedClasses(); // Recursive loading/initialization.
 *             return initializer.isInitializationSuccessful();
 *         }
 *     }
 *     [/code]</p>
 * 
 * <p> This utility use reflection to find the classes loaded and may not be
 *     supported on all platforms.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 26, 2007
 */
public class Initializer {

    /** The class loader for this initializer */
    private final ClassLoader classLoader;
    
    /** Indicates if the initialization has been performed successfully. */
    private boolean isInitializationSuccessful = false;
    
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
            java.lang.reflect.Field fldClasses = cls.getDeclaredField("classes");
            fldClasses.setAccessible(true);
            Vector<Class<?>> list = (Vector<Class<?>>) fldClasses.get(classLoader);
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
    public void loadClass(String className) {
        try {
            classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            LogContext.error("Class " + className + " not found.");
        }
    }

    /**
     * Initializes all the loaded classes. If the initialization leads to more 
     * classes being loaded, these classes are initialized as well 
     * (recursive process).
     */
    public void initializeLoadedClasses() {
        int nbrClassesInitialized = 0;
        while (true) {
            Class<?>[] classes = loadedClasses();
            if (classes == null) {
                LogContext.warning("Automatic class initialization not supported.");
                return;
            }
            if (nbrClassesInitialized >= classes.length) break; // Done.
            for (int i = nbrClassesInitialized; i < classes.length; i++) {
                Class<?> cls = classes[i];
                try {
                    Class.forName(cls.getName(), true, classLoader);
                } catch (ClassNotFoundException ex) {
                    LogContext.error(ex); // Should never happen.
                }
            }
            nbrClassesInitialized = classes.length;
        }
        LogContext.info("Initialization of ", nbrClassesInitialized, " classes loaded by ", classLoader);
    }

    /**
     * Indicates whether or not initialization has been successful.
     */
    public boolean isInitializationSuccessful() {
        return isInitializationSuccessful;
    }

}