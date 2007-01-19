/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import j2me.io.ObjectStreamException;
import j2me.io.Serializable;
import j2mex.realtime.MemoryArea;
import javolution.lang.Immutable;
import javolution.util.FastCollection.Record;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * This class represents a unique index object. Instances of this class 
 * are immutable and can be used for direct iteration over random access 
 * collections (e.g. {@link FastTable}) or for specialized <code>int</code> 
 * to <code>Object</code> mapping. For example:[code]
 *    class SparseVector<F> {
 *        FastMap<Index, F> _elements = new FastMap<Index, F>();
 *        ...
 *    }
 * [/code] 
 * Direct object equality can be used (<code>==</code>) to compare indexes.
 * Indexes have no adverse effect on the garbage collectors but should only
 * be used for reasonably small <code>int</code> values.
 * 
 * <p><b>RTSJ:</b> Instance of this classes are always allocated in 
 *    <code>ImmortalMemory</code>.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, December 18, 2006
 */
public final class Index implements Record, Immutable, Serializable {

    /**
     * Holds the index zero (value <code>0</code>).
     */
    public static final Index ZERO = new Index(0);

    /**
     * Holds positive indexes (immortal memory).
     */
    private static final FastTable POSITIVE = new FastTable();
    static {
        POSITIVE.add(ZERO);
    }

    /**
     * Holds negative indexes (immortal memory).
     */
    private static final FastTable NEGATIVE = new FastTable();
    static {
        NEGATIVE.add(ZERO);
    }

    /**
     * Holds the default XML representation for indexes.
     * This presentation consists of a <code>"value"</code> attribute 
     * holding the index <code>int</code> value.
     */
    protected static final XMLFormat/*<Index>*/XML = new XMLFormat(new Index(0)
            .getClass()) {

        public boolean isReferenceable() {
            return false; // Always by value (immutable). 
        }

        public Object newInstance(Class cls, InputElement xml)
                throws XMLStreamException {
            return Index.valueOf(xml.getAttribute("value", 0));
        }

        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            xml.setAttribute("value", ((Index) obj).intValue());
        }
    };

    /**
     * Holds the index position.
     */
    private final int _value;

    /**
     * Holds the next index.
     */
    private transient Index _next;

    /**
     * Holds the previous node.
     */
    private transient Index _previous;

    /**
     * Creates an index at the specified position.
     * 
     * @param i the index position.
     */
    private Index(int i) {
        _value = i;
    }

    /**
     * Returns the unique index for the specified <code>int</code> value 
     * (creating it as well as the indices toward {@link #ZERO zero} 
     *  if they do not exist). 
     * 
     * @param i the index value.
     * @return the corresponding unique index.
     */
    public static Index valueOf(int i) {
        return ((i >= 0) && (i < POSITIVE.size())) ? (Index) POSITIVE.get(i)
                : ((i < 0) && (-i < NEGATIVE.size())) ? (Index) NEGATIVE
                        .get(-i) : createInstance(i);
    }

    private static synchronized Index createInstance(int value) {
        if (value >= 0) {
            while (value >= POSITIVE.size()) {
                MemoryArea.getMemoryArea(POSITIVE).executeInArea(
                        AUGMENT_POSITIVE);
            }
            return (Index) POSITIVE.get(value);
        } else {
            while (-value >= NEGATIVE.size()) {
                MemoryArea.getMemoryArea(NEGATIVE).executeInArea(
                        AUGMENT_NEGATIVE);
            }
            return (Index) NEGATIVE.get(-value);
        }
    }

    private static Runnable AUGMENT_POSITIVE = new Runnable() {
        public void run() {
            Index prev = (Index) POSITIVE.getLast();
            for (int i = 0; i < 16; i++) { // 16 at a time.
                Index index = new Index(prev._value + 1);
                index._previous = prev;
                prev._next = index;
                POSITIVE.add(index);
                prev = index;
            }
        }
    };

    private static Runnable AUGMENT_NEGATIVE = new Runnable() {
        public void run() {
            Index next = (Index) NEGATIVE.getLast();
            for (int i = 0; i < 16; i++) { // 16 at a time.
                Index index = new Index(next._value - 1);
                index._next = next;
                next._previous = index;
                NEGATIVE.add(index);
                next = index;
            }
        }
    };

    /**
     * Returns the index value.
     * 
     * @return the index value.
     */
    public final int intValue() {
        return _value;
    }

    /**
     * Returns the <code>String</code> representation of this index.
     * 
     * @return this index value formatted as a string.
     */
    public final String toString() {
        return String.valueOf(_value);
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
        return valueOf(_value);
    }    

    // Implements Record interface.
    public final Record getNext() {
        return _next;
    }

    // Implements Record interface.
    public final Record getPrevious() {
        return _previous;
    }    

    private static final long serialVersionUID = 1L;
}