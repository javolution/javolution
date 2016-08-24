/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_SERVICE_B_HPP
#define _JAVOLUTION_OSGI_SERVICE_B_HPP

#include "javolution/lang/Object.hpp"

namespace javolution {
    namespace osgi {
        class ServiceB_API;
        typedef Type::Handle<ServiceB_API> ServiceB;
    }
}
/**
 * Service B specification.
 */
class javolution::osgi::ServiceB_API : public virtual javolution::lang::Object_API {
public:
    virtual void myServiceB() = 0;
    static const javolution::lang::String NAME;
};

#endif
