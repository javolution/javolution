/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Latest release available at http://javolution.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation (http://www.gnu.org/copyleft/lesser.html); either version
 * 2.1 of the License, or any later version.
 */
package java.lang.reflect;

public interface Member {

    public static final int PUBLIC = 0;

    public static final int DECLARED = 1;

    Class getDeclaringClass();

    String getName();
    
    int getModifiers();

}