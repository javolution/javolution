/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_INVALID_SYNTAX_EXCEPTION_HPP
#define _ORG_OSGI_FRAMEWORK_INVALID_SYNTAX_EXCEPTION_HPP

#include "javolution/lang/Exception.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class InvalidSyntaxException_API;
            class InvalidSyntaxException : public javolution::lang::Exception {
            public:
                InvalidSyntaxException(Type::NullHandle = Type::Null) : Exception() {} // Null
                InvalidSyntaxException(InvalidSyntaxException_API* ptr) : Exception((Exception_API*)ptr) {}
            };
        }
    }
}

/**
 * A Framework exception used to indicate that a filter string has an invalid
 * syntax.
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/InvalidSyntaxException.html">
 *       OSGi - InvalidSyntaxException</a>
 * @version 1.0
 */
class org::osgi::framework::InvalidSyntaxException_API : public virtual javolution::lang::Exception_API {
protected:

    InvalidSyntaxException_API(javolution::lang::String const& message) :
        javolution::lang::Exception_API(message) {
    };

public:

    /**
     * Returns the exception having the specified message.
     *
     * @param message the exception message.
     */
    static InvalidSyntaxException newInstance(String const& message = Type::Null) {
        return new InvalidSyntaxException_API(message);
    }

};

#endif
