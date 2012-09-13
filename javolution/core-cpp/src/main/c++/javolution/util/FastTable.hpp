/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_UTIL_FAST_TABLE_HPP
#define _JAVOLUTION_UTIL_FAST_TABLE_HPP

#include "javolution/lang/System.hpp"
#include "javolution/lang/IndexOutOfBoundsException.hpp"
#include "javolution/lang/IllegalStateException.hpp"
#include "javolution/util/NoSuchElementException.hpp"
#include "javolution/util/AbstractList.hpp"

namespace javolution {
    namespace util {
        template<class E>class FastTable_API;
        template<class E>class FastTableIterator_API;
        template<class E>class ArraySubList_API;

        // Defines FastTable<E> handler alias.
        template<class E> class FastTable : public Type::Handle<FastTable_API<E> > {
        public:
            FastTable(Type::NullHandle = Type::Null) : Type::Handle<FastTable_API<E> >() {} // Null
            template<class Y> FastTable(Type::Handle<Y> const & r) : Type::Handle<FastTable_API<E> >(r) {}
            FastTable(FastTable_API<E>* ptr) : Type::Handle<FastTable_API<E> >(ptr) {}
        };
    }
}

/**
 * This class represents a resizable-array implementation of the list interface.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/FastTable.html">
 *       Java - FastTable</a>
 * @version 1.0
 */
template<class E>
class javolution::util::FastTable_API : public javolution::util::AbstractList_API<E> {
    friend class FastTableIterator_API<E>;

    /**
     * Holds list elements.
     */
    Type::Array<E> elements;

    /**
     * Holds list current size.
     */
    Type::int32 count;

    /**
     * Holds this list mutex.
     */
    Type::Mutex mutex;

public:

    /**
     * Default constructor.
     */
    FastTable_API() : count(0), mutex(), elements(Type::Array<E>(16)) {
    }

    // Methods from AbstractList hidden by current methods should be 'unhidden' 
    using javolution::util::AbstractList_API<E>::addAll; // addAll(Collection) hidden.
    using javolution::util::AbstractList_API<E>::remove; // remove(E) hidden.

    /**
     * Returns a new empty instance of this class.
     */
    static FastTable<E> newInstance() {
        return new FastTable_API();
    }

    ////////////////////////
    // Collection Methods //
    ////////////////////////

    /**
     * Ensures that this collection contains the specified element (optional operation).
     */
    virtual Type::boolean add(E const& element) {
        if (count >= elements.length) resize();
        elements[count++] = element;
        return true;
    }

    /**
     * Returns an iterator over the elements in this list.
     * The iterator returned should not outlive the list it iterates over.
     */
    virtual Iterator<E> iterator() const {
        return listIterator();
    }

    /**
     *  Returns the number of elements in this collection.
     */
    virtual Type::int32 size() const {
        return count;
    }

    // Overrides (optimization).
    virtual void clear() {
    	for (int i=0; i < count;) {
    		elements[i++] = Type::Null;
    	}
        count = 0;
    }

    //////////////////
    // List Methods //
    //////////////////

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).
     */
    virtual void add(Type::int32 index, E const& element) {
        if ((index < 0) || (index > count))
            throwIndexOutOfBoundsException(index);
        shiftRight(index, 1);
        elements[index] = element;
        count++;
    }

    /**
     * Inserts all of the elements in the specified collection into this list
     * at the specified position (optional operation).
     */
    virtual Type::boolean addAll(Type::int32 index, Collection<E> c) {
        if ((index < 0) || (index > count))
            throwIndexOutOfBoundsException(index);
        Type::int32 shift = c->size();
        shiftRight(index, shift);
        Iterator <E> cIterator = c->iterator();
        for (Type::int32 i = index, n = index + shift; i < n; i++) {
            elements[i] = cIterator->next();
        }
        count += shift;
        return shift != 0;
    }

    /**
     * Returns the element at the specified position in this list.
     */
    virtual E const& get(Type::int32 index) const {
        if (index >= count) throwIndexOutOfBoundsException(index);
        return elements[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element (optional operation).
     */
    virtual E set(Type::int32 index, E const& element) {
        if (index >= count) throwIndexOutOfBoundsException(index);
        E previous = elements[index];
        elements[index] = element;
        return previous;
    }

    /**
     * Returns a list iterator of the elements in this list (in proper sequence).
     * The iterator returned should not outlive the list it iterates over.
     */
    virtual ListIterator<E> listIterator() const {
        return listIterator(0);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper sequence),
     * starting at the specified position in this list.
     * The iterator returned should not outlive the list it iterates over.
     */
    virtual ListIterator<E> listIterator(Type::int32 index) const {
        return new FastTableIterator_API<E> (
        		const_cast<FastTable_API<E>*>(this), index, count);
    }

    /**
     * Removes the first occurrence in this list of the specified element
     * (optional operation).
     */
    virtual E remove(Type::int32 index) {
        E previous = get(index);
        shiftLeft(index + 1, 1);
        count--;
        elements[count] = Type::Null; // Deallocates.
        return previous;
    }

    /**
     * Returns the index in this list of the first occurrence of the specified element,
     * or <code>-1</code> if this list does not contain this element.
     */
    virtual Type::int32 indexOf(javolution::lang::Object o) const {
        if (o == Type::Null) {
            for (Type::int32 i = 0; i < count; i++) {
                if (elements[i] == Type::Null)
                    return i;
            }
        } else {
            for (Type::int32 i = 0; i < count; i++) {
                if (o->equals(elements[i]))
                    return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index in this list of the last occurrence of the specified element,
     * or <code>-1</code> if this list does not contain this element.
     */
    virtual Type::int32 lastIndexOf(javolution::lang::Object o) const {
        if (o == Type::Null) {
            for (Type::int32 i = count - 1; i >= 0; i--) {
                if (elements[i] == Type::Null)
                    return i;
            }
        } else {
            for (Type::int32 i = count - 1; i >= 0; i--) {
                if (o->equals(elements[i]))
                    return i;
            }
        }
        return -1;
    }

    /**
     * Returns a temporary view of the portion of this list between the
     * specified fromIndex, inclusive, and toIndex, exclusive.
     * This view returned should not outlive its parent list.
     */
    virtual List<E> subList(Type::int32 fromIndex, Type::int32 toIndex) const {
        if ((fromIndex < 0) || (toIndex > count) || (fromIndex > toIndex))
            throwIndexOutOfBoundsException(fromIndex, toIndex);
        return new ArraySubList_API<E>(
		    const_cast<FastTable_API<E>*>(this), fromIndex, toIndex);
    }

    /**
     * Returns the mutex to perform synchronized access to this collection.
     */
    virtual Type::Mutex& getMutex() const {
        return const_cast<FastTable_API<E>*>(this)->mutex;
    }

private:

    // Resizes the array holding the list elements.
    void resize() {
        Type::int32 newCapacity = elements.length * 2; // Double capacity.
        Type::Array<E> newElements = Type::Array<E>(newCapacity);
        javolution::lang::System_API::arraycopy(elements, 0, newElements, 0, elements.length);
        elements = newElements;
    }

    // Shifts element from the specified index to the right (higher indexes).
    void shiftRight(Type::int32 index, Type::int32 shift) {
        while (count + shift >= elements.length) {
            resize();
        }
        for (Type::int32 i = count; --i >= index;) {
            Type::int32 dest = i + shift;
            elements[dest] = elements[i];
        }
    }

    // Shifts element from the specified index to the left (lower indexes).

    void shiftLeft(Type::int32 index, Type::int32 shift) {
        for (Type::int32 i = index; i < count; i++) {
            Type::int32 dest = i - shift;
            elements[dest] = elements[i];
        }
    }

    void throwIndexOutOfBoundsException(Type::int32 index) const {
        throw javolution::lang::IndexOutOfBoundsException_API::newInstance(
                javolution::lang::StringBuilder_API::newInstance()
                ->append(L"Index: ")
                ->append(index)
                ->append(L", List size: ")
                ->append(size())->toString());
    }

    void throwIndexOutOfBoundsException(Type::int32 fromIndex, Type::int32 toIndex) const {
        throw javolution::lang::IndexOutOfBoundsException_API::newInstance(
                javolution::lang::StringBuilder_API::newInstance()
                ->append(L"FromIndex: ")
                ->append(fromIndex)
                ->append(L", ToIndex: ")
                ->append(toIndex)
                ->append(L", List size: ")
                ->append(size())->toString());
    }
};

template<class E>
class javolution::util::FastTableIterator_API : public virtual javolution::util::ListIterator_API<E> {

	Type::Handle<FastTable_API<E> > list;
    Type::int32 currentIndex;
    Type::int32 start; // Inclusive.
    Type::int32 end; // Exclusive.
    Type::int32 nIndex;

public:

    FastTableIterator_API(FastTable<E> const& lst, Type::int32 startIndex, Type::int32 endIndex) {
    	this->list = lst;
    	this->start = startIndex;
    	this->end = endIndex;
    	this->nIndex = startIndex;
    	this->currentIndex = -1;
    }

    Type::boolean hasNext() const {
        return (nIndex != end);
    }

    E const& next() {
        if (nIndex == end) throw NoSuchElementException_API::newInstance(L"iterator->next()");
        Type::int32 i = currentIndex = nIndex++;
        return list->elements[i];
    }

    Type::int32 nextIndex() const {
        return nIndex;
    }

    Type::boolean hasPrevious() const {
        return nIndex != start;
    }

    E const& previous() {
        if (nIndex == start) throw NoSuchElementException_API::newInstance(L"iterator->previous()");
        Type::int32 i = currentIndex = --nIndex;
        return list->elements[i];
    }

    Type::int32 previousIndex() const {
        return nIndex - 1;
    }

    void add(E const& o) {
        list->add(nIndex++, o);
        end++;
        currentIndex = -1;
    }

    void set(E const& o) {
        if (currentIndex < 0)
            throw javolution::lang::IllegalStateException_API::newInstance(L"iterator->set(E)");
        list->set(currentIndex, o);
    }

    void remove() {
        if (currentIndex < 0) throw javolution::lang::IllegalStateException_API::newInstance(L"iterator->remove()");
        list->remove(currentIndex);
        end--;
        if (currentIndex < nIndex) {
            nIndex--;
        }
        currentIndex = -1;
    }

};

template<class E>
class javolution::util::ArraySubList_API : public javolution::util::AbstractList_API<E> {

    FastTable<E> source;
    Type::int32 offset;
    Type::int32 count;

public:

    ArraySubList_API(FastTable<E> const& src, Type::int32 fromIndex, Type::int32 toIndex) {
        this->source = src;
        this->offset = fromIndex;
        this->count = toIndex - fromIndex;
    }

    // Methods from Collection hidden by current methods should be 'unhidden' 
    using javolution::util::Collection_API<E>::addAll;

    /////////////////////////
    // Collection  methods //
    /////////////////////////

    Iterator<E> iterator() const {
        return listIterator();
    }

    Type::int32 size() const {
        return count;
    }

    //////////////////
    // List methods //
    //////////////////

    E const& get(Type::int32 index) const {
        return source->get(index + offset);
    }

    Type::int32 indexOf(javolution::lang::Object o) const {
        if (o == Type::Null) {
            for (Type::int32 i = 0; i < count; i++) {
                if (get(i) == Type::Null)
                    return i;
            }
        } else {
            for (Type::int32 i = 0; i < count; i++) {
                if (o->equals(get(i)))
                    return i;
            }
        }
        return -1;
    }

    Type::int32 lastIndexOf(javolution::lang::Object o) const {
        if (o == Type::Null) {
            for (Type::int32 i = count - 1; i >= 0; i--) {
                if (get(i) == Type::Null)
                    return i;
            }
        } else {
            for (Type::int32 i = count - 1; i >= 0; i--) {
                if (o->equals(get(i)))
                    return i;
            }
        }
        return -1;
    }

    ListIterator<E> listIterator() const {
        return listIterator(0);
    }

    ListIterator<E> listIterator(Type::int32 index) const {
        return new FastTableIterator_API<E > (source, offset + index, offset + count);
    }

    List<E> subList(Type::int32 fromIndex, Type::int32 toIndex) const {
        return new ArraySubList_API<E>(source, offset + fromIndex, offset + toIndex - fromIndex);
    }

    /////////////////////////////////////////
    // SubLists are not modifiable (views) //
    /////////////////////////////////////////

    Type::boolean add(E const&) {
        throwUnsupportedOperationException();
        return false;
    }

    void add(Type::int32, E const&) {
        throwUnsupportedOperationException();
    }

    E set(Type::int32, E const&) {
        throwUnsupportedOperationException();
        return Type::Null;
    }

    E remove(Type::int32) {
        throwUnsupportedOperationException();
        return Type::Null;
    }

    Type::boolean remove(E const&) {
        throwUnsupportedOperationException();
        return false;
    }

    Type::boolean addAll(Type::int32, Collection<E>) {
        throwUnsupportedOperationException();
        return false;
    }

    virtual Type::Mutex& getMutex() const {
        return source->getMutex();
    }

private:

    // Unfortunately, some compilers (e.g. CC)  generates warning if throw with no return value.
    void throwUnsupportedOperationException() const {
        if (this) throw javolution::lang::UnsupportedOperationException_API::newInstance(
                L"SubList view is not modifiable");
    }

};

#endif
