/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.lang;

import _templates.java.lang.CharSequence;
import _templates.java.util.Collection;
import _templates.java.util.Iterator;
import _templates.javolution.context.LogContext;
import _templates.javolution.text.TextBuilder;
import _templates.javolution.util.FastComparator;
import _templates.javolution.util.FastMap;
import _templates.javolution.util.FastSet;
import _templates.javolution.util.FastTable;

/**
 * <p> This utility class greatly facilitates the use of reflection to invoke 
 *     constructors or methods which may or may not exist at runtime or
 *     may be loaded/unloaded dynamically such as when running on a
 *     <a href="http://www.osgi.org/">OSGi Platform</a>. For example:[code]
 *         public class Activator implements BundleActivator {
 *              public void start(BundleContext context) throws Exception {
 *                   Reflection.getInstance().add(Activator.class.getClassLoader());
 *                   ...
 *              }
 *              public void stop(BundleContext context) throws Exception {
 *                   Reflection.getInstance().remove(Activator.class.getClassLoader());
 *                   ...
 *              }
 *         }[/code]
 *
 * <p> The constructors/methods are identified through their signatures
 *     represented as a {@link String}. When the constructor/method does
 *     not exist (e.g. class not found) or when the platform does not support
 *     reflection, the constructor/method is <code>null</code> 
 *     (no exception raised). Here is an example of timer taking advantage 
 *     of the new (JRE1.5+) high resolution time when available:[code]
 *     public static long microTime() {
 *         if (NANO_TIME_METHOD != null) { // JRE 1.5+
 *             Long time = (Long) NANO_TIME_METHOD.invoke(null); // Static method.
 *             return time.longValue() / 1000;
 *         } else { // Use the less accurate time in milliseconds.
 *             return System.currentTimeMillis() * 1000;
 *         }
 *     }
 *     private static final Reflection.Method NANO_TIME_METHOD 
 *         = Reflection.getInstance().getMethod("java.lang.System.nanoTime()");[/code]</p>
 *   
 * <p> Arrays and primitive types are supported. For example:[code]
 *     Reflection.Constructor sbc = Reflection.getInstance().getConstructor("java.lang.StringBuilder(int)");
 *     if (sbc != null) { // JDK 1.5+
 *        Object sb = sbc.newInstance(new Integer(32));
 *        Reflection.Method append = Reflection.getInstance().getMethod("java.lang.StringBuilder.append(char[], int, int)");
 *        append.invoke(sb, new char[] { 'h', 'i' }, new Integer(0), new Integer(2));
 *        System.out.println(sb);
 *    }
 * 
 *    > hi[/code]</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.5, April 20, 2009
 */
public abstract class Reflection {

    /**
     * Holds the default implementation (configurable).
     */
    private static volatile Reflection INSTANCE = new Default();

    /**
     * Holds the default implementation (configurable).
     */
    public static final Configurable/*<Class<? extends Reflection>>*/ CLASS = new Configurable(Default.class) {

        protected void notifyChange(Object oldValue, Object newValue) {
            try {
                INSTANCE = (Reflection) ((Class) newValue).newInstance();
            } catch (Throwable error) {
                LogContext.error(error);
            }
        }
    };

    /**
     * Default constructor.
     */
    protected Reflection() {
    }

    /**
     * Returns the current reflection instance. The implementation class
     * is defined by {@link #CLASS} (configurable}.
     *
     * @return the reflection instance.
     */
    public static final Reflection getInstance() {
        return INSTANCE;
    }

    /**
     * Adds the specified class loader to the research tree.
     *
     * @param classLoader the class loader being added.
     */
    public abstract void add(Object classLoader);

    /**
     * Removes the specified class loader from the research tree.
     * This method clears any cache data to allow for classes
     * associated to the specified class loader to be garbage collected.
     *
     * @param classLoader the class loader being removed.
     */
    public abstract void remove(Object classLoader);

    /**
     * Returns the class having the specified name. This method searches the
     * class loader of the reflection implementation, then any
     * {@link #add additional} class loaders.
     * If the class is found, it is initialized
     * and returned; otherwise <code>null</code> is returned.
     * The class may be cached for performance reasons.
     *
     * @param name the name of the class to search for.
     * @return the corresponding class or <code>null</code>
     */
    public abstract Class getClass(CharSequence name);

    /**
     * Equivalent to {@link #getClass(CharSequence)} (for J2ME compatibility).
     */
    public Class getClass(String name) {
        Object obj = name;
        if (obj instanceof CharSequence)
            return getClass((CharSequence) obj);
        // String not a CharSequence on J2ME
        TextBuilder tmp = TextBuilder.newInstance();
        try {
            tmp.append(name);
            return getClass(tmp);
        } finally {
            TextBuilder.recycle(tmp);
        }
    }

    /**
     * Returns the parent class of the specified class or interface.
     *
     * @param forClass the class for which the parent class is returned.
     * @return the parent class of the specified class or <code>null</code>
     *         if none (e.g. Object.class or top interface).
     */
    public abstract Class getSuperclass(Class forClass);

    /**
     * Returns the interfaces implemented by the specified class or interface.
     *
     * @param forClass the class for which the interfaces are returned.
     * @return an array holding the interfaces implemented (empty if none).
     */
    public abstract Class[] getInterfaces(Class forClass);

    /**
     * Returns the constructor having the specified signature.
     *
     * @param signature the textual representation of the constructor signature.
     * @return the corresponding constructor or <code>null</code> if none
     *         found.
     */
    public abstract Constructor getConstructor(String signature);

    /**
     * Returns the method having the specified signature.
     *
     * @param signature the textual representation of the method signature.
     * @return the corresponding constructor or <code>null</code> if none
     *         found.
     */
    public abstract Method getMethod(String signature);

    /**
     * Returns the field of specified type which has been attached to a class.
     * If <code>inherited</code> is <code>true</code> the class hierarchy
     * of the given class (parent classes and implementing interfaces) is
     * searched. The method forces the initialization of the specified
     * <code>forClass</code>.
     *
     * @param forClass the base class for which the attached field is searched.
     * @param type the type of field being searched for.
     * @param inherited indicates if the class hierarchy is searched too.
     * @return an attached field of specified type possibly inherited or <code>null</code>
     *         if none found.
     * @see    #setField(java.lang.Object, java.lang.Class, java.lang.Class)
     */
    public abstract /*<T>*/ Object/*{T}*/ getField(Class forClass, Class/*<T>*/ type, boolean inherited);

    /**
     * Attaches a field of specified type to a class (the attached field is
     * dereferenced when the class is unloaded).
     *
     * @param obj the field object being attached.
     * @param forClass the class to which the field is attached.
     * @param type the category type of the field being attached.
     * @throws IllegalArgumentException if a field of specified type is already
     *         attached to the specified class.
     */
    public abstract /*<T>*/ void setField(Object/*{T}*/ obj, Class forClass, Class/*<T>*/ type);

    /**
     * This interface represents a run-time constructor obtained through reflection.
     *
     * Here are few examples of utilization:[code]
     * // Default constructor (fastList = new FastList())
     * Reflection.Constructor fastListConstructor
     *     = Reflection.getInstance().getConstructor("javolution.util.FastList()");
     * Object fastList = fastListConstructor.newInstance();
     *
     * // Constructor with arguments (fastMap = new FastMap(64))
     * Reflection.Constructor fastMapConstructor
     *     = Reflection.getInstance().getConstructor("javolution.util.FastMap(int)");
     * Object fastMap = fastMapConstructor.newInstance(new Integer(64));
     * [/code]
     */
    public interface Constructor {

        /**
         * Returns an array of <code>Class</code> objects that represents
         * the formal parameter types, in declaration order of this constructor.
         *
         * @return the parameter types for this constructor.
         */
        Class[] getParameterTypes();

        /**
         * Invokes this constructor with no argument.
         *
         * @return the object being instantiated.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 0</code>
         */
        Object newInstance();

        /**
         * Invokes this constructor with the specified single argument.
         *
         * @param arg0 the first argument.
         * @return the object being instantiated.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 1</code>
         */
        Object newInstance(Object arg0);

        /**
         * Invokes this constructor with the specified two arguments.
         *
         * @param arg0 the first argument.
         * @param arg1 the second argument.
         * @return the object being instantiated.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 2</code>
         */
        Object newInstance(Object arg0, Object arg1);

        /**
         * Invokes this constructor with the specified three arguments.
         *
         * @param arg0 the first argument.
         * @param arg1 the second argument.
         * @param arg2 the third argument.
         * @return the object being instantiated.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 3</code>
         */
        Object newInstance(Object arg0, Object arg1, Object arg2);
        /**
         * Invokes this constructor with the specified arguments.
         *
         * @param args the arguments.
         * @return the object being instantiated.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != args.length</code>
        @JVM-1.5+@
        Object newInstance(Object... args);
        /**/
    }

    /**
     * This interface represents a run-time method obtained through reflection.
     *
     * Here are few examples of utilization:[code]
     * // Non-static method: fastMap.put(myKey, myValue)
     * Reflection.Method putKeyValue
     *     = Reflection.getInstance().getMethod(
     *         "javolution.util.FastMap.put(java.lang.Object, java.lang.Object)");
     * Object previous = putKeyValue.invoke(fastMap, myKey, myValue);
     *
     * // Static method: System.nanoTime()  (JRE1.5+)
     * Reflection.Method nanoTime
     *     = Reflection.getInstance().getMethod("java.lang.System.nanoTime()");
     * long time = ((Long)nanoTime.invoke(null)).longValue();[/code]
     */
    public interface Method {

        /**
         * Returns an array of <code>Class</code> objects that represents
         * the formal parameter types, in declaration order of this constructor.
         *
         * @return the parameter types for this constructor.
         */
        Class[] getParameterTypes();

        /**
         * Invokes this method on the specified object which might be
         * <code>null</code> if the method is static (convenience method).
         *
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @return the result of the invocation.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 0</code>
         */
        Object invoke(Object thisObject);

        /**
         * Invokes this method with the specified single argument
         * on the specified object which might be <code>null</code>
         * if the method is static (convenience method).
         *
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @param arg0 the single argument.
         * @return the result of the invocation.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 1</code>
         */
        Object invoke(Object thisObject, Object arg0);

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
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 2</code>
         */
        Object invoke(Object thisObject, Object arg0, Object arg1);

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
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != 3</code>
         */
        Object invoke(Object thisObject, Object arg0, Object arg1,
                Object arg2);
        /**
         * Invokes this method with the specified arguments
         * on the specified object which might be <code>null</code>
         * if the method is static.
         *
         * @param thisObject the object upon which this method is invoked
         *        or <code>null</code> for static methods.
         * @param args the arguments.
         * @return the result of the invocation.
         * @throws IllegalArgumentException if
         *          <code>this.getParametersTypes().length != args.length</code>
        @JVM-1.5+@
        Object invoke(Object thisObject, Object... args);
        /**/
    }

//////////////////////////////////
// Holds Default Implementation //
//////////////////////////////////
    private static final class Default extends Reflection {

        private final FastMap _fields = new FastMap().shared();

        private final Collection _classLoaders = new FastSet().shared();

        private final FastMap _nameToClass = new FastMap().shared().setKeyComparator(FastComparator.LEXICAL);

        public void add(Object classLoader) {
            _classLoaders.add(classLoader);
        }

        public void remove(Object classLoader) {
            _classLoaders.remove(classLoader);
            _nameToClass.clear(); // Clear cache.
            for (Iterator i = _fields.entrySet().iterator(); i.hasNext();) {
                FastMap.Entry entry = (FastMap.Entry) i.next();
                Class cls = (Class) entry.getKey();
                if (cls.getClassLoader().equals(classLoader)) {
                    _fields.remove(cls); // Remove class and its fields.
                }
            }
        }

        public Class getClass(CharSequence name) {
            Class cls = (Class) _nameToClass.get(name); // First search cache.
            return (cls != null) ? cls : searchClass(name.toString());
        }

        private Class searchClass(String name) {
            Class cls = null;
            try {
                cls = Class.forName(name);
            } catch (ClassNotFoundException e1) {
                /* @JVM-1.4+@
                for (Iterator i = _classLoaders.iterator(); i.hasNext();) {
                ClassLoader classLoader = (ClassLoader) i.next();
                try {
                cls = Class.forName(name, true, classLoader);
                } catch (ClassNotFoundException e2) {
                // Not found, continue.
                }
                }
                /**/
            }
            if (cls != null) { // Cache the result.
                _nameToClass.put(name, cls);
            }
            return cls;
        }

        public Constructor getConstructor(String signature) {
            int argStart = signature.indexOf('(') + 1;
            if (argStart < 0) {
                throw new IllegalArgumentException("Parenthesis '(' not found");
            }
            int argEnd = signature.indexOf(')');
            if (argEnd < 0) {
                throw new IllegalArgumentException("Parenthesis ')' not found");
            }
            String className = signature.substring(0, argStart - 1);
            Class theClass = getClass(className);
            if (theClass == null) {
                return null;
            }
            String args = signature.substring(argStart, argEnd);
            if (args.length() == 0) {
                return new DefaultConstructor(theClass);
            }
            /*@JVM-1.4+@
            Class[] argsTypes = classesFor(args);
            if (argsTypes == null) return null;
            try {
            return new ReflectConstructor(theClass.getConstructor(argsTypes),
            signature);
            } catch (NoSuchMethodException e) {
            }
            /**/
            LogContext.warning("Reflection not supported (Reflection.getConstructor(String)");
            return null;
        }

        public Class[] getInterfaces(Class cls) {
            /*@JVM-1.4+@
            if (true) return cls.getInterfaces();
            /**/
            LogContext.warning("Reflection not supported (Reflection.getInterfaces(Class)");
            return new Class[0];
        }

        public Class getSuperclass(Class cls) {
            /*@JVM-1.4+@
            if (true) return cls.getSuperclass();
            /**/
            LogContext.warning("Reflection not supported (Reflection.getSuperclass(Class)");
            return null;
        }

        // Implements abstract method.
        public Method getMethod(String signature) {
            /*@JVM-1.4+@
            int argStart = signature.indexOf('(') + 1;
            if (argStart < 0) {
            throw new IllegalArgumentException("Parenthesis '(' not found");
            }
            int argEnd = signature.indexOf(')');
            if (argEnd < 0) {
            throw new IllegalArgumentException("Parenthesis ')' not found");
            }
            int nameStart = signature.substring(0, argStart).lastIndexOf('.') + 1;
            try {

            String className = signature.substring(0, nameStart - 1);
            Class theClass = getClass(className);
            if (theClass == null) return null;
            String methodName = signature.substring(nameStart, argStart - 1);
            String args = signature.substring(argStart, argEnd);
            Class[] argsTypes = classesFor(args);
            if (argsTypes == null) return null;
            return new ReflectMethod(theClass.getMethod(methodName, argsTypes),
            signature);
            } catch (Throwable t) {
            }
            /**/
            LogContext.warning("Reflection not supported (Reflection.getMethod(String)");
            return null;
        }

        public Object getField(Class forClass, Class type, boolean inherited) {
            ClassInitializer.initialize(forClass);
            return getField2(forClass, type, inherited);
        }
        private Object getField2(Class forClass, Class type, boolean inherited) {
            FastMap typeToField = (FastMap) _fields.get(forClass);
            if (typeToField != null) {
                Object field = typeToField.get(type);
                if (field != null)
                    return field;
            }
            if (!inherited)
                return null;

            // Search direct interfaces.
            Class[] interfaces = getInterfaces(forClass);
            for (int i = 0; i < interfaces.length; i++) {
                Object field = getField2(interfaces[i], type, false);
                if (field != null)
                    return field;
            }

            // Recursion with the parent class.
            Class parentClass = getSuperclass(forClass);
            return (parentClass != null) ? getField2(parentClass, type, inherited) : null;
        }

        public void setField(Object obj, Class forClass, Class type) {
            synchronized (forClass) { // We don't want to attach simultaneously to the same class.
                FastMap typeToField = (FastMap) _fields.get(forClass);
                if ((typeToField != null) && typeToField.containsKey(type))
                    throw new IllegalArgumentException("Field of type " + type + " already attached to class " + forClass);
                if (typeToField == null) {
                    typeToField = new FastMap();
                    _fields.put(forClass, typeToField);
                }
                typeToField.put(type, obj);
            }
        }

        ////////////////////////////////
        // Constructor Implementation //
        ////////////////////////////////
        private static abstract class BaseConstructor implements Constructor {

            private final Class[] _parameterTypes;

            protected BaseConstructor(Class[] parameterTypes) {
                _parameterTypes = parameterTypes;
            }

            public Class[] getParameterTypes() {
                return _parameterTypes;
            }

            protected abstract Object allocate(Object[] args);

            public final Object newInstance() {
                if (_parameterTypes.length != 0)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                return allocate(EMPTY_ARRAY);
            }

            public final Object newInstance(Object arg0) {
                if (_parameterTypes.length != 1)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                Object[] args = {arg0};
                return allocate(args);
            }

            public final Object newInstance(Object arg0, Object arg1) {
                if (_parameterTypes.length != 2)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                Object[] args = {arg0, arg1};
                return allocate(args);
            }

            public final Object newInstance(Object arg0, Object arg1, Object arg2) {
                if (_parameterTypes.length != 3)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                Object[] args = {arg0, arg1, arg2};
                return allocate(args);
            }
            /**
            @JVM-1.5+@
            public final Object newInstance(Object... args) {
            if (_parameterTypes.length != args.length)
            throw new IllegalArgumentException(
            "Expected number of parameters is " + _parameterTypes.length);
            return allocate(args);
            }
            /**/
        }

        private static class DefaultConstructor extends BaseConstructor {

            final Class _class;

            DefaultConstructor(Class cl) {
                super(new Class[0]); // No arguments.
                _class = cl;
            }

            public Object allocate(Object[] args) {
                try {
                    return _class.newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException("Default constructor instantiation error for " + _class.getName() + " (" + e.getMessage() + ")");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Default constructor illegal access error for " + _class.getName() + " (" + e.getMessage() + ")");
                }
            }

            public String toString() {
                return _class + " default constructor";
            }
        }

        /*@JVM-1.4+@
        private final class ReflectConstructor extends BaseConstructor {
        private final java.lang.reflect.Constructor _value;

        private final String _signature;

        public ReflectConstructor(java.lang.reflect.Constructor value,
        String signature) {
        super(value.getParameterTypes());
        _value = value;
        _signature = signature;
        }

        public Object allocate(Object[] args) {
        try {
        return _value.newInstance(args);
        } catch (InstantiationException e) {
        throw new RuntimeException("Instantiation error for "
        + _signature + " constructor", e);
        } catch (IllegalAccessException e) {
        throw new RuntimeException("Illegal access error for "
        + _signature + " constructor", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
        if (e.getCause() instanceof RuntimeException)
        throw (RuntimeException)e.getCause();
        throw new RuntimeException("Invocation exception  for "
        + _signature + " constructor",
        (java.lang.reflect.InvocationTargetException) e.getCause());
        }
        }

        public String toString() {
        return _signature + " constructor";
        }
        }
        /**/
        ///////////////////////////
        // Method Implementation //
        ///////////////////////////
        private static abstract class BaseMethod implements Method {

            private final Class[] _parameterTypes;

            protected BaseMethod(Class[] parameterTypes) {
                _parameterTypes = parameterTypes;
            }

            public Class[] getParameterTypes() {
                return _parameterTypes;
            }

            protected abstract Object execute(Object thisObject, Object[] args);

            public final Object invoke(Object thisObject) {
                if (_parameterTypes.length != 0)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                return execute(thisObject, EMPTY_ARRAY);
            }

            public final Object invoke(Object thisObject, Object arg0) {
                if (_parameterTypes.length != 1)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                Object[] args = {arg0};
                return execute(thisObject, args);
            }

            public final Object invoke(Object thisObject, Object arg0, Object arg1) {
                if (_parameterTypes.length != 2)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                Object[] args = {arg0, arg1};
                return execute(thisObject, args);
            }

            public final Object invoke(Object thisObject, Object arg0, Object arg1,
                    Object arg2) {
                if (_parameterTypes.length != 3)
                    throw new IllegalArgumentException(
                            "Expected number of parameters is " + _parameterTypes.length);
                Object[] args = {arg0, arg1, arg2};
                return execute(thisObject, args);
            }
            /**
            @JVM-1.5+@
            public final Object invoke(Object thisObject, Object... args) {
            if (_parameterTypes.length != args.length)
            throw new IllegalArgumentException(
            "Expected number of parameters is " + _parameterTypes.length);
            return execute(thisObject, args);
            }
            /**/
        }

        /*@JVM-1.4+@
        private final class ReflectMethod extends BaseMethod {

        private final java.lang.reflect.Method _value;

        private final String _signature;

        public ReflectMethod(java.lang.reflect.Method value, String signature) {
        super(value.getParameterTypes());
        _value = value;
        _signature = signature;
        }

        public Object execute(Object that, Object[] args) {
        try {
        return _value.invoke(that, args);
        } catch (IllegalAccessException e) {
        throw new IllegalAccessError("Illegal access error for " + _signature + " method");
        } catch (java.lang.reflect.InvocationTargetException e) {
        if (e.getCause() instanceof RuntimeException)
        throw (RuntimeException) e.getCause();
        throw new RuntimeException(
        "Invocation exception for " + _signature + " method", e.getCause());
        }
        }

        public String toString() {
        return _signature + " method";
        }
        }
        /**/
        /**
         * Returns the classes for the specified argument.
         *
         * @param args the comma separated arguments.
         * @return the classes or <code>null</code> if one of the class is not found.
        @JVM-1.4+@
        private Class[] classesFor(String args) {
        args = args.trim();
        if (args.length() == 0) {
        return new Class[0];
        }
        // Counts commas.
        int commas = 0;
        for (int i=0;;) {
        i = args.indexOf(',', i);
        if (i++ < 0) break;
        commas++;
        }
        Class[] classes = new Class[commas + 1];

        int index = 0;
        for (int i = 0; i < commas; i++) {
        int sep = args.indexOf(',', index);
        classes[i] = classFor(args.substring(index, sep).trim());
        if (classes[i] == null) return null;
        index = sep + 1;
        }
        classes[commas] = classFor(args.substring(index).trim());
        if (classes[commas] == null) return null;
        return classes;
        }

        private Class classFor(String className)  {
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
        return getClass(className);
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
        return "L" + className + ";";
        }
        }
        /**/
    }
    private static final Object[] EMPTY_ARRAY = new Object[0]; // Immutable.

}
