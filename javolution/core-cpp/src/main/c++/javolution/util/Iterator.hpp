/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_ITERATOR_HPP
#define _JAVOLUTION_UTIL_ITERATOR_HPP

#include "javolution/lang/Object.hpp"

namespace javolution {
    namespace util {
        template<class E>
        class Iterator_API;

        // Defines Iterator<E> handler alias.
        template<class E> class Iterator : public Type::Handle<Iterator_API<E> > {
        public:
            Iterator(Type::NullHandle = Type::Null) : Type::Handle < Iterator_API<E> >() {} // Null
            template<class Y> Iterator(Type::Handle<Y> const & r) : Type::Handle<Iterator_API<E> > (r) {}
            Iterator(Iterator_API<E>* ptr) : Type::Handle < Iterator_API<E> > (ptr) {}
        };

    }
}

/**
 * This class represents an iterator over a collection.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/Iterator.html">
 *       Java - Iterator</a>
 * @version 1.0
 */
template <class E> class javolution::util::Iterator_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Returns <code>true</code> if the iteration has more elements.
     */
    virtual Type::boolean hasNext() const = 0;

    /**
     * Returns the next element in the iteration.
     */
    virtual E const& next() = 0;

    /**
     * Removes from the underlying collection the last element returned by the iterator
     * (optional operation).
     */
    virtual void remove() = 0;

};

#endif
