/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_NULL_POINTER_EXCEPTION_HPP
#define _JAVOLUTION_LANG_NULL_POINTER_EXCEPTION_HPP

#include "javolution/lang/RuntimeException.hpp"

namespace javolution {
    namespace lang {
        class NullPointerException_API;
        class NullPointerException : public RuntimeException { 
        public:
            NullPointerException(Type::NullHandle = Type::Null) : RuntimeException() {} // Null
            NullPointerException(NullPointerException_API* ptr) : RuntimeException((RuntimeException_API*)ptr) {}
        };
    }
}

/**
 * Thrown when an application attempts to use <code>Type::Null</code> in a case where an object is required.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/NullPointerException.html">
 *       Java - NullPointerException</a>
 * @version 1.0
 */
class javolution::lang::NullPointerException_API : public javolution::lang::RuntimeException_API {
protected:

    NullPointerException_API(String const& message) :
        RuntimeException_API(message) {
    };

public:

    /**
     * Returns a NullPointerException having the specified message.
     *
     * @param message the exception message.
     */
    static NullPointerException newInstance(String const& message = Type::Null) {
        return new NullPointerException_API(message);
    }
};

#endif
