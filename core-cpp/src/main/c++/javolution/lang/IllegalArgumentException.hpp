/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_ILLEGAL_ARGUMENT_EXCEPTION_HPP
#define _JAVOLUTION_LANG_ILLEGAL_ARGUMENT_EXCEPTION_HPP

#include "javolution/lang/RuntimeException.hpp"

namespace javolution {
    namespace lang {
        class IllegalArgumentException_API;
        class IllegalArgumentException : public RuntimeException { 
        public:
            IllegalArgumentException(Type::NullHandle = Type::Null) : RuntimeException() {} // Null
            IllegalArgumentException(IllegalArgumentException_API* ptr) : RuntimeException((RuntimeException_API*)ptr) {}
        };
    }
}

/**
 * Thrown to indicate that a method has been passed an illegal or inappropriate argument.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/IllegalArgumentException.html">
 *       Java - IllegalArgumentException</a>
 * @version 1.0
 */
class javolution::lang::IllegalArgumentException_API : public javolution::lang::RuntimeException_API {
protected:

    IllegalArgumentException_API(String const& message) :
        RuntimeException_API(message) {
    };

public:

    /**
     * Returns the runtime exception having the specified message.
     *
     * @param message the exception message.
     */
    static IllegalArgumentException newInstance(String const& message = Type::Null) {
        return new IllegalArgumentException_API(message);
    }
};
#endif
