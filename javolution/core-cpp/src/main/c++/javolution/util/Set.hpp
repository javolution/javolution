/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_SET_HPP
#define _JAVOLUTION_UTIL_SET_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/util/Collection.hpp"

namespace javolution {
    namespace util {
        template<class E>
        class Set_API;

        // Defines Set<T> handler alias.
        template<class E> class Set : public Type::Handle<Set_API<E> > {
            public:
            Set(Type::NullHandle = Type::Null) : Type::Handle< Set_API<E> >() {} // Null
            template<class Y > Set(Type::Handle<Y> const & r) : Type::Handle<Set_API<E> > (r) {}
            Set(Set_API<E>* ptr) : Type::Handle < Set_API <E> > (ptr) {}
        };

    }
}

/**
 * This class represents a collection that contains no duplicate elements.
 * More formally, sets contain no pair of elements e1 and e2 such that
 * e1.equals(e2), and at most one null element.
 * As implied by its name, this interface models the mathematical set abstraction.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/Set.html">
 *       Java - Set</a>
 * @version 1.0
 */
template <class E> class javolution::util::Set_API : public virtual javolution::util::Collection_API<E> {
public:

    /**
     * Compares the specified object with this set for equality.  Returns
     * <code>true</code> if the specified object is also a set, the two sets
     * have the same size, and every member of the specified set is
     * contained in this set (or equivalently, every member of this set is
     * contained in the specified set). This definition ensures that the
     * equals method works properly across different implementations of the
     * set interface.
     */
    virtual Type::boolean equals(javolution::lang::Object const& o) const = 0;

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set,
     * where the hash code of a <code>Type::Null</code> element is defined
     * to be zero.
     */
    virtual Type::int32 hashCode() const = 0;
};

#endif
