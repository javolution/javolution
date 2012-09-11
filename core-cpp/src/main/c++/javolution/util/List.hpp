/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_LIST_HPP
#define _JAVOLUTION_UTIL_LIST_HPP

#include "javolution/util/Collection.hpp"
#include "javolution/util/ListIterator.hpp"

namespace javolution {
    namespace util {
        template<class E>
        class List_API;

        // Defines List<E> handler alias.
        template<class E> class List : public Type::Handle<List_API<E> > {
        public:
            List(Type::NullHandle = Type::Null) : Type::Handle < List_API<E> >() {} // Null
            template<class Y > List(Type::Handle<Y> const & r) : Type::Handle<List_API<E> > (r) {}
            List(List_API<E>* ptr) : Type::Handle < List_API<E> > (ptr) {}
        };

    }
}

/**
 * This class represents n ordered collection (also known as a sequence).
 * The user of this interface has precise control over where in the list
 * each element is inserted. The user can access elements by their integer
 * index (position in the list), and search for elements in the list.
 *
 * For example:<code><pre>
 *    List<String> list = FastTable_API<String>::newInstance();
 *    list->add(String_API::valueOf("first"));
 *    list->add(String_API::valueOf("second"));
 *    list->add(String_API::valueOf("third"));
 *    list->add(Type::Null);
 *    std::wcout << list << std::endl;
 *
 *    >> [first, second, third, null]
 * </pre></code>
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/List.html">
 *       Java - List</a>
 * @version 1.0
 */
template <class E> class javolution::util::List_API : public virtual javolution::util::Collection_API<E> {
public:

    // Methods from Collection hidden by current methods should be 'unhidden' 
    using javolution::util::Collection_API<E>::add; // add(E) hidden.
    using javolution::util::Collection_API<E>::addAll; // addAll(Collection) hidden.
    using javolution::util::Collection_API<E>::remove; // remove(E) hidden.


    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).
     */
    virtual void add(Type::int32 index, E const& element) = 0;

    /**
     * Inserts all of the elements in the specified collection into this list
     * at the specified position (optional operation).
     */
    virtual Type::boolean addAll(Type::int32 index, javolution::util::Collection<E> c) = 0;

    /**
     * Returns the element at the specified position in this list.
     */
    virtual E const& get(Type::int32 index) const = 0;

    /**
     * Replaces the element at the specified position in this list with 
     * the specified element (optional operation).
     */
    virtual E set(Type::int32 index, E const& element) = 0;
    
    /**
     * Returns a list iterator of the elements in this list (in proper sequence).
     */
    virtual ListIterator<E> listIterator() const = 0;

    /**
     * Returns a list iterator of the elements in this list (in proper sequence),
     * starting at the specified position in this list.
     */
    virtual ListIterator<E> listIterator(Type::int32 index) const = 0;

    /**
     * Removes the first occurrence in this list of the specified element 
     * (optional operation).
     */
    virtual E remove(Type::int32 index) = 0;

    /**
     * Returns the index in this list of the first occurrence of the specified element,
     * or <code>-1</code> if this list does not contain this element.
     */
     virtual Type::int32 indexOf(javolution::lang::Object o) const = 0;

    /**
     * Returns the index in this list of the last occurrence of the specified element,
     * or <code>-1</code> if this list does not contain this element.
     */
     virtual Type::int32 lastIndexOf(javolution::lang::Object o) const = 0;

     /**
      *  Returns a view of the portion of this list between the specified fromIndex,
      * inclusive, and toIndex, exclusive.
      */
     virtual javolution::util::List<E> subList(Type::int32 fromIndex, Type::int32 toIndex) const = 0;

    /**
     * Compares the specified object with this list for equality.  Returns
     * <code>true</code> if and only if the specified object is also a list,
     * both lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>.
     */
    virtual Type::boolean equals(javolution::lang::Object o) const = 0;

    /**
     * Returns the hash code value for this list. The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  int hashCode = 1;
     *  Iterator&lt;E&gt; i = list->iterator();
     *  while (i->hasNext()) {
     *      E obj = i->next();
     *      hashCode = 31*hashCode + (obj==Type::Null ? 0 : obj->hashCode());
     *  }
     * </pre>
     */
    virtual Type::int32 hashCode() const = 0;

};

#endif
