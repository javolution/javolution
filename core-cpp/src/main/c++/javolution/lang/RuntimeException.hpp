/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_RUNTIME_EXCEPTION_HPP
#define _JAVOLUTION_LANG_RUNTIME_EXCEPTION_HPP

#include "javolution/lang/Exception.hpp"

namespace javolution {
    namespace lang {
        class RuntimeException_API;
        class RuntimeException : public Exception {
        public:
            RuntimeException(Type::NullHandle = Type::Null) : Exception() {} // Null
            RuntimeException(RuntimeException_API* ptr) : Exception((Exception_API*)ptr) {}
        };
    }
}

/**
 * This class and its sub-classes represents exception which should not 
 * occur during normal program execution (typically programming error!).
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/RuntimeException.html">
 *       Java - RuntimeException</a>
 * @version 1.0
 */
class javolution::lang::RuntimeException_API : public javolution::lang::Exception_API {
protected:

    /**
     * Creates an RuntimeException having the specified message.
     *
     * @param message the error message.
     */
    RuntimeException_API(String message = Type::Null) :
        Exception_API(message) {
    };

public:

    /**
     * Returns an exception having the specified message.
     *
     * @param message the message.
     */
    static RuntimeException newInstance(String message = Type::Null) {
        return new RuntimeException_API(message);
    }

};

#endif
