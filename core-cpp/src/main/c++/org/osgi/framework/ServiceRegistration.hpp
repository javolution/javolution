/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_SERVICE_REGISTRATION_HPP
#define _ORG_OSGI_FRAMEWORK_SERVICE_REGISTRATION_HPP

#include "javolution/lang/Object.hpp"
#include "org/osgi/framework/ServiceReference.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class ServiceRegistration_API;
            typedef Type::Handle<ServiceRegistration_API> ServiceRegistration;
        }
    }
}

/**
 * This interface represents a registered service.
 *
 * The Framework returns a ServiceRegistration object when a
 * BundleContext.registerService method invocation is successful.
 * The ServiceRegistration object is for the private use of the
 * registering bundle and should not be shared with other bundles.
 *
 * The ServiceRegistration object may be used to update the
 * properties of the service or to unregister the service.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/ServiceRegistration.html">
 *       OSGi - ServiceRegistration</a>
 * @version 1.0
 */
class org::osgi::framework::ServiceRegistration_API  : public virtual javolution::lang::Object_API {
public:

    /**
     * Returns a ServiceReference object for a service being registered.
     */
     virtual ServiceReference getReference() const = 0;

    /**
     * Unregisters a service object with the framework.
     */
     virtual void unregister() = 0;


};

#endif
