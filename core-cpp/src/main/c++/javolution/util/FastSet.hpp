/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_UTIL_FAST_SET_HPP
#define _JAVOLUTION_UTIL_FAST_SET_HPP

#include "javolution/util/AbstractCollection.hpp"
#include "javolution/util/AbstractSet.hpp"
#include "javolution/util/FastMap.hpp"

namespace javolution {
    namespace util {
        template<class E>class FastSet_API;

        // Defines FastSet<T> handler alias.

        template<class E> class FastSet : public Type::Handle<FastSet_API<E> > {
        public:

            FastSet(Type::NullHandle = Type::Null) : Type::Handle<FastSet_API<E> >() {
            } // Null

            template<class Y> FastSet(Type::Handle<Y> const & r) : Type::Handle<FastSet_API<E> >(r) {
            }

            FastSet(FastSet_API<E>* ptr) : Type::Handle<FastSet_API<E> >(ptr) {
            }
        };

    }
}

/**
 * This class represents a Set backed by a hash map.
 *
 * Note: Unlike standard <code>java.util.FastSet</code>, null values are not supported.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/FastSet.html">
 *       Java - FastSet</a>
 * @version 1.0
 */
template<class E>
class javolution::util::FastSet_API : public javolution::util::AbstractSet_API<E> {
    FastMap<E, E> map;

public:

    /**
     * Default constructor.
     */
    FastSet_API() {
        map = new FastMap_API<E, E > ();
    }

    /**
     * Returns a new empty instance of this class.
     */
    static FastSet<E> newInstance() {
        return new FastSet_API();
    }

    ////////////////////////
    // Collection Methods //
    ////////////////////////

    /**
     * Ensures that this collection contains the specified element (optional operation).
     *
     * @throws NullPointerException if the key is null.
     */
    virtual Type::boolean add(E const& element) {
        return map->put(element, element) == Type::Null;
    }

    /**
     * Returns an iterator over the elements in this set.
     */
    virtual Iterator<E> iterator() const {
        return map->keySet()->iterator();
    }

    /**
     *  Returns the number of elements in this collection.
     */
    virtual Type::int32 size() const {
        return map->size();
    }

    virtual Type::Mutex& getMutex() const {
        return map->getMutex();
    }

    // Optimization.

    virtual void clear() {
        map->clear();
    }

    // Optimization.

    virtual Type::boolean contains(E const& value) const {
        return map->containsKey(value); // Optimization.
    }

    // Optimization.

    virtual Type::boolean remove(E const& element) {
        if (element != Type::Null)
            return map->remove(element) != Type::Null;
        // Special case for null key.
        Type::boolean hasNull = map->containsKey(Type::Null);
        if (!hasNull) return false;
        map->remove(Type::Null);
        return true;
    }

};

#endif
