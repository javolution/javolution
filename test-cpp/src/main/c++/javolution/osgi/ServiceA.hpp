/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_SERVICE_A_HPP
#define _JAVOLUTION_OSGI_SERVICE_A_HPP

#include "javolution/lang/Object.hpp"
namespace javolution {
    namespace osgi {
        class ServiceA_API;
        typedef Type::Handle<ServiceA_API> ServiceA;
    }
}

/**
 * Service A specification.
 */
class javolution::osgi::ServiceA_API : public virtual javolution::lang::Object_API {
public:
    virtual void myServiceA() = 0;
    static const javolution::lang::String NAME;
};
#endif

