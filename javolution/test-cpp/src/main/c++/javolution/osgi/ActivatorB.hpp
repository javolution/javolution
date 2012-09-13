/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_ACTIVATOR_B_HPP
#define _JAVOLUTION_OSGI_ACTIVATOR_B_HPP

#include "org/osgi/framework/BundleActivator.hpp"
#include "javolution/osgi/ServiceBImpl.hpp"
#include "javolution/osgi/ServiceA.hpp"
#include "org/osgi/util/tracker/ServiceTracker.hpp"

namespace javolution {
    namespace osgi {
        class ActivatorB_API;
        typedef Type::Handle<ActivatorB_API> ActivatorB;
    }
}

/**
 * Activator A implementation for testing purpose.
 */
class javolution::osgi::ActivatorB_API : public virtual org::osgi::framework::BundleActivator_API {
public:
    static ActivatorB newInstance() {
        return ActivatorB(new ActivatorB_API());
    }

    void start(org::osgi::framework::BundleContext ctx) {
        ctx->registerService(
                ServiceB_API::NAME,
                ServiceBImpl_API::newInstance(),
                Type::Null);
        trackerServiceA = org::osgi::util::tracker::ServiceTracker_API::newInstance(
                ctx,
                javolution::lang::String_API::valueOf(L"javolution.osgi.ServiceA"),
                Type::Null);
        trackerServiceA->open();
    }

    void stop(org::osgi::framework::BundleContext ctx) {
        trackerServiceA->close();
        trackerServiceA = Type::Null;
    }

    static ServiceA getServiceA() {
        org::osgi::util::tracker::ServiceTracker st = trackerServiceA;
        if (st == Type::Null) return Type::Null;
        return Type::dynamic_handle_cast <ServiceA_API> (st->getService());
    }

private:

    static org::osgi::util::tracker::ServiceTracker trackerServiceA;

};

// Initalization of static members.
org::osgi::util::tracker::ServiceTracker javolution::osgi::ActivatorB_API::trackerServiceA = Type::Null;

#endif
