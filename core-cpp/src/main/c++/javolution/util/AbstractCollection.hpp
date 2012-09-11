/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_UTIL_ABSTRACT_COLLECTION_HPP
#define _JAVOLUTION_UTIL_ABSTRACT_COLLECTION_HPP

#include "javolution/util/Collection.hpp"
#include "javolution/lang/StringBuilder.hpp"

namespace javolution {
    namespace util {
        template<class E>class AbstractCollection_API;
        template<class E>class List_API; // Forwards reference.

        // Defines AbstractCollection<E> handler alias.
        template<class E> class AbstractCollection : public Type::Handle<AbstractCollection_API<E> > {
        public:
            AbstractCollection(Type::NullHandle = Type::Null) : Type::Handle<AbstractCollection_API<E> >() {
            } // Null
            template<class Y> AbstractCollection(Type::Handle<Y> const & r) : Type::Handle<AbstractCollection_API<E> >(r) {
            }
            AbstractCollection(AbstractCollection_API<E>* ptr) : Type::Handle<AbstractCollection_API<E> >(ptr) {
            }
        };
    }
}

/**
 * This class provides a skeletal implementation of the Collection interface,
 * to minimize the effort required to implement this interface.
 *
 * To implement an unmodifiable collection, the programmer needs only to extend
 * this class and provide implementations for the iterator and size methods.
 * (The iterator returned by the iterator() method must implement hasNext and next.)
 *
 * To implement a modifiable collection, the programmer must additionally override
 * this class's add method (which otherwise throws an UnsupportedOperationException),
 * and the iterator returned by the iterator method must additionally implement
 * its remove method.
 *
 * The programmer should generally provide a void (no argument) and Collection
 * constructor, as per the recommendation in the Collection interface specification.
 *
 * The documentation for each non-abstract methods in this class describes its
 * implementation in detail. Each of these methods may be overridden if the collection
 * being implemented admits a more efficient implementation.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/AbstractCollection.html">
 *       Java - AbstractCollection</a>
 * @version 1.0
 */
template<class E>
class javolution::util::AbstractCollection_API : public virtual javolution::util::Collection_API<E> {
protected:

    /**
     * Default constructor.
     */
    AbstractCollection_API() {
    };

public:

    // Overrides.
    virtual Type::boolean add(E const&) {
        throwUnsupportedOperationException();
        return false;
    }

    // Overrides.
    virtual Type::boolean addAll(javolution::util::Collection<E> c) {
        Type::boolean modified = false;
        javolution::util::Iterator<E> i = c->iterator();
        while (i->hasNext()) {
            if (add(i->next())) {
                modified = true;
            }
        }
        return modified;
    }

    // Overrides.
    virtual void clear() {
        javolution::util::Iterator<E> i = this->iterator();
        while (i->hasNext()) {
            i->next();
            i->remove();
        }
    }

    // Overrides.
    virtual Type::boolean contains(E const& value) const {
        javolution::util::Iterator<E> i = this->iterator();
        if (value == Type::Null) {
            while (i->hasNext()) {
                if (i->next() == Type::Null)
                    return true;
            }
            return false;
        }
        while (i->hasNext()) {
            if (value->equals(i->next()))
                return true;
        }
        return false;
    }

    // Overrides.
    virtual Type::boolean containsAll(javolution::util::Collection<E> c) const {
        javolution::util::Iterator<E> i = c->iterator();
        while (i->hasNext()) {
            if (!this->contains(i->next()))
                return false;
        }
        return true;
    }

    // Overrides.
    virtual Type::boolean isEmpty() const {
        return this->size() == 0;
    }

    // Overrides.
    virtual javolution::util::Iterator<E> iterator() const {
        throwUnsupportedOperationException();
        return Type::Null;
    }

    // Overrides.
    virtual Type::boolean remove(E const& element) {
        javolution::util::Iterator<E> i = this->iterator();
        if (element == Type::Null) {
            while (i->hasNext()) {
                if (i->next() == Type::Null) {
                    i->remove();
                    return true;
                }
            }
            return false;
        }
        while (i->hasNext()) {
            if (element->equals(i->next())) {
                i->remove();
                return true;
            }
        }
        return false;
    }

    // Overrides.
    virtual Type::boolean removeAll(javolution::util::Collection<E> c) {
        Type::boolean modified = false;
        javolution::util::Iterator<E> i = this->iterator();
        while (i->hasNext()) {
            if (c->contains(i->next())) {
                i->remove();
                modified = true;
            }
        }
        return modified;
    }

    // Overrides.
    virtual Type::boolean retainAll(javolution::util::Collection<E> c) {
        Type::boolean modified = false;
        javolution::util::Iterator<E> i = this->iterator();
        while (i->hasNext()) {
            if (!c->contains(i->next())) {
                i->remove();
                modified = true;
            }
        }
        return modified;
    }

    // Overrides.
    virtual Type::int32 size() const {
        throwUnsupportedOperationException();
        return 0;
    }

    // Overrides.
    virtual Type::Array<javolution::lang::Object> toArray() const {
        Type::int32 n = this->size();
        Type::Array<javolution::lang::Object> result = Type::Array<javolution::lang::Object> (n);
        javolution::util::Iterator<E> i = this->iterator();
        Type::int32 j = 0;
        while (i->hasNext()) {
            result[j++] = i->next();
        }
        return result;
    }

    // Overrides.
    virtual Type::Array<E> toArray(Type::Array<E> result) const {
        if (result.length != this->size())
            throw javolution::lang::UnsupportedOperationException_API::newInstance(
                L"Method toArray(Type::Array<E>) requires array's length == size()");
        javolution::util::Iterator<E> i = this->iterator();
        Type::int32 j = 0;
        while (i->hasNext()) {
            result[j++] = i->next();
        }
        return result;
    }

    // Overrides.
    virtual Type::boolean equals(javolution::lang::Object obj) const {
    	javolution::util::Collection<E> that = Type::dynamic_handle_cast<javolution::util::Collection_API<E> >(obj);
        if (that == Type::Null) return false;
        if (this->size() != that->size()) return false;
        Type::boolean thisIsList = dynamic_cast<const List_API<E>* >(this) != 0;
        Type::boolean thatIsList = Type::dynamic_handle_cast<List_API<E> >(that) != 0;
        if (thisIsList && thatIsList) { // They should have the same order.
            javolution::util::Iterator<E> i = this->iterator();
            javolution::util::Iterator<E> j = that->iterator();
            while (i->hasNext()) {
                E thisElement = i->next();
                E thatElement = j->next();
                if (thisElement == Type::Null) {
                    if (thatElement != Type::Null) return false;
                } else {
                    if (!thisElement->equals(thatElement)) return false;
                }
            }
            return true;
        } else if (!thisIsList && !thatIsList) { // None is a list.
            return containsAll(javolution::util::Collection<E>(that)); // We know they have same size.
        } else { // One of the collection is a list but not the other.
            return false;
        }
    }

    // Overrides.
    virtual Type::int32 hashCode() const {
        Type::boolean isList = dynamic_cast<const List_API<E>* > (this) != 0;
        javolution::util::Iterator<E> i = this->iterator();
        int h = 1;
        while (i->hasNext()) {
            if (isList) {
                h *= 31;
            }
            h += hash(i->next());
        }
        return h;
    }

    // Overrides.
    virtual javolution::lang::String toString() const {
        javolution::lang::StringBuilder sb = javolution::lang::StringBuilder_API::newInstance();
        sb->append(L"[");
        javolution::util::Iterator<E> i = this->iterator();
        while (i->hasNext()) {
            sb->append(i->next());
            if (i->hasNext()) {
                sb->append(L", ");
            }
        }
        sb->append(L"]");
        return sb->toString();
    }

private:

    // Returns the hash code of the specified element.
    static Type::int32 hash(E const& e) {
        javolution::lang::Object_API* obj = e.get();
        return (obj == 0) ? 0 : obj->hashCode();
    }

    // Unfortunately, some compilers (e.g. CC)  generates warning if throw with no return value.
    void throwUnsupportedOperationException() const {
        if (this) throw javolution::lang::UnsupportedOperationException_API::newInstance(
                L"Method should be implemented by concrete collection");
    }
};

#endif
