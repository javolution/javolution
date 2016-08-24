/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_SERVICE_A_IMPL_HPP
#define _JAVOLUTION_OSGI_SERVICE_A_IMPL_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/osgi/ServiceA.hpp"

namespace javolution {
    namespace osgi {
        class ServiceAImpl_API;
        typedef Type::Handle<ServiceAImpl_API> ServiceAImpl;
    }
}


/**
 * Service A implementation.
 */
class javolution::osgi::ServiceAImpl_API : public virtual javolution::osgi::ServiceA_API {
public:
    static ServiceAImpl newInstance() {
        return ServiceAImpl(new javolution::osgi::ServiceAImpl_API());
    }
    void myServiceA() {
        std::cout << "My Service A has been called" << std::endl;
    }
};
const javolution::lang::String javolution::osgi::ServiceA_API::NAME = L"javolution.osgi.ServiceA";

#endif
