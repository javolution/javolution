/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_INDEX_OUT_OF_BOUNDS_EXCEPTION_HPP
#define _JAVOLUTION_LANG_INDEX_OUT_OF_BOUNDS_EXCEPTION_HPP

#include "javolution/lang/RuntimeException.hpp"

namespace javolution {
    namespace lang {
        class IndexOutOfBoundsException_API;
        class IndexOutOfBoundsException : public RuntimeException { 
        public:
            IndexOutOfBoundsException(Type::NullHandle = Type::Null) : RuntimeException() {} // Null
            IndexOutOfBoundsException(IndexOutOfBoundsException_API* ptr) : RuntimeException((RuntimeException_API*)ptr) {}
        };
    }   
}

/**
 * Thrown to inddicate that an index of some sort (such as to an array,
 * to a string, or to a vector) is out of range.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/IndexOutOfBoundsException.html">
 *       Java - IndexOutOfBoundsException</a>
 * @version 1.0
 */
class javolution::lang::IndexOutOfBoundsException_API : public javolution::lang::RuntimeException_API {
protected:

    IndexOutOfBoundsException_API(String message) :
        RuntimeException_API(message) {
    };

public:

    /**
     * Returns the runtime exception having the specified message.
     *
     * @param message the exception message.
     */
    static IndexOutOfBoundsException newInstance(String message = Type::Null) {
        return new IndexOutOfBoundsException_API(message);
    }

};
#endif
