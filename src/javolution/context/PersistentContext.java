/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import j2me.io.Serializable;
import j2me.lang.Comparable;
import javolution.util.FastMap;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> This class represents a context persistent accross multiple program 
 *     executions. It is typically used to hold persistent 
 *     {@link Reference references}.</p>  
 *     
 * <p> How this context is loaded/saved is application specific. 
 *     Although, the simplest way is to use Javolution XML serialization 
 *     facility. For example:[code]
 *      import javolution.xml.XMLObjectReader;
 *      import javolution.xml.XMLObjectWriter;
 *      public void main(String[]) {
 *           // Loads persistent context (typically at start-up).
 *           PersistentContext ctx = new XMLObjectReader().setInput(
 *               new FileInputStream("C:/persistent.xml")).read(PersistentContext.class);
 *           PersistentContext.setInstance(ctx);
 *           ...
 *           
 *           // Saves persistent context for future execution.
 *           new XMLObjectWriter().setInput(
 *               new FileOutputStream("C:/persistent.xml")).write(
 *                   PersistentContext.getInstance(), PersistentContext.class);
 *      }[/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2006
 */
public class PersistentContext extends Context {

    /**
     * Holds the class object (cannot use .class with j2me).
     */
    private static final Class CLASS = new PersistentContext().getClass();;

    /**
     * Holds the single instance.
     */
    private static PersistentContext _PersistentContext = new PersistentContext();

    /**
     * Holds current id to value mapping.
     */
    private final FastMap _idToValue = new FastMap();

    /**
     * Holds the XML representation for persistent contexts
     * (holds persistent reference mapping).
     */
    protected static final XMLFormat/*<PersistentContext>*/XML = new XMLFormat(
            CLASS) {
        public void read(InputElement xml, Object obj)
                throws XMLStreamException {
            final PersistentContext ctx = (PersistentContext) obj;
            ctx._idToValue.putAll((FastMap) xml.get("References"));
        }

        public void write(Object obj, OutputElement xml)
                throws XMLStreamException {
            final PersistentContext ctx = (PersistentContext) obj;
            xml.add(ctx._idToValue, "References");
        }
    };

    /**
     * Default constructor.
     */
    public PersistentContext() {
    }

    /**
     * Returns the persistent instance (singleton). 
     * 
     * @return the persistent instance.
     */
    public PersistentContext getInstance() {
        return _PersistentContext;
    }

    /**
     * Sets the persistent instance. 
     * 
     * @param ctx the persistent instance.
     */
    public void setInstance(PersistentContext ctx) {
        _PersistentContext = ctx;
        synchronized (Reference.INSTANCES) {
             for (FastMap.Entry e = Reference.INSTANCES.head(), end = Reference.INSTANCES.tail();
                   (e = (FastMap.Entry) e.getNext())!= end;) {
                 Reference ref = (Reference) e.getValue();
                 ref.set(ctx._idToValue.get(ref._id));
             }
        }
    }

    /**
     * Returns the current persistent context.  
     *
     * @return <code>PersistentContext.getInstance()</code>
     */
    public static/*PersistentContext*/Context current() {
        return _PersistentContext;
    }

    // Implements Context abstract method.
    protected void enterAction() {
        throw new j2me.lang.UnsupportedOperationException(
                "Cannot enter persistent context (already in)");
    }

    // Implements Context abstract method.
    protected void exitAction() {
        throw new j2me.lang.UnsupportedOperationException(
                "Cannot exit persistent context (always in)");
    }

    /**
     * <p> This class represents a reference over an object which can be kept 
     *     persistent accross multiple program executions. Instances of this class 
     *     are typically used to hold global data time consuming to regenerate. 
     *     For example:[code]
     *     public class FastMap<K,V> implements Map<K, V> {
     *         // Provides a constructor for persistent maps.
     *         public FastMap(String id) {
     *             new PersistentContext<Map<K, V>.Reference(id, this) {
     *                  protected void notifyValueChange() {
     *                      FastMap.this.clear();
     *                      FastMap.this.putAll(this.get());
     *                  }
     *             };
     *         }
     *     }
     *     ...
     *     // Persistent lookup table for units multiplications.
     *     static FastMap<Unit, FastMap<Unit, Unit>> UNITS_MULT_LOOKUP 
     *          =  new FastMap<Unit, FastMap<Unit, Unit>>("UNITS_MULT_LOOKUP").setShared(true);
     *    [/code]</p>
     *    
     * <p> Persistent references may also be used to hold optimum configuration 
     *     values set from previous executions. For example:[code]
     *     public Targets {  
     *          private static PersistentContext.Reference<Integer> CAPACITY 
     *               = new PersistentContext.Reference<Integer>("Targets#CAPACITY", 256);
     *          private Target[] _targets = new Target[CAPACITY.get()];
     *          private int _count;
     *          public void add(Target target) {
     *              if (_count == _targets.length) { // Ooops, resizes.
     *                  Target[] tmp = new Target[_count * 2];
     *                  System.arraycopy(_targets, 0, tmp, 0, _count);
     *                  _targets = tmp;
     *                  CAPACITY.setMinimum(_targets.length); // Persists. 
     *              }
     *              _targets[_count++] target;
     *         }
     *     }[/code]
     *
     * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
     * @version 4.0, September 4, 2006
     */
    public static class Reference /*<T>*/implements
            javolution.lang.Reference/*<T>*/, Serializable {

        /**
         * Holds the instances.
         */
        private final static FastMap INSTANCES = new FastMap();

        /**
         * Holds the unique identifier.
         */
        private final String _id;

        /**
         * Holds the current value.
         */
        private Object/*{T}*/ _value;

        /**
         * Creates a persistent reference identified by the specified name and 
         * having the specified default value.
         * 
         * @param id the unique identifier.
         * @param defaultValue the default value.
         * @throws IllegalArgumentException if the name is not unique.
         */
        public Reference(String id, Object/*{T}*/defaultValue) {
            _id = id;
            _value = defaultValue;
            synchronized (INSTANCES) {
                if (INSTANCES.containsKey(id))
                    throw new IllegalArgumentException("Identifier " + id
                            + " already in use");
                INSTANCES.put(id, this);
            }
            if (_PersistentContext._idToValue.containsKey(id)) {
                set((Object/*{T}*/) _PersistentContext._idToValue.get(id));
            }
        }

        // Implements Reference interface.
        public Object/*{T}*/get() {
            return _value;
        }

        // Implements Reference interface.
        public void set(Object/*{T}*/value) {
            _value = value;
            notifyValueChange();
        }

        /**
         * Sets this reference to the specified value only if 
         * <code>(value.compareTo(this.get()) &gt; 0)</code>.
         * 
         * @param value the minimum value for this reference.
         * @throws IllegalArgumentException if the specified value is not 
         *         {@link Comparable} or an {@link Integer} instance (J2ME).
         */
        public void setMinimum(Object/*{T}*/value) {
            synchronized (this) {
                if (value instanceof Comparable) {
                    Object prevValue = get();
                    if (((Comparable) value).compareTo(prevValue) > 0) {
                        set(value);
                    }
                } else if (value instanceof Integer) {
                    Object prevValue = get();
                    if (((Integer) value).intValue() > ((Integer) prevValue)
                            .intValue()) {
                        set(value);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        /**
         * Sets this reference to the specified value only if 
         * <code>(value.compareTo(this.get()) &lt; 0)</code>.
         * 
         * @param value the maximum value for this reference.
         * @throws IllegalArgumentException if the specified value is not 
         *         {@link Comparable} or an {@link Integer} instance (J2ME).
         */
        public void setMaximum(Object/*{T}*/value) {
            synchronized (this) {
                if (value instanceof Comparable) {
                    Object prevValue = get();
                    if (((Comparable) value).compareTo(prevValue) < 0) {
                        set(value);
                    }
                } else if (value instanceof Integer) {
                    Object prevValue = get();
                    if (((Integer) value).intValue() < ((Integer) prevValue)
                            .intValue()) {
                        set(value);
                    }
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        /**
         * Returns the string representation of the current value of this 
         * reference.
         *
         * @return <code>String.valueOf(this.get())</code>
         */
        public String toString() {
            return String.valueOf(get());
        }

        /**
         * Notifies this reference that its value has changed (for example
         * a new persistent context has been loaded).
         * The default implementation does nothing.
         */
        protected void notifyValueChange() {
        }

    }
}