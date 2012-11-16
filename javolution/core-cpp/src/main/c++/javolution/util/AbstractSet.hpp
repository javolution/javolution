/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_ABSTRACT_SET_HPP
#define _JAVOLUTION_UTIL_ABSTRACT_SET_HPP

#include "javolution/util/AbstractCollection.hpp"
#include "javolution/util/Set.hpp"

namespace javolution {
    namespace util {
        template<class E>class AbstractSet_API;

        // Defines AbstractSet<T> handler alias.
        template<class E> class AbstractSet : public Type::Handle<AbstractSet_API<E> > {
        public:
            AbstractSet(Type::NullHandle = Type::Null) : Type::Handle<AbstractSet_API<E> >() {} // Null
            template<class Y> AbstractSet(Type::Handle<Y> const & r) : Type::Handle<AbstractSet_API<E> >(r) {}
            AbstractSet(AbstractSet_API<E>* ptr) : Type::Handle<AbstractSet_API<E> >(ptr) {}
        };
    }
}

/**
 * This class represents a skeletal implementation of the Set interface to
 * minimize the effort required to implement this interface.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/AbstractSet.html">
 *       Java - AbstractSet</a>
 * @version 1.0
 */
template<class E>
class javolution::util::AbstractSet_API : public javolution::util::AbstractCollection_API<E>, public virtual javolution::util::Set_API<E> {
protected:

    /**
     * Default constructor.
     */
    AbstractSet_API() {
    }

public:

    // Overrides methods common to AbstractCollection and Set to remove ambiguity.
    // (e.g. Visual C++ Warning C4250)

    virtual Type::boolean add(E const& element) {
        return AbstractCollection_API<E>::add(element);
    }

    virtual Type::boolean addAll(javolution::util::Collection<E> const& c) {
        return AbstractCollection_API<E>::addAll(c);
    }

    virtual void clear() {
        AbstractCollection_API<E>::clear();
    }

    virtual Type::boolean contains(E const& value) const {
        return AbstractCollection_API<E>::contains(value);
    }

    virtual Type::boolean containsAll(javolution::util::Collection<E> const& c) const {
        return AbstractCollection_API<E>::containsAll(c);
    }

    virtual Type::boolean isEmpty() const {
        return AbstractCollection_API<E>::isEmpty();
    }

    virtual javolution::util::Iterator<E> iterator() const {
        return AbstractCollection_API<E>::iterator();
    }

    virtual Type::boolean remove(E const& element) {
        return AbstractCollection_API<E>::remove(element);
    }

    virtual Type::boolean removeAll(javolution::util::Collection<E> const& c) {
        return AbstractCollection_API<E>::removeAll(c);
    }

    virtual Type::boolean retainAll(javolution::util::Collection<E> const& c) {
        return AbstractCollection_API<E>::retainAll(c);
    }

    virtual Type::int32 size() const {
        return AbstractCollection_API<E>::size();
    }

    virtual Type::Array<javolution::lang::Object> toArray() const {
        return AbstractCollection_API<E>::toArray();
    }

    virtual Type::Array<E> toArray(Type::Array<E> const& result) const {
        return AbstractCollection_API<E>::toArray(result);
    }

    virtual Type::boolean equals(javolution::lang::Object const& obj) const {
        return AbstractCollection_API<E>::equals(obj);
    }

    virtual Type::int32 hashCode() const {
        return AbstractCollection_API<E>::hashCode();
    }

    virtual javolution::lang::String toString() const {
        return AbstractCollection_API<E>::toString();
    }
};
#endif
