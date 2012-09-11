/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_NO_SUCH_ELEMENT_EXCEPTION_HPP
#define _JAVOLUTION_UTIL_NO_SUCH_ELEMENT_EXCEPTION_HPP

#include "javolution/lang/RuntimeException.hpp"

namespace javolution {
    namespace util {
        class NoSuchElementException_API;
        class NoSuchElementException : public javolution::lang::RuntimeException { 
        public:
            NoSuchElementException(Type::NullHandle = Type::Null) : javolution::lang::RuntimeException() {} // Null
            NoSuchElementException(NoSuchElementException_API* ptr) : javolution::lang::RuntimeException((javolution::lang::RuntimeException_API*)ptr) {}
        };
    }
}

/**
 * Thrown to indicate that there are no more elements in a collection.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/NoSuchElementException.html">
 *       Java - NoSuchElementException</a>
 * @version 1.0
 */
class javolution::util::NoSuchElementException_API : public javolution::lang::RuntimeException_API {
protected:

    NoSuchElementException_API(javolution::lang::String message) :
        javolution::lang::RuntimeException_API(message) {
    };

public:

    /**
     * Returns the runtime exception having the specified message.
     *
     * @param message the exception message.
     */
    static NoSuchElementException newInstance(javolution::lang::String message = Type::Null) {
        return new NoSuchElementException_API(message);
    }

};

#endif
