/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

/**
 * <p> This class provides static methods to return {@link ObjectPool} for
 *     primitive type arrays or generic object arrays. The pool returned 
 *     allocates arrays from the "stack" when the current thread executes in 
 *     {@link PoolContext}. For example:<pre>
 *     char[] chars = (char[]) ArrayPool.charArray(128).next();
 *     Object[] elements = (Object[]) ArrayPool.objectArray(64).next();
 *     </pre>
 * <p> The arrays returned may have a length larger than the specified capacity.
 *     If the exact length or specific component types are required,
 *     custom {@link ObjectFactory} should be used.</p>
 * <p> Allocating large arrays is very time consuming and prone to garbage
 *     collection interrruptions. It is therefore highly recommended to use this
 *     class when new arrays are dynamically allocated.</p>
 * <p> Arrays may be individually recycled when it can be asserted 
 *     that the arrays will not be referenced anymore. For example:<pre>
 *     ObjectPool pool = ArrayPool.charArray(1024);
 *     char[] buffer = (char[]) pool.next(); 
 *     for (int i = reader.read(buffer, 0, buffer.length); i > 0;) {
 *         ...
 *     } 
 *     pool.recycle(buffer);</pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class ArrayPool {

    /**
     * Holds the minimum length array (<code>16</code>).
     */
    public static final int MIN_LENGTH = 16;

    /**
     * Default constructor (forbids derivation).
     */
    private ArrayPool() {
    }

    /**
     * Returns the current pool for <code>Object[]</code>.
     *
     * @param  capacity the minimum length of the array.
     * @return the current pool.
     */
    public static ObjectPool objectArray(int capacity) {
        return OBJECTS_FACTORIES[indexFor(capacity)].currentPool();
    }
    private final static class ObjectsFactory extends ObjectFactory {

        private final int _length;

        private ObjectsFactory(int length) {
            _length = length;
        }

        public Object create() {
            return new Object[_length];
        }
    }

    private static final ObjectsFactory[] OBJECTS_FACTORIES;
    static {
        OBJECTS_FACTORIES = new ObjectsFactory[28];
        for (int i = 0; i < OBJECTS_FACTORIES.length; i++) {
            OBJECTS_FACTORIES[i] = new ObjectsFactory(MIN_LENGTH << i);
        }
    }

    /**
     * Returns the current pool for <code>byte[]</code>.
     *
     * @param  capacity the minimum length of the array.
     * @return the current pool.
     */
    public static ObjectPool byteArray(int capacity) {
        return BYTES_FACTORIES[indexFor(capacity)].currentPool();
    }

    private final static class BytesFactory extends ObjectFactory {

        private final int _length;

        private BytesFactory(int length) {
            _length = length;
        }

        public Object create() {
            return new byte[_length];
        }
    }

    private static final BytesFactory[] BYTES_FACTORIES;
    static {
        BYTES_FACTORIES = new BytesFactory[28];
        for (int i = 0; i < BYTES_FACTORIES.length; i++) {
            BYTES_FACTORIES[i] = new BytesFactory(MIN_LENGTH << i);
        }
    }

    /**
     * Returns the current pool for <code>char[]</code>.
     *
     * @param  capacity the minimum length of the array.
     * @return the current pool.
     */
    public static ObjectPool charArray(int capacity) {
        return CHARS_FACTORIES[indexFor(capacity)].currentPool();
    }

    private final static class CharsFactory extends ObjectFactory {

        private final int _length;

        private CharsFactory(int length) {
            _length = length;
        }

        public Object create() {
            return new char[_length];
        }
    }

    private static final CharsFactory[] CHARS_FACTORIES;
    static {
        CHARS_FACTORIES = new CharsFactory[28];
        for (int i = 0; i < CHARS_FACTORIES.length; i++) {
            CHARS_FACTORIES[i] = new CharsFactory(MIN_LENGTH << i);
        }
    }

    /**
     * Returns the current pool for <code>int[]</code>.
     *
     * @param  capacity the minimum length of the array.
     * @return the current pool.
     */
    public static ObjectPool intArray(int capacity) {
        return INTS_FACTORIES[indexFor(capacity)].currentPool();
    }

    private final static class IntsFactory extends ObjectFactory {

        private final int _length;

        private IntsFactory(int length) {
            _length = length;
        }

        public Object create() {
            return new int[_length];
        }
    }

    private static final IntsFactory[] INTS_FACTORIES;
    static {
        INTS_FACTORIES = new IntsFactory[28];
        for (int i = 0; i < INTS_FACTORIES.length; i++) {
            INTS_FACTORIES[i] = new IntsFactory(MIN_LENGTH << i);
        }
    }

    /**
     * Returns the current pool for <code>long[]</code>.
     *
     * @param  capacity the minimum length of the array.
     * @return the current pool.
     */
    public static ObjectPool longArray(int capacity) {
        return LONGS_FACTORIES[indexFor(capacity)].currentPool();
    }

    private final static class LongsFactory extends ObjectFactory {

        private final int _length;

        private LongsFactory(int length) {
            _length = length;
        }

        public Object create() {
            return new long[_length];
        }
    }

    private static final LongsFactory[] LONGS_FACTORIES;
    static {
        LONGS_FACTORIES = new LongsFactory[28];
        for (int i = 0; i < LONGS_FACTORIES.length; i++) {
            LONGS_FACTORIES[i] = new LongsFactory(MIN_LENGTH << i);
        }
    }

    /**
     * Returns the current pool for <code>float[]</code>.
     *
     * @param  capacity the minimum length of the array.
     * @return the current pool.
     */
    public static ObjectPool floatArray(int capacity) {
        return FLOATS_FACTORIES[indexFor(capacity)].currentPool();
    }

    private final static class FloatsFactory extends ObjectFactory {

        private final int _length;

        private FloatsFactory(int length) {
            _length = length;
        }

        public Object create() {
            return new float[_length];
        }
    }

    private static final FloatsFactory[] FLOATS_FACTORIES;
    static {
        FLOATS_FACTORIES = new FloatsFactory[28];
        for (int i = 0; i < FLOATS_FACTORIES.length; i++) {
            FLOATS_FACTORIES[i] = new FloatsFactory(MIN_LENGTH << i);
        }
    }

    /**
     * Returns the current pool for <code>double[]</code>.
     *
     * @param  capacity the minimum length of the array.
     * @return the current pool.
     */
    public static ObjectPool doubleArray(int capacity) {
        return DOUBLES_FACTORIES[indexFor(capacity)].currentPool();
    }

    private final static class DoublesFactory extends ObjectFactory {

        private final int _length;

        private DoublesFactory(int length) {
            _length = length;
        }

        public Object create() {
            return new double[_length];
        }
    }

    private static final DoublesFactory[] DOUBLES_FACTORIES;
    static {
        DOUBLES_FACTORIES = new DoublesFactory[28];
        for (int i = 0; i < DOUBLES_FACTORIES.length; i++) {
            DOUBLES_FACTORIES[i] = new DoublesFactory(MIN_LENGTH << i);
        }
    }

    /**
     * Returns a factory index (0-27) for the specified capacity.
     *  
     * @param capacity the required capacity.
     * @return <code>j</code> such as 
     *         <code>({@link #MIN_LENGTH} << j) >= capacity</code>
     */
    public static int indexFor(int capacity) {
        return (capacity < INDEX_FOR.length)
                ? INDEX_FOR[capacity]
                : indexFor2(capacity);
    }
    private static int indexFor2(int capacity) {
        int j = 0;
        while (capacity > (MIN_LENGTH << j)) {
            j++;
        }
        return j;
    }
    private static final int[] INDEX_FOR;
    static {
        INDEX_FOR = new int[1025];
        for (int i = INDEX_FOR.length; i > 0;) {
            INDEX_FOR[--i] = indexFor2(i);
        }
    }

    /**
     * Clears an array beginning at the specified position.
     *  
     * @param  array the array whose components are set to <code>null</code>.
     * @param  start the the starting position.
     * @param  length the number of components to clear.
     * @throws IndexOutOfBoundsException if clearing would cause access of data
     *         outside array bounds.
     */
    public static void clear(Object[] array, int start, int length) {
        if (length < 32) {
            for (int i = start + length; i > start;) {
                array[--i] = null;
            }
        } else { // Use array copy.
            for (int i = length - 1024; i >= 0; i -= 1024) {
                System.arraycopy(NULL_1024, 0, array, i + start, 1024);
            }
            System.arraycopy(NULL_1024, 0, array, start, length & 1023);
        }
    }
    private static final Object[] NULL_1024 = new Object[1024];

}