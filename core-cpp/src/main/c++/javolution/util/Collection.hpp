/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_COLLECTION_HPP
#define _JAVOLUTION_UTIL_COLLECTION_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/UnsupportedOperationException.hpp" // Raised by optional operations not supported.
#include "javolution/util/Iterator.hpp"

namespace javolution {
    namespace util {
        template<class E>
        class Collection_API;

        // Defines Collection<E> handler alias.
        template<class E> class Collection : public Type::Handle<Collection_API<E> > {
        public:
            Collection(Type::NullHandle = Type::Null) : Type::Handle < Collection_API<E> >() {} // Null
            template<class Y> Collection(Type::Handle<Y> const & r) : Type::Handle<Collection_API<E> > (r) {}
            Collection(Collection_API<E>* ptr) : Type::Handle < Collection_API<E> > (ptr) {}
        };

    }
}

/**
 * This class represents the root interface in the collection hierarchy.
 * A collection represents a group of objects, known as its elements.
 * Some collections allow duplicate elements and others do not.
 * Some are ordered and others unordered.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/Collection.html">
 *       Java - Collection</a>
 * @version 1.0
 */
template <class E> class javolution::util::Collection_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Ensures that this collection contains the specified element (optional operation).
     */
    virtual Type::boolean add(E const& element) = 0;

    /**
     * Adds all of the elements in the specified collection to this collection (optional operation).
     */
    virtual Type::boolean addAll(javolution::util::Collection<E> c) = 0;

    /**
     * Removes all of the elements from this collection (optional operation).
     */
    virtual void clear() = 0;

    /**
     * Returns true if this collection contains the specified element.
     */
    virtual Type::boolean contains(E const& value) const = 0;

    /**
     * Returns true if this collection contains all of the elements in the specified collection.
     */
    virtual Type::boolean containsAll(javolution::util::Collection<E> c) const = 0;

    /**
     * Compares the specified object with this collection for equality.
     * If this collection is a list then the specified object must be
     * a list with the element in the same order.
     */
    virtual Type::boolean equals(javolution::lang::Object o) const = 0;
    
    /**
     * Returns the hash code value for this collection (consistent with
     * equals).
     */
    virtual Type::int32 hashCode() const = 0;

    /**
     * Returns true if this collection contains no elements.
     */
    virtual Type::boolean isEmpty() const = 0;

    /**
     * Returns an iterator over the elements in this collection.
     */
    virtual javolution::util::Iterator<E> iterator() const = 0;

    /**
     * Removes a single instance of the specified element from this collection,
     * if it is present (optional operation).
     */
    virtual Type::boolean remove(E const& element) = 0;

    /**
     * Removes all this collection's elements that are also contained in the
     *  specified collection (optional operation).
     */
    virtual Type::boolean removeAll(javolution::util::Collection<E> c) = 0;

    /**
     *  Retains only the elements in this collection that are contained in the
     *  specified collection (optional operation).
     */
    virtual Type::boolean retainAll(javolution::util::Collection<E> c) = 0;

    /**
     *  Returns the number of elements in this collection.
     */
    virtual Type::int32 size() const = 0;

    /**
     * Returns an array containing all of the elements in this collection.
     */
    virtual Type::Array<javolution::lang::Object> toArray() const = 0;

    /**
     * Returns an array containing all of the elements in this collection;
     * the runtime type of the returned array is that of the specified array.
     */
    virtual Type::Array<E> toArray(Type::Array<E> array) const = 0;

};

#endif
