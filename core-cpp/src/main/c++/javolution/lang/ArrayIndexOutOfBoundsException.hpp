/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_HPP
#define _JAVOLUTION_LANG_ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_HPP

#include "javolution/lang/IndexOutOfBoundsException.hpp"

namespace javolution {
    namespace lang {
        class ArrayIndexOutOfBoundsException_API;
        class ArrayIndexOutOfBoundsException : public IndexOutOfBoundsException {
        public:
            ArrayIndexOutOfBoundsException(Type::NullHandle = Type::Null) : IndexOutOfBoundsException() {} // Null
            ArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException_API* ptr) : IndexOutOfBoundsException((IndexOutOfBoundsException_API*)ptr) {}
        };
    }
}

/**
 * Thrown to indicate that an array has been accessed with an illegal index.
 * The index is either negative or greater than or equal to the size of the array.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/ArrayIndexOutOfBoundsException.html">
 *       Java - ArrayIndexOutOfBoundsException</a>
 * @version 1.0
 */
class javolution::lang::ArrayIndexOutOfBoundsException_API : public virtual javolution::lang::IndexOutOfBoundsException_API {
protected:

    ArrayIndexOutOfBoundsException_API(String const& message) :
        IndexOutOfBoundsException_API(message) {
    };

public:

    /**
     * Returns the runtime exception having the specified message.
     *
     * @param message the exception message.
     */
    static ArrayIndexOutOfBoundsException newInstance(String const& message = Type::Null) {
        return new ArrayIndexOutOfBoundsException_API(message);
    }

};

#endif
