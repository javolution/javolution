/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.JavolutionError;
import javolution.lang.Configurable;

/**
 * <p> This class represents a queue of ready to use, potentially recycled 
 *     objects. Instances of this classs are produced by 
 *     {@link AllocatorContext#getQueue(ObjectFactory)}.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, May 6, 2007
 */
public abstract class ObjectQueue/*<T>*/{

    /**
     * Indicates if object queueing is enabled. Changing this parameter can only
     * be done prior to ObjectFactory initialization (typically at start-up). 
     * For example:[code]
     *     static final Configurable.Logic DISABLE_QUEUES = new Configurable.Logic() {
     *         public void run() {
     *             configure(ObjectQueue.QUEUES_ENABLED, false);             
     *         }
     *     };
     *     public void main(String[] args) {
     *         DISABLE_QUEUES.run(); // To be done first.
     *         ...
     *     }[/code]
     */
    public static final Configurable/*<Boolean>*/QUEUES_ENABLED = new Configurable/*<Boolean>*/(
            new Boolean(true)) {
        protected void notifyChange() {
            if (((Boolean) this.get()).booleanValue() != ObjectFactory.USE_QUEUES)
                throw new JavolutionError(
                        "Setting cannot be changed after intialization of the "
                                + "ObjectFactory class (it must be done first)");
        }
    };

    /**
     * Holds the factory used by this pool.  
     */
    protected final ObjectFactory/*<T>*/factory;

    /**
     * Holds the factory.  
     */
    protected Thread user;

    /**
     * Holds the pool's objects.  
     */
    protected Object/*{T}*/[] objects = (Object/*{T}*/[]) new Object[16];

    /**
     * Holds the index of the next object.  
     */
    protected int nextIndex;

    /**
     * Holds the number of objects in this queue.  
     */
    protected int size;

    /**
     * Creates a queue for the specified factory.
     * 
     * @param factory this queue's factory.
     */
    protected ObjectQueue(ObjectFactory/*<T>*/factory) {
        this.factory = factory;
    }

    /**
     * Indicates if this queue is the active queue for the current thread.
     * 
     * @return <code>user == Thread.currentThread()</code>
     */
    public final boolean isCurrent() {
        return user == Thread.currentThread();
    }

    /**
     * Returns the next available object from this queue or {@link #allocate} 
     * one if none.
     * 
     * @return the next available object from this pool.
     */
    public final Object/*{T}*/next() {
        return nextIndex < size ? objects[nextIndex++] : allocate();
    }

    /**
     * Returns the number of elements in this queue.
     * 
     * @return this queue size.
     */
    public final int getSize() {
        return size;
    }

    /**
     * Sets the size of this queue, creating or removing objects as necessary.
     * This method can be used to pre-allocate the queue at start-up.
     * 
     * @param newSize the new queue size.
     */
    public void setSize(int newSize) {
        if (size < newSize) {
            while (newSize > objects.length) {
                resize();
            }
            for (int i = size; i < newSize;) {
                objects[i++] = factory.create();
            }
        } else {
            for (int i = newSize; i < size;) {
                objects[i++] = null;
            }
        }
        size = newSize;
    }

    /**
     * Clears this queue.
     */
    public void clear() {
        for (int i = 0; i < size;) {
            objects[i++] = null;
        }
        nextIndex = size = 0;
    }

    /**
     * Resizes this queue (hopefully rare as queues should be kept small).
     */
    protected void resize() {
        Object/*{T}*/[] tmp = (Object/*{T}*/[]) new Object[objects.length * 2];
        System.arraycopy(objects, 0, tmp, 0, size);
        objects = tmp;
    }

    /**
     * Allocates a new object, this method is called when the queue is empty.
     * 
     * @return the allocated object.
     */
    protected abstract Object/*{T}*/allocate();

    /**
     * Recycles the specified object to this queue.
     * 
     * @param object the object to recycle.
     */
    protected abstract void recycle(Object/*{T}*/object);

}