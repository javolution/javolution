/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_LIST_ITERATOR_HPP
#define _JAVOLUTION_UTIL_LIST_ITERATOR_HPP

#include "javolution/util/Iterator.hpp"

namespace javolution {
    namespace util {
        template<class E>
        class ListIterator_API;

        // Defines ListIterator<E> handler alias.
        template<class E> class ListIterator : public Type::Handle<ListIterator_API<E> > {
        public:
            ListIterator(Type::NullHandle = Type::Null) : Type::Handle < ListIterator_API<E> >() {} // Null
            template<class Y> ListIterator(Type::Handle<Y> const & r) : Type::Handle<ListIterator_API<E> > (r) {}
            ListIterator(ListIterator_API<E>* ptr) : Type::Handle < ListIterator_API<E> > (ptr) {}
        };
    }
}

/**
 * This class represents an iterator for lists that allows the programmer to
 * traverse the list in either direction, modify the list during iteration,
 * and obtain the iterator's current position in the list.
 * A ListIterator has no current element; its cursor position always lies
 * between the element that would be returned by a call to previous() and the
 * element that would be returned by a call to next().
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/ListIterator.html">
 *       Java - ListIterator</a>
 * @version 1.0
 */
template <class E> class javolution::util::ListIterator_API : public virtual javolution::util::Iterator_API<E> {
public:

    /**
     * Inserts the specified element into the list (optional operation).
     */
    virtual void add(E const& e) = 0;

    /**
     * Returns <code>true</code> if this list iterator has more elements when
     * traversing the list in the reverse direction.
     */
    virtual Type::boolean hasPrevious() const = 0;

    /**
     * Returns the previous element in the list.
     */
    virtual E const& previous() = 0;

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <code>next</code>.
     * Returns the list size if the list iterator is at the end of the list.
     */
    virtual Type::int32 nextIndex() const = 0;

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to <code>previous</code>.
     * Returns <code>-1</code> if the list iterator is at the beginning of the list.
     */
    virtual Type::int32 previousIndex() const = 0;

    /**
     * Replaces the last element returned by <code>next</code> or
     * <code>previous</code> with the specified element (optional operation).
     */
    virtual void set(E const& e) = 0;

};

#endif
