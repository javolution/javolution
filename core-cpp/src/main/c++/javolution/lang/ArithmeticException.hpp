/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_ARITHMETIC_EXCEPTION_HPP
#define _JAVOLUTION_LANG_ARITHMETIC_EXCEPTION_HPP

#include "javolution/lang/RuntimeException.hpp"

namespace javolution {
    namespace lang {
        class ArithmeticException_API;
        class ArithmeticException : public RuntimeException { 
        public:
            ArithmeticException(Type::NullHandle = Type::Null) : RuntimeException() {} // Null
            ArithmeticException(ArithmeticException_API* ptr) : RuntimeException((RuntimeException_API*)ptr) {}
        };
    }
}

/**
 * Thrown an exceptional arithmetic condition has occurred.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/ArithmeticException.html">
 *       Java - ArithmeticException</a>
 * @version 1.0
 */
class javolution::lang::ArithmeticException_API : public javolution::lang::RuntimeException_API {
protected:

    ArithmeticException_API(String message) : RuntimeException_API(message) {
    };

public:

    /**
     * Returns an arithmetic exception having the specified message.
     *
     * @param message the exception message or Type::Null
     */
    static ArithmeticException newInstance(String message = Type::Null) {
        return new ArithmeticException_API(message);
    }

};

#endif
