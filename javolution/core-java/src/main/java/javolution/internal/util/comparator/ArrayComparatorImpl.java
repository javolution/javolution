/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.comparator;

import java.util.Arrays;

/**
 * The array comparator implementation.
 */
public class ArrayComparatorImpl extends StandardComparatorImpl<Object> {

    private static final long serialVersionUID = 4134048629840904441L;

    @Override
    public int hashCodeOf(Object array) {
        if (array instanceof Object[])
            return Arrays.deepHashCode((Object[]) array);
        if (array instanceof byte[])
            return Arrays.hashCode((byte[]) array);
        if (array instanceof short[])
            return Arrays.hashCode((short[]) array);
        if (array instanceof int[])
            return Arrays.hashCode((int[]) array);
        if (array instanceof long[])
            return Arrays.hashCode((long[]) array);
        if (array instanceof char[])
            return Arrays.hashCode((char[]) array);
        if (array instanceof float[])
            return Arrays.hashCode((float[]) array);
        if (array instanceof double[])
            return Arrays.hashCode((double[]) array);
        if (array instanceof boolean[])
            return Arrays.hashCode((boolean[]) array);
        if (array != null)
            return array.hashCode();
        return 0;
    }

    @Override
    public boolean areEqual(Object array1, Object array2) {
        if (array1 == array2)
            return true;
        if ((array1 == null) || (array2 == null))
            return false;
        if (array1 instanceof Object[] && array2 instanceof Object[])
            return Arrays.deepEquals((Object[]) array1, (Object[]) array2);
        if (array1 instanceof byte[] && array2 instanceof byte[])
            return Arrays.equals((byte[]) array1, (byte[]) array2);
        if (array1 instanceof short[] && array2 instanceof short[])
            return Arrays.equals((short[]) array1, (short[]) array2);
        if (array1 instanceof int[] && array2 instanceof int[])
            return Arrays.equals((int[]) array1, (int[]) array2);
        if (array1 instanceof long[] && array2 instanceof long[])
            return Arrays.equals((long[]) array1, (long[]) array2);
        if (array1 instanceof char[] && array2 instanceof char[])
            return Arrays.equals((char[]) array1, (char[]) array2);
        if (array1 instanceof float[] && array2 instanceof float[])
            return Arrays.equals((float[]) array1, (float[]) array2);
        if (array1 instanceof double[] && array2 instanceof double[])
            return Arrays.equals((double[]) array1, (double[]) array2);
        if (array1 instanceof boolean[] && array2 instanceof boolean[])
            return Arrays.equals((boolean[]) array1, (boolean[]) array2);
        return array1.equals(array2);
    }

}
