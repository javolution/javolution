/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;
import j2me.lang.UnsupportedOperationException;

/**
 * This class provides a default implementation for {@link ObjectPool}.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
final class DefaultPool extends ObjectPool {

    /**
     * Holds the pool's objects.
     */
    private Object[] _objects = new Object[DEFAULT_POOL_SIZE];

    /**
     * Holds the current pool's index.
     */
    private int _index;

    /**
     * Holds the current pool's length.
     */
    private int _length;

    /**
     * Indicates if object cleanup is enabled (default <code>true</code>).
     */
    private boolean _isCleanupEnabled = true;

    /**
     * Holds this pool's factory.
     */
    private final ObjectFactory _factory;

    /**
     * Creates a pool for the specified factory.
     * 
     * @param factory the factory for this pool.
     */
    DefaultPool(ObjectFactory factory) {
        _factory = factory;
    }

    // Implements ObjectPool abstract method.
    public Object next() {
        return (_index > 0) ? _objects[--_index] : allocate();
    }

    private Object allocate() {
        Object obj = _factory.create();
        if (_length >= _objects.length) { // Resizes.
            Object[] tmp = new Object[_length * 2];
            System.arraycopy(_objects, 0, tmp, 0, _length);
            _objects = tmp;
        }
        _objects[_length++] = obj;
        return obj;
    }

    // Implements ObjectPool abstract method.
    public void recycle(Object obj) {
        int i = indexOf(obj);
        if (i >= 0) {
            _objects[i] = _objects[_index];
            _objects[_index++] = obj;
            if (_isCleanupEnabled) {
                cleanup(obj);
            }
        } else {
            throw new IllegalArgumentException("obj: Object not in the pool");
        }
    }

    // Implements ObjectPool abstract method.
    protected void recycleAll() {
        // Cleanup used objects if cleanup enabled.
        for (int i = _index; _isCleanupEnabled && (i < _length);) {
            cleanup(_objects[i++]);
        }
        // Resets pointer.
        _index = _length;
    }

    // Implements ObjectPool abstract method.
    protected void clearAll() {
        _objects = new Object[DEFAULT_POOL_SIZE];
        _index = 0;
        _length = 0;
    }

    /**
     * Searches for the specified object in this pool.
     *
     * @param  obj the object to search for.
     * @return the index of this object or <code>-1</code> if not found.
     */
    private int indexOf(Object obj) {
        for (int i = _index; i < _length; i++) {
            if (_objects[i] == obj) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Attempts to clean-up this object. If the attempt fails, object cleaning
     * is disabled.
     *
     * @param  obj the object to cleanup.
     */
    private void cleanup(Object obj) {
        try {
            _factory.cleanup(obj);
        } catch (UnsupportedOperationException ex) {
            _isCleanupEnabled = false;
        }
    }
}