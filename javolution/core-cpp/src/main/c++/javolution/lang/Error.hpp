/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_ERROR_HPP
#define _JAVOLUTION_LANG_ERROR_HPP

#include "javolution/lang/Throwable.hpp"

namespace javolution {
    namespace lang {
        class Error_API;
        class Error : public Throwable { 
        public:
            Error(Type::NullHandle = Type::Null) : Throwable() {} // Null
            Error(Error_API* ptr) : Throwable((Throwable_API*)ptr) {}
        };
    }
}

/**
 * This class and its subclasses indicates serious problems that a reasonable
 * application should not try to catch.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Error.html">
 *       Java - Error</a>
 * @version 1.0
 */
class javolution::lang::Error_API : public javolution::lang::Throwable_API {
public:

    /**
     * Returns an Error having the specified message.
     *
     * @param message the Error message.
     */
    static Error newInstance(String message = Type::Null) {
        return new Error_API(message);
    }

protected:

    Error_API(String message) :
        Throwable_API(message) {
    };

};

#endif
