/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_UNSUPPORTED_OPERATION_EXCEPTION_HPP
#define _JAVOLUTION_LANG_UNSUPPORTED_OPERATION_EXCEPTION_HPP

#include "javolution/lang/RuntimeException.hpp"

namespace javolution {
    namespace lang {
        class UnsupportedOperationException_API;
        class UnsupportedOperationException : public RuntimeException {
        public:
            UnsupportedOperationException(Type::NullHandle = Type::Null) : RuntimeException() {} // Null
            UnsupportedOperationException(UnsupportedOperationException_API* ptr) : RuntimeException((RuntimeException_API*)ptr) {}
        };
     }
}

/**
 * Thrown to indicate that the requested operation is not supported.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/UnsupportedOperationException.html">
 *       Java - UnsupportedOperationException</a>
 * @version 1.0
 */
class javolution::lang::UnsupportedOperationException_API : public javolution::lang::RuntimeException_API {
protected:

    UnsupportedOperationException_API(String message) :
        RuntimeException_API(message) {
    };

public:

    /**
     * Returns an unsupported operation having the specified message.
     *
     * @param message the exception message.
     */
    static UnsupportedOperationException newInstance(String message = Type::Null) {
        return new UnsupportedOperationException_API(message);
    }

};

#endif
