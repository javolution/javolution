/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util;

import java.io.ObjectStreamException;
import java.lang.Appendable;
import java.lang.Comparable;
import java.lang.Number;
import java.util.List;
import javax.realtime.MemoryArea;

import org.javolution.lang.Configurable;
import org.javolution.lang.Immutable;
import org.javolution.lang.Realtime;
import org.javolution.text.Cursor;
import org.javolution.text.Text;
import org.javolution.text.TextFormat;
import org.javolution.text.TypeFormat;
import org.javolution.util.FastCollection.Record;
import org.javolution.xml.XMLSerializable;

import java.lang.CharSequence;
import java.io.IOException;

/**
 * <p> This class represents a <b>unique</b> index which can be used instead of 
 *     <code>java.lang.Integer</code> for primitive data types collections. 
 *     For example:[code]
 *         class SparseVector<F> {
 *             FastMap<Index, F> _elements = new FastMap<Index, F>();
 *             ...
 *         }[/code]</p>
 *          
 * <p> Unicity is guaranteed and direct equality (<code>==</code>) can be used 
 *     in place of object equality (<code>Index.equals(Object)</code>).</p>
 * 
 * <p> Indices have no adverse effect on the garbage collector (persistent 
 *     instances), but should not be used for large integer values as that  
 *     would increase the permanent memory footprint significantly.</p> 
 * 
 * <p><b>RTSJ:</b> Instance of this classes are allocated in 
 *    <code>ImmortalMemory</code>. Indices can be pre-allocated at start-up
 *    to avoid run-time allocation delays by configuring 
 *    {@link #INITIAL_FIRST} and/or {@link #INITIAL_LAST} or through 
 *    {@link #setMinimumRange}.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 26, 2007
 */
public final class Index extends Number implements 
        Comparable <Index> , Record, Realtime, Immutable, XMLSerializable  {

    /**
     * Holds the index zero (value <code>0</code>).
     */
    public static final Index ZERO = new Index(0);

    /**
     * Holds negative indices (immortal memory).
     */
    private static Index[] _NegativeIndices = new Index[32];
    static {
        _NegativeIndices[0] = ZERO;
        _NegativeIndices[1] = new Index(-1);
    }
    
    /**
     * Holds positive indices length.
     */
    private static int _NegativeIndicesLength = 2;

    /**
     * Holds the initial first index value (default <code>-1</code>).
     */
    public static final Configurable <Integer>  INITIAL_FIRST
        = new Configurable(new Integer(-(_NegativeIndicesLength - 1))) {
        protected void notifyChange(Object oldValue, Object newValue) {
            // Ensures Index creation from minimum value. 
            Index.valueOf(((Integer)newValue).intValue());
        }
    };
        
    /**
     * Holds positive indices (immortal memory).
     */
    private static Index[] _PositiveIndices = new Index[32];
    static {
        _PositiveIndices[0] = ZERO;
        for (int i=1; i < _PositiveIndices.length; i++) {
            _PositiveIndices[i] = new Index(i);
         }
    }

    /**
     * Holds positive indices length.
     */
    private static int _PositiveIndicesLength = _PositiveIndices.length;

    /**
     * Holds the initial last index value (default <code>31</code>).
     */
    public static final Configurable <Integer>  INITIAL_LAST
        = new Configurable(new Integer(_PositiveIndicesLength - 1)) {
        protected void notifyChange(Object oldValue, Object newValue) {
            // Ensures Index creation to maximum value. 
            Index.valueOf(((Integer)newValue).intValue());
        }
    };
    
    /**
     * Holds the immortal memory area (static fields are initialized in 
     * immortal memory). 
     */
    private static final MemoryArea IMMORTAL_MEMORY = 
        MemoryArea.getMemoryArea(new Object());

    /**
     * Holds the index position.
     */
    private final int _value;

    /**
     * Creates an index at the specified position.
     * 
     * @param i the index position.
     */
    private Index(int i) {
        _value = i;
    }

    /**
     * Creates the indices for the specified range of values if they don't 
     * exist.
     * 
     * @param first the first index value.
     * @param last the last index value.
     * @throws IllegalArgumentException if <code>first > last</code>
     */
    public static void setMinimumRange(int first, int last) {
    	if (first > last) throw new IllegalArgumentException();
    	Index.valueOf(first);
    	Index.valueOf(last);
    }    
    
    /**
     * Returns the unique index for the specified <code>int</code> value 
     * (creating it as well as the indices toward {@link #ZERO zero} 
     *  if they do not exist). 
     * 
     * @param i the index value.
     * @return the corresponding unique index.
     */
    public static Index valueOf(int i) { // Short to be inlined.
        return (i >= 0) ? (i < _PositiveIndicesLength) ? _PositiveIndices[i] 
            : createPositive(i) : valueOfNegative(-i);
    }    
    
    /**
     * Returns all the indices greater or equal to <code>start</code>
     * but less than <code>end</code>.
     *
     * @param start the start index.
     * @param end the end index.
     * @return <code>[start .. end[</code>
     */
    public static List <Index>  rangeOf(int start, int end) {
        FastTable <Index>  list = FastTable.newInstance();
        for (int i=start; i < end; i++) {
            list.add(Index.valueOf(i));
        }
        return  list;
    }

    /**
     * Returns the list of all the indices specified.
     *
     * @param indices the indices values.
     * @return <code>{indices[0], indices[1], ...}</code>
     *  */
    public static List<Index> valuesOf(int ... indices) {
        FastTable<Index> list = FastTable.newInstance();
        for (int i:indices) {
            list.add(Index.valueOf(i));
        }
        return  list;
    } /**/

    private static Index valueOfNegative(int i) {    
        return i < _NegativeIndicesLength ?
                    _NegativeIndices[i] : createNegative(i);
    }

    private static synchronized Index createPositive(int i) {
        if (i < _PositiveIndicesLength) // Synchronized check. 
            return _PositiveIndices[i];
        while (i >= _PositiveIndicesLength) {
            IMMORTAL_MEMORY.executeInArea(AUGMENT_POSITIVE);
        }
        return _PositiveIndices[i];
    }
    
    private static synchronized Index createNegative(int i) {
            if (i < _NegativeIndicesLength) // Synchronized check. 
                return _NegativeIndices[i];
            while (i >= _NegativeIndicesLength) {
                IMMORTAL_MEMORY.executeInArea(AUGMENT_NEGATIVE);
            }
            return _NegativeIndices[i];
        
    }

    private static final Runnable AUGMENT_POSITIVE = new Runnable() {
        public void run() {
            for (int i = _PositiveIndicesLength, 
                     n = _PositiveIndicesLength + INCREASE_AMOUNT; i < n; i++) {
                
                Index index = new Index(i);
 
                if (_PositiveIndices.length <= i) { // Resize.
                    Index[] tmp = new Index[_PositiveIndices.length * 2];
                    System.arraycopy(_PositiveIndices, 0, tmp, 0, _PositiveIndices.length);
                    _PositiveIndices = tmp;
                }
                
                _PositiveIndices[i] = index;
            }
            _PositiveIndicesLength += INCREASE_AMOUNT;
        }
    };

    private static final Runnable AUGMENT_NEGATIVE = new Runnable() {
        public void run() {
            for (int i = _NegativeIndicesLength, 
                     n = _NegativeIndicesLength + INCREASE_AMOUNT; i < n; i++) {
                
                Index index = new Index(-i);

                if (_NegativeIndices.length <= i) { // Resize.
                    Index[] tmp = new Index[_NegativeIndices.length * 2];
                    System.arraycopy(_NegativeIndices, 0, tmp, 0, _NegativeIndices.length);
                    _NegativeIndices = tmp;
                }
                
                _NegativeIndices[i] = index;
            }
           _NegativeIndicesLength += INCREASE_AMOUNT;
        }
    };
    
    private static final int INCREASE_AMOUNT = 32;

    /**
     * Returns the index value as <code>int</code>.
     * 
     * @return the index value.
     */
    public int intValue() {
        return _value;
    }

    /**
     * Returns the index value as <code>long</code>.
     * 
     * @return the index value.
     */
    public long longValue() {
        return intValue();
    }
    
    /**
     * Returns the index value as <code>float</code>.
     * 
     * @return the index value.
     */
    public float floatValue() {
        return (float) intValue();
    }

    /**
     * Returns the index value as <code>int</code>.
     * 
     * @return the index value.
     */
    public double doubleValue() {
        return (double) intValue();
    }

    /**
     * Returns the <code>String</code> representation of this index.
     * 
     * @return <code>TextFormat.getInstance(Cursor.class).formatToString(_value)</code>
     */
    public String toString() {
        return TextFormat.getInstance(Index.class).formatToString(this);
    }

    /**
     * Indicates if this index is equals to the one specified (unicity 
     * ensures that this method is equivalent to <code>==</code>).
     * 
     * @return <code>this == obj</code>
     */
    public final boolean equals(Object obj) {
        return this == obj;
    }

    /**
     * Returns the hash code for this index.
     *
     * @return the index value.
     */
    public final int hashCode() {
        return _value;
    }

    /**
     * Ensures index unicity during deserialization.
     * 
     * @return the unique instance for this deserialized index.
     */
    protected final Object readResolve() throws ObjectStreamException {
        return Index.valueOf(_value);
    }    

    //  Implements Comparable interface.
    public final int compareTo( Index  that) {
        return this._value - ((Index)that)._value;
    }

    // Implements Record interface.
    public final Record getNext() {
        return Index.valueOf(_value + 1);
    }

    // Implements Record interface.
    public final Record getPrevious() {
        return Index.valueOf(_value - 1);
    }    

    // Implements Realtime interface.
    public Text toText() {
        return TextFormat.getInstance(Index.class).format(this);
    }

   /**
     * Holds the default text format.
     */
    static final TextFormat TEXT_FORMAT = new TextFormat(Index.class) {

        public Appendable format(Object obj, Appendable dest)
                throws IOException {
            return TypeFormat.format(((Index) obj).intValue(), dest);
        }

        public Object parse(CharSequence csq, Cursor cursor) {
            return Index.valueOf(TypeFormat.parseInt(csq, 10, cursor));
        }
    };

    private static final long serialVersionUID = 1L;

}