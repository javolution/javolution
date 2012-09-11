/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_EXCEPTION_HPP
#define _JAVOLUTION_LANG_EXCEPTION_HPP

#include "javolution/lang/Throwable.hpp"

namespace javolution {
    namespace lang {
        class Exception_API;
        class Exception : public Throwable {
        public:
            Exception(Type::NullHandle = Type::Null) : Throwable() {} // Null
            Exception(Exception_API* ptr) : Throwable((Throwable_API*)ptr) {}
        };
    }
}

/**
 * This class and its subclasses are a form of
 * <code>Throwable</code> that indicates conditions that a reasonable
 * application might want to catch.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Exception.html">
 *       Java - Exception</a>
 * @version 1.0
 */
class javolution::lang::Exception_API : public javolution::lang::Throwable_API {
protected:

    Exception_API(String message) :
        Throwable_API(message) {
    };

public:

    /**
     * Returns an exception having the specified message.
     *
     * @param message the exception message.
     */
    static Exception newInstance(String message = Type::Null) {
        return new Exception_API(message);
    }

};

#endif
