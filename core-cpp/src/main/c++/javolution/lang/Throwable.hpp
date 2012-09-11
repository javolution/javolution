/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_THROWABLE_HPP
#define _JAVOLUTION_LANG_THROWABLE_HPP

#include <exception>
#include <booster/backtrace.hpp>
#include "javolution/lang/Object.hpp"
#include "javolution/lang/Class.hpp"
#include "javolution/lang/String.hpp"

#define LINE_INFO javolution::lang::StringBuilder_API::newInstance()->append(L"File: ")->append((Type::int32)__FILE__)->append(L", Line: ")->append((Type::int32)__LINE__)->toString()

namespace javolution {
    namespace lang {
        class Throwable_API;
        class Throwable : public Type::Handle<Throwable_API>, public std::exception { 
        public:
            Throwable(Type::NullHandle = Type::Null) : Type::Handle<Throwable_API>() {} // Null
            Throwable(Throwable_API* ptr) : Type::Handle<Throwable_API>(ptr) {}
            JAVOLUTION_DLL virtual const char* what() const throw();
            virtual ~Throwable() throw() { };
        };
    }
}

/**
 * The class is the superclass of all errors and exceptions.
 *
 * Note: This class can have subclasses but because this classes and all
 *       its sub-classes are manipulated using Type::Handle<Throwable_API>
 *       instances, member methods should not  be overridden.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Throwable.html">
 *       Java - Throwable</a>
 * @version 1.0
 */
class javolution::lang::Throwable_API : public virtual javolution::lang::Object_API, public booster::backtrace {

	/**
     * Holds the error message.
     */
    String _message;

protected:

    JAVOLUTION_DLL Throwable_API(String message);

public:

    /**
     * Creates a runtime exception having the specified message.
     * Note: A debug message holding the stack trace may be logged
     *       (useful if an exception is raised during static initialization).
     *
     * @param thisMessage the error message or Type::Null if none.
     */
    static Throwable newInstance(String message = Type::Null) {
        return new Throwable_API(message);
    }

    /**
     * Returns the detail message of this exception or Type::Null if none.
     *
     * @return the detail message string or Type::Null
     */
    virtual String getMessage() const {
        return _message;
    }
    
    /**
     * Logs the stack trace as error.
     *
     * Note: The stack trace is automatically printed when errors are raised
     *       unless <code>Logging_API::isLogErrorEnabled("JAVOLUTION")</code> is
     *       <code>false</code>. Application may disables the printing
     *       of the stack trace by calling:
     *       <code>Logging_API::setLogErrorEnabled(L"JAVOLUTION", false)</code>
     */
    JAVOLUTION_DLL virtual void printStackTrace() const;

    // Should not be overridden by sub-classes.
    String toString() const {
    	return getMessage();
    }

};

#endif
