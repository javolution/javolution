/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_SYSTEM_HPP
#define _JAVOLUTION_LANG_SYSTEM_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/IndexOutOfBoundsException.hpp"
#include <string.h>

namespace javolution {
    namespace lang {
        class System_API;
    }
}

/**
 * The System class contains several useful class fields and methods.
 *
 * @version 2.0
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/System.html">
 *       Java - System</a>
 */
class javolution::lang::System_API {

	/**
     * Utility class, cannot be instantiated.
     */
    System_API() {
    }

public:

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * This method ensures that copy constructors of the array elements
     * are being called.
     *
     * @param src the source array.
     * @param srcPos the starting position in the source array.
     * @param dest the destination array.
     * @param destPos the starting position in the destination data.
     * @param length the number of array elements to be copied.
     * @throws IndexOutOfBoundsException if <code>(srcPos+length &gt; src.length)
     *          || (destPos+length &gt; dest.length)</code>
     */
    template<class E> static void arraycopy(
    		Type::Array<E> src, Type::int32 srcPos,
    		Type::Array<E> dest, Type::int32 destPos, Type::int32 length) {
    	if ( (srcPos+length > src.length) || (destPos+length > dest.length) )
    		throw javolution::lang::IndexOutOfBoundsException_API::newInstance();
    	// We cannot use std::memcpy for non-primitive types (copy constructor not called),
    	for (int i=0; i < length; i++) {
    		dest[i+destPos] = src[i+srcPos];
    	}
    }

    /**
     * Copies an array of primitive types from the specified source array,
     * beginning at the specified position, to the specified position of
     * the destination array. This method should not be used on non-primitive
     * types since the copy constructor of the element is not called.
     *
     * @param src the source array.
     * @param srcPos the starting position in the source array.
     * @param dest the destination array.
     * @param destPos the starting position in the destination data.
     * @param length the number of array elements to be copied.
     */
    template<typename E> static void arraycopy(
    		E* src, Type::int32 srcPos,
    		E* dest, Type::int32 destPos, Type::int32 length) {
        if ((length <= 0) && ((E)length)) return ; // Would not compile if E is not a primitive type.
    	E* s = src + srcPos;
    	E* d = dest + destPos;
    	memcpy(d, s, length * sizeof(E));
    }

};

#endif
