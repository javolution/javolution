/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

/**
 * <p> This class represents an array factory; it facilitates the detection and
 *     elimination of inderministic array resizing.</p>
 *     
 * <p> Resizing large arrays may introduce unacceptable pause time to real-time
 *     applications and may indicate ill-suited lengths for internal arrays.
 *     By using instances of this class in conjonction with an 
 *     {@link AllocationProfile}, arrays can be preallocated with
 *     the appropriate length to prevent resizing from occurring.<p>
 *      
 * <p> To benefit from this facility, applications need only to use instances of
 *     this class for allocating and resizing internal arrays. For example:<pre>
 *     class Foo {
 *         static final ArrayFactory BARS_FACTORY = new ArrayFactory(32) {
 *             protected Object create(int length) {
 *                 return new Bar[length];
 *             }
 *         };
 *         Bar bars = (Bar[]) BARS_FACTORY.newObject(); // bars.length >= 32
 *         int barsLength;
 *         public void add(Bar bar) {
 *             if (barsLength >= bars.length) {  
 *                 bars = (Bar[]) BARS_FACTORY.resize(bars);
 *             }
 *             bars[barsLength++] = bar;
 *         }
 *     }</pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, March 18, 2005
 */
public abstract class ArrayFactory extends ObjectFactory {
    
    /**
     * Holds the minimum length.
     */ 
    final int _minimumLength; 

    /**
     * Holds the maximum length (after resize).
     */ 
    volatile int _maximumLength; 
    
    /**
     * Creates an array factory producing arrays of specified minimum length.
     * 
     * @param  minimumLength the minimum length of the arrays produced.
     * @throws UnsupportedOperationException if more than one instance per
     *         factory sub-class or if the {@link #MAX} number of factories
     *         has been reached. 
     */
    protected ArrayFactory(int minimumLength) {
        _minimumLength = minimumLength;
        _maximumLength = minimumLength;
    } 

    /**
     * Resizes this array product of this factory. This method may readjust
     * the maximum size of the arrays allocated by this factory.   
     *
     * @param array the array product of this factory being resized. 
     * @return a copy of this array into a larger array.
     */
    public final Object resize(Object[] array) {
        int newLength = array.length << 1;
        if (newLength > _maximumLength) {
           _maximumLength = newLength;
        }
        Object newArray = create(newLength);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Resizes this <code>char</code> array.  
     *
     * @param array the <code>char</code> array being resized. 
     * @return a copy of this array into a larger array.
     */
    public final char[] resize(char[] array) {
        int newLength = array.length << 1;
        if (newLength > _maximumLength) {
            _maximumLength = newLength;
         }
        char[] newArray = (char[]) create(newLength);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }
    
    /**
     * Resizes this <code>boolean</code> array.  
     *
     * @param array the <code>boolean</code> array being resized. 
     * @return a copy of this array into a larger array.
     */
    public final boolean[] resize(boolean[] array) {
        int newLength = array.length << 1;
        if (newLength > _maximumLength) {
            _maximumLength = newLength;
         }
        boolean[] newArray = (boolean[]) create(newLength);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Resizes this <code>int</code> array.  
     *
     * @param array the <code>int</code> array being resized. 
     * @return a copy of this array into a larger array.
     */
    public final int[] resize(int[] array) {
        int newLength = array.length << 1;
        if (newLength > _maximumLength) {
            _maximumLength = newLength;
         }
        int[] newArray = (int[]) create(newLength);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Resizes this <code>long</code> array.  
     *
     * @param array the <code>long</code> array being resized. 
     * @return a copy of this array into a larger array.
     */
    public final long[] resize(long[] array) {
        int newLength = array.length << 1;
        if (newLength > _maximumLength) {
            _maximumLength = newLength;
         }
        long[] newArray = (long[]) create(newLength);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Resizes this <code>float</code> array.  
     *
     * @param array the <code>float</code> array being resized. 
     * @return a copy of this array into a larger array.
     /*@FLOATING_POINT@
    public final float[] resize(float[] array) {
        int newLength = array.length << 1;
        if (newLength > _maximumLength) {
            _maximumLength = newLength;
        }
        float[] newArray = (float[]) create(newLength);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }
    /**/

    /**
     * Resizes this <code>double</code> array.  
     *
     * @param array the <code>double</code> array being resized. 
     * @return a copy of this array into a larger array.
     /*@FLOATING_POINT@
    public final double[] resize(double[] array) {
        int newLength = array.length << 1;
        if (newLength > _maximumLength) {
            _maximumLength = newLength;
        }
        double[] newArray = (double[]) create(newLength);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }
    /**/

    /**
     * Creates a new array of minimum length. 
     *
     * @return a new array.
     */
    protected final Object create() {
        return create(_minimumLength);
    }

    /**
     * Creates a new array of specified length.
     *
     * @param length the length of the array to be produced. 
     * @return a new array.
     */
    protected abstract Object create(int length);

    // Overrides.
    synchronized void preallocate() {
        for (int i = 0; i < _allocatedCount; i++) {
            Node node = new Node();
            node._object = this.create(_maximumLength);
            node._next = _preallocated;
            _preallocated = node;
        }
        if (_allocatedCount > _preallocatedCount) {
            _preallocatedCount = _allocatedCount;
        }
        _allocatedCount = 0;
    }
}