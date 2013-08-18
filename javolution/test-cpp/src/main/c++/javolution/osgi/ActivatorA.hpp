/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_ACTIVATOR_A_HPP
#define _JAVOLUTION_OSGI_ACTIVATOR_A_HPP

#include "org/osgi/framework/BundleActivator.hpp"
#include "javolution/osgi/ServiceAImpl.hpp"
#include "javolution/osgi/ServiceB.hpp"
#include "org/osgi/util/tracker/ServiceTracker.hpp"

namespace javolution {
    namespace osgi {
        class ActivatorA_API;
        typedef Type::Handle<ActivatorA_API> ActivatorA;
    }
}

/**
 * Activator A implementation for testing purpose.
 */
class javolution::osgi::ActivatorA_API : public virtual org::osgi::framework::BundleActivator_API {
public:
    static ActivatorA newInstance() {
        return ActivatorA(new ActivatorA_API());
    }

    void start(org::osgi::framework::BundleContext const& ctx) {
        ctx->registerService(
                ServiceA_API::NAME,
                ServiceAImpl_API::newInstance(),
                Type::Null);
        trackerServiceB = org::osgi::util::tracker::ServiceTracker_API::newInstance(
                ctx,
                javolution::lang::String_API::valueOf(L"javolution.osgi.ServiceB"),
                Type::Null);
        trackerServiceB->open();
    }

    void stop(org::osgi::framework::BundleContext const& ctx) {
        trackerServiceB->close();
        trackerServiceB = Type::Null;
    }

    static ServiceB getServiceB() {
        org::osgi::util::tracker::ServiceTracker st = trackerServiceB;
        if (st == Type::Null) return Type::Null;
        return Type::dynamic_handle_cast <ServiceB_API> (st->getService());
    }

private:

    static  org::osgi::util::tracker::ServiceTracker trackerServiceB;

};

// Initalization of static members.
 org::osgi::util::tracker::ServiceTracker javolution::osgi::ActivatorA_API::trackerServiceB = Type::Null;

#endif
