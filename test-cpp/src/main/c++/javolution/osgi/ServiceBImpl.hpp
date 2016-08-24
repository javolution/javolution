/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_SERVICE_B_IMPL_HPP
#define _JAVOLUTION_OSGI_SERVICE_B_IMPL_HPP

#include "javolution/osgi/ServiceB.hpp"

namespace javolution {
    namespace osgi {
        class ServiceBImpl_API;
        typedef Type::Handle<ServiceBImpl_API> ServiceBImpl;
    }
}

/**
 * Service A implementation.
 */
class javolution::osgi::ServiceBImpl_API : public virtual javolution::osgi::ServiceB_API {
public:
    static ServiceBImpl newInstance() {
        return ServiceBImpl(new javolution::osgi::ServiceBImpl_API());
    }
    void myServiceB() {
        std::cout << "My Service B has been called" << std::endl;
    }
};
const javolution::lang::String javolution::osgi::ServiceB_API::NAME = L"javolution.osgi.ServiceB";

#endif
