/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.function;

import java.io.Serializable;
import java.util.Arrays;

import javolution.util.function.Equality;

/**
 * The array equality implementation.
 */
public final class ArrayEqualityImpl implements Equality<Object>, Serializable {

	private static final long serialVersionUID = 0x700L; // Version.
	public static final ArrayEqualityImpl INSTANCE = new ArrayEqualityImpl();

	@Override
	public boolean areEqual(Object left, Object right) {
	    if (left == right)
            return true;
        if ((left == null) || (right == null))
            return false;
        if (left instanceof Object[] && right instanceof Object[])
            return Arrays.deepEquals((Object[]) left, (Object[]) right);
        if (left instanceof byte[] && right instanceof byte[])
            return Arrays.equals((byte[]) left, (byte[]) right);
        if (left instanceof short[] && right instanceof short[])
            return Arrays.equals((short[]) left, (short[]) right);
        if (left instanceof int[] && right instanceof int[])
            return Arrays.equals((int[]) left, (int[]) right);
        if (left instanceof long[] && right instanceof long[])
            return Arrays.equals((long[]) left, (long[]) right);
        if (left instanceof char[] && right instanceof char[])
            return Arrays.equals((char[]) left, (char[]) right);
        if (left instanceof float[] && right instanceof float[])
            return Arrays.equals((float[]) left, (float[]) right);
        if (left instanceof double[] && right instanceof double[])
            return Arrays.equals((double[]) left, (double[]) right);
        if (left instanceof boolean[] && right instanceof boolean[])
            return Arrays.equals((boolean[]) left, (boolean[]) right);
        return left.equals(right);
   }

}
