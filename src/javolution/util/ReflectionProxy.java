/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * <p> This optional class provides reflection utilities which may not be 
 *     exist on all platforms (e.g. J2ME CLDC); its methods are called 
 *     only when reflection is supported and it is expected that it 
 *     <b>will not compile</b> on all platforms. </p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
final class ReflectionProxy {

    /**
     * Default constructor.
     */
    private ReflectionProxy() {
    }

    /**
     * Returns the public constructor of the specified class with the specified
     * arguments types.
     * 
     * @param forClass the class for which the constructor is returned.
     * @param argTypes the arguments types of the constructor to return.
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    static Constructor getConstructor(Class forClass, Class[] argsTypes)
            throws SecurityException, NoSuchMethodException {
        // This call may not compile on all platforms in which case the 
        // UnsupportedOperationException can be raised or errors on this class
        // can simply be ignored (the ReflectionProxy class is optional).
        return forClass.getConstructor(argsTypes);
        //throw new UnsupportedOperationException();
    }

    /**
     * Returns the public method of the specified class with the specified
     * name and arguments types.
     * 
     * @param forClass the class for which the method is returned.
     * @param methodName the name of the method.
     * @param argTypes the arguments types of the method to return.
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    static Method getMethod(Class forClass, String methodName, Class[] argsTypes)
            throws SecurityException, NoSuchMethodException {
        // This call may not compile on all platforms in which case the 
        // UnsupportedOperationException can be raised or errors on this class
        // can simply be ignored (the ReflectionProxy class is optional).
        return forClass.getMethod(methodName, argsTypes);
        //throw new UnsupportedOperationException();
    }

}