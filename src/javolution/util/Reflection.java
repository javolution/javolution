/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.Javolution;

import java.lang.reflect.InvocationTargetException;

/**
 * <p> This utility class greatly facilitates the use of reflection to invoke 
 *     constructors or methods which may or may not exist at runtime.</p>
 * 
 * <p> The constructors/methods are identified through their signatures
 *     represented as a {@link String}. When the constructor/method does
 *     not exist (e.g. class not found) or when the platform does not support
 *     reflection, the constructor/method is <code>null</code> 
 *     (no exception raised). Here is an example of timer taking advantage 
 *     of the new (JRE1.5+) high resolution time when available:<pre>
 *     public static long microTime() {
 *         if (NANO_TIME_METHOD != null) { // JRE 1.5+
 *             Long time = (Long) NANO_TIME_METHOD.invoke(null); // Static method.
 *             return time.longValue() / 1000;
 *         } else { // Use the less accurate time in milliseconds.
 *             return System.currentTimeMillis() * 1000;
 *         }
 *     }
 *     private static final Reflection.Method NANO_TIME_METHOD 
 *         = Reflection.getMethod(<b>"java.lang.System.nanoTime()"</b>);</pre></p>
 *   
 * <p> Arrays and primitive types are supported. For example:<pre>
 *     Reflection.Constructor sbc = Reflection.getConstructor(<b>"java.lang.StringBuilder(int)"</b>);
 *     if (sbc != null) { // JDK 1.5+
 *        Object sb = sbc.newInstance(new Integer(32));
 *        Reflection.Method append = Reflection.getMethod(<b>"java.lang.StringBuilder.append(char[], int, int)"</b>);
 *        append.invoke(sb, new char[] { 'h', 'i' }, new Integer(0), new Integer(2));
 *        System.out.println(sb);
 *    }
 * 
 *    > hi</pre></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class Reflection {

    /**
     * Default constructor (private to forbid instantiation).
     */
    private Reflection() {
    }

    /**
     * Returns and initializes the class having the specified name. 
     * This method searches the <code>Reflection.class</code> class loader 
     * first (which might be the bootstrap class loader), then the context
     * class loader and finally the system class loader.
     * 
     * @param name the name of the class to search for. 
     * @return the corresponding class.
     * @throws ClassNotFoundException
     * @see java.lang.Thread#getContextClassLoader()
     * @see java.lang.ClassLoader#getSystemClassLoader()
     */
    public static Class getClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name); // Try Reflection.class class loader.
        } catch (ClassNotFoundException e0) { // Try context class loader.
            if ((GET_CONTEXT_CLASS_LOADER != null) && (FOR_NAME != null)) {
                Object contextClassLoader = GET_CONTEXT_CLASS_LOADER
                        .invoke(Thread.currentThread());
                try {
                    return (Class) FOR_NAME.invoke(null, name,
                            new Boolean(true), contextClassLoader);
                } catch (Throwable e1) { // Try system class loader.
                    if ((GET_SYSTEM_CLASS_LOADER != null) && (FOR_NAME != null)) {
                        Object systemClassLoader = GET_SYSTEM_CLASS_LOADER
                                .invoke(null);
                        try {
                            return (Class) FOR_NAME.invoke(null, name,
                                    new Boolean(true), systemClassLoader);
                        } catch (Throwable e2) {
                            throw new ClassNotFoundException("Class " + name
                                    + " cannot be loaded");
                        }
                    }
                }
            }
        }
        throw new ClassNotFoundException("Class " + name + " cannot be loaded");

    }

    private static final Method GET_CONTEXT_CLASS_LOADER = getMethod("java.lang.Thread.getContextClassLoader()");

    private static final Method GET_SYSTEM_CLASS_LOADER = getMethod("java.lang.ClassLoader.getSystemClassLoader()");

    private static final Method FOR_NAME = getMethod("java.lang.Class.forName(java.lang.String, boolean, java.lang.ClassLoader)");

    /**
     * Returns the constructor having the specified signature. 
     * 
     * @param signature the textual representation of the constructor signature. 
     * @return the corresponding constructor or <code>null</code> if none 
     *         found. 
     */
    public static Constructor getConstructor(String signature) {
        int argStart = signature.indexOf('(') + 1;
        if (argStart < 0) {
            throw new IllegalArgumentException("Parenthesis '(' not found");
        }
        int argEnd = signature.indexOf(')');
        if (argEnd < 0) {
            throw new IllegalArgumentException("Parenthesis ')' not found");
        }
        String className = signature.substring(0, argStart - 1);
        Class theClass = simpleClassFor(className);
        if (theClass == null) {
            return null;
        }
        String args = signature.substring(argStart, argEnd);
        Class[] argsTypes = classesFor(args);
        if (argsTypes == null) {
            return null;
        }
        if (argsTypes.length == 0) {
            return new DefaultConstructor(theClass);
        } else {
            try {
                return new ReflectConstructor(ReflectionProxy.getConstructor(
                        theClass, argsTypes), signature);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    private static class DefaultConstructor extends Constructor {
        final Class _class;

        DefaultConstructor(Class cl) {
            _class = cl;
        }

        public Object allocate(Object[] args) {
            try {
                return _class.newInstance();
            } catch (InstantiationException e) {
                throw new Javolution.InternalError("Instantiation error for "
                        + _class.getName() + " default constructor", e);
            } catch (IllegalAccessException e) {
                throw new Javolution.InternalError("Illegal access error for "
                        + _class.getName() + " constructor", e);
            }
        }

        public String toString() {
            return _class + " default constructor";
        }
    }

    private static final class ReflectConstructor extends Constructor {
        private final java.lang.reflect.Constructor _value;

        private final String _signature;

        public ReflectConstructor(java.lang.reflect.Constructor value,
                String signature) {
            _value = value;
            _signature = signature;
        }

        public Object allocate(Object[] args) {
            try {
                return _value.newInstance(args);
            } catch (IllegalArgumentException e) {
                throw new Javolution.InternalError("Illegal argument for "
                        + _signature + " constructor", e);
            } catch (InstantiationException e) {
                throw new Javolution.InternalError("Instantiation error for "
                        + _signature + " constructor", e);
            } catch (IllegalAccessException e) {
                throw new Javolution.InternalError("Illegal access error for "
                        + _signature + " constructor", e);
            } catch (InvocationTargetException e) {
                throw new Javolution.InternalError("Invocation exception  for "
                        + _signature + " constructor",
                        (InvocationTargetException) e.getTargetException());
            }
        }

        public String toString() {
            return _signature + " constructor";
        }
    }

    /**
     * Returns the method having the specified signature.
     * 
     * @param signature the textual representation of the method signature. 
     * @return the corresponding constructor or <code>null</code> if none 
     *         found. 
     */
    public static Method getMethod(String signature) {
        int argStart = signature.indexOf('(') + 1;
        if (argStart < 0) {
            throw new IllegalArgumentException("Parenthesis '(' not found");
        }
        int argEnd = signature.indexOf(')');
        if (argEnd < 0) {
            throw new IllegalArgumentException("Parenthesis ')' not found");
        }
        int nameStart = signature.substring(0, argStart).lastIndexOf('.') + 1;

        String className = signature.substring(0, nameStart - 1);
        Class theClass = simpleClassFor(className);
        if (theClass == null) {
            return null;
        }
        String methodName = signature.substring(nameStart, argStart - 1);
        String args = signature.substring(argStart, argEnd);
        Class[] argsTypes = classesFor(args);
        if (argsTypes == null) {
            return null;
        }
        try {
            // This code has to be compiled using JDK1.1+
            return new ReflectMethod(ReflectionProxy.getMethod(theClass,
                    methodName, argsTypes), signature);
        } catch (Throwable t) {
            return null;
        }
    }

    private static final class ReflectMethod extends Method {
        private final java.lang.reflect.Method _value;

        private final String _signature;

        public ReflectMethod(java.lang.reflect.Method value, String signature) {
            _value = value;
            _signature = signature;
        }

        public Object execute(Object that, Object[] args) {
            try {
                return _value.invoke(that, args);
            } catch (IllegalArgumentException e) {
                throw new Javolution.InternalError("Illegal argument for "
                        + _signature + " method", e);
            } catch (IllegalAccessException e) {
                throw new Javolution.InternalError("Illegal access error for "
                        + _signature + " method", e);
            } catch (InvocationTargetException e) {
                throw new Javolution.InternalError("Invocation exception for "
                        + _signature + " method", (InvocationTargetException) e
                        .getTargetException());
            }
        }

        public String toString() {
            return _signature + " method";
        }
    }

    /**
     * Returns the classes for the specified argument.
     * 
     * @param args the comma separated arguments.
     * @return the classes or <code>null</code> if one of the class is not found.
     */
    private static Class[] classesFor(String args) {
        try {
            args = args.trim();
            if (args.length() == 0) {
                return new Class[0];
            }
            // Count semicolons  occurences.
            int semiColons = 0;
            for (int i = 0; i < args.length(); i++) {
                if (args.charAt(i) == ',') {
                    semiColons++;
                }
            }
            Class[] classes = new Class[semiColons + 1];

            int index = 0;
            for (int i = 0; i < semiColons; i++) {
                int sep = args.indexOf(',', index);
                classes[i] = classFor(args.substring(index, sep).trim());
                index = sep + 1;
            }
            classes[semiColons] = classFor(args.substring(index).trim());
            return classes;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private static Class classFor(String className)
            throws ClassNotFoundException {
        int arrayIndex = className.indexOf("[]");
        if (arrayIndex >= 0) {
            if (className.indexOf("[][]") >= 0) {
                if (className.indexOf("[][][]") >= 0) {
                    if (className.indexOf("[][][][]") >= 0) {
                        throw new UnsupportedOperationException(
                                "The maximum array dimension is 3");
                    } else { // Dimension three.
                        return getClass("[[["
                                + descriptorFor(className.substring(0,
                                        arrayIndex)));
                    }
                } else { // Dimension two.
                    return getClass("[["
                            + descriptorFor(className.substring(0, arrayIndex)));
                }
            } else { // Dimension one.
                return getClass("["
                        + descriptorFor(className.substring(0, arrayIndex)));
            }
        }
        if (className.equals("boolean")) {
            return boolean.class;
        } else if (className.equals("byte")) {
            return byte.class;
        } else if (className.equals("char")) {
            return char.class;
        } else if (className.equals("short")) {
            return short.class;
        } else if (className.equals("int")) {
            return int.class;
        } else if (className.equals("long")) {
            return long.class;
        } else if (className.equals("float")) {
            return float.class;
        } else if (className.equals("double")) {
            return double.class;
        } else {
            return Class.forName(className);
        }
    }

    private static String descriptorFor(String className) {
        if (className.equals("boolean")) {
            return "Z";
        } else if (className.equals("byte")) {
            return "B";
        } else if (className.equals("char")) {
            return "C";
        } else if (className.equals("short")) {
            return "S";
        } else if (className.equals("int")) {
            return "I";
        } else if (className.equals("long")) {
            return "J";
        } else if (className.equals("float")) {
            return "F";
        } else if (className.equals("double")) {
            return "D";
        } else {
            return "L" + className.replace('.', '/') + ";";
        }
    }

    /**
     * Returns the class with the specified name.
     * 
     * @param className the full path name of the class.
     * @return the corresponding class or <code>null</code> not found.
     */
    private static Class simpleClassFor(String className) {
        try {
            return getClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * This class represents a run-time constructor obtained through reflection.
     * 
     * Here are few examples of utilization:
     * <pre>
     * // Default constructor (<code>fastList = new FastList()</code>)
     * Reflection.Constructor fastListConstructor 
     *     = Reflection.getConstructor("javolution.util.FastList()");
     * Object fastList = fastListConstructor.newInstance();
     * 
     * // Constructor with arguments (<code>fastMap = new FastMap(64)</code>)
     * Reflection.Constructor fastMapConstructor 
     *     = Reflection.getConstructor("javolution.util.FastMap(int)");
     * Object fastMap = fastMapConstructor.newInstance(new Integer(64));
     * </pre>
     */
    public static abstract class Constructor {

        /**
         * Default constructor.
         */
        public Constructor() {
        }

        /**
         * Allocates a new object using this constructor with the specified
         * arguments.
         * 
         * @param args the constructor arguments. 
         * @return the object being instantiated. 
         */
        public abstract Object allocate(Object[] args);

        /**
         * Invokes this constructor with no argument (convenience method).
         * 
         * @return the object being instantiated. 
         */
        public final Object newInstance() {
            return allocate(NO_ARG);
        }

        private static final Object[] NO_ARG = new Object[0];

        /**
         * Invokes this constructor with the specified single argument.
         * 
         * @param arg0 the first argument. 
         * @return the object being instantiated. 
         */
        public final Object newInstance(Object arg0) {
            synchronized (this) {
                array1[0] = arg0;
                return allocate(array1);
            }
        }

        private final Object[] array1 = new Object[1];

        /**
         * Invokes this constructor with the specified two arguments.
         * 
         * @param arg0 the first argument. 
         * @param arg1 the second argument. 
         * @return the object being instantiated. 
         */
        public final Object newInstance(Object arg0, Object arg1) {
            synchronized (this) {
                array2[0] = arg0;
                array2[1] = arg1;
                return allocate(array2);
            }
        }

        private final Object[] array2 = new Object[2];

        /**
         * Invokes this constructor with the specified three arguments.
         * 
         * @param arg0 the first argument. 
         * @param arg1 the second argument. 
         * @param arg2 the third argument. 
         * @return the object being instantiated. 
         */
        public final Object newInstance(Object arg0, Object arg1, Object arg2) {
            synchronized (this) {
                array3[0] = arg0;
                array3[1] = arg1;
                array3[2] = arg2;
                return allocate(array3);
            }
        }

        private final Object[] array3 = new Object[3];

        /**
         * Invokes this constructor with the specified four arguments.
         * 
         * @param arg0 the first argument. 
         * @param arg1 the second argument. 
         * @param arg2 the third argument. 
         * @param arg3 the fourth argument. 
         * @return the object being instantiated. 
         */
        public final Object newInstance(Object arg0, Object arg1, Object arg2,
                Object arg3) {
            synchronized (this) {
                array4[0] = arg0;
                array4[1] = arg1;
                array4[2] = arg2;
                array4[3] = arg3;
                return allocate(array4);
            }
        }

        private final Object[] array4 = new Object[4];
    }

    /**
     * This class represents a run-time method obtained through reflection.
     * 
     * Here are few examples of utilization:
     * <pre>
     * // Non-static method: fastMap.put(myKey, myValue)
     * Reflection.Method putKeyValue  
     *     = Reflection.getMethod(
     *         "javolution.util.FastMap.put(java.lang.Object, java.lang.Object)");
     * Object previous = putKeyValue.invoke(fastMap, myKey, myValue); 
     * 
     * // Static method: System.nanoTime()  (JRE1.5+) 
     * Reflection.Method nanoTime 
     *     = Reflection.getMethod("java.lang.System.nanoTime()");
     * long time = ((Long)nanoTime.invoke(null)).longValue();
     * </pre>
     */
    public static abstract class Method {

        /**
         * Default constructor.
         */
        public Method() {
        }

        /**
         * Executes this method with the specified arguments.
         * 
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @param args the method arguments. 
         * @return the result of the execution. 
         */
        public abstract Object execute(Object thisObject, Object[] args);

        /**
         * Invokes this method on the specified object which might be 
         * <code>null</code> if the method is static (convenience method).
         * 
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @return the result of the invocation.
         */
        public final Object invoke(Object thisObject) {
            return execute(thisObject, NO_ARG);
        }

        private static final Object[] NO_ARG = new Object[0];

        /**
         * Invokes this method with the specified single argument
         * on the specified object which might be <code>null</code>
         * if the method is static (convenience method).
         * 
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @param arg0 the single argument. 
         * @return the result of the invocation.
         */
        public final Object invoke(Object thisObject, Object arg0) {
            synchronized (this) {
                array1[0] = arg0;
                return execute(thisObject, array1);
            }
        }

        private final Object[] array1 = new Object[1];

        /**
         * Invokes this method with the specified two arguments
         * on the specified object which might be <code>null</code>
         * if the method is static (convenience method).
         * 
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @param arg0 the first argument. 
         * @param arg1 the second argument. 
         * @return the result of the invocation.
         * @throws RuntimeException wrapping any exception raised during 
         *         invocation (see <code>Throwable.getCause()</code>). 
         */
        public final Object invoke(Object thisObject, Object arg0, Object arg1) {
            synchronized (this) {
                array2[0] = arg0;
                array2[1] = arg1;
                return execute(thisObject, array2);
            }
        }

        private final Object[] array2 = new Object[2];

        /**
         * Invokes this method with the specified three arguments
         * on the specified object which might be <code>null</code>
         * if the method is static.
         * 
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @param arg0 the first argument (convenience method). 
         * @param arg1 the second argument. 
         * @param arg2 the third argument. 
         * @return the result of the invocation.
         */
        public final Object invoke(Object thisObject, Object arg0, Object arg1,
                Object arg2) {
            synchronized (this) {
                array3[0] = arg0;
                array3[1] = arg1;
                array3[2] = arg2;
                return execute(thisObject, array3);
            }
        }

        private final Object[] array3 = new Object[3];

        /**
         * Invokes this method with the specified four arguments
         * on the specified object which might be <code>null</code>
         * if the method is static (convenience method).
         * 
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @param arg0 the first argument. 
         * @param arg1 the second argument. 
         * @param arg2 the third argument. 
         * @param arg3 the fourth argument. 
         * @return the result of the invocation.
         */
        public final Object invoke(Object thisObject, Object arg0, Object arg1,
                Object arg2, Object arg3) {
            synchronized (this) {
                array4[0] = arg0;
                array4[1] = arg1;
                array4[2] = arg2;
                array4[3] = arg3;
                return execute(thisObject, array4);
            }
        }

        private final Object[] array4 = new Object[4];
    }

}