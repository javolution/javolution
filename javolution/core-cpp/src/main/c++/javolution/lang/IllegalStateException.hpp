/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_ILLEGAL_STATE_EXCEPTION_HPP
#define _JAVOLUTION_LANG_ILLEGAL_STATE_EXCEPTION_HPP

#include "javolution/lang/RuntimeException.hpp"

namespace javolution {
    namespace lang {
        class IllegalStateException_API;
        class IllegalStateException : public RuntimeException { 
        public:
            IllegalStateException(Type::NullHandle = Type::Null) : RuntimeException() {} // Null
            IllegalStateException(IllegalStateException_API* ptr) : RuntimeException((RuntimeException_API*)ptr) {}
        };
     }
}

/**
 * Thrown to signal that a method has been invoked at an illegal or
 * inappropriate time.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/IllegalStateException.html">
 *       Java - IllegalStateException</a>
 * @version 1.0
 */
class javolution::lang::IllegalStateException_API : public javolution::lang::RuntimeException_API {
protected:

    IllegalStateException_API(String message) :
        RuntimeException_API(message) {
    };

public:

    /**
     * Returns the runtime exception having the specified message.
     *
     * @param message the exception message.
     */
    static IllegalStateException newInstance(String message = Type::Null) {
        return new IllegalStateException_API(message);
    }

};
#endif
