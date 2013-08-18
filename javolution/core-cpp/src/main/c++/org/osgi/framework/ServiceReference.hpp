/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_SERVICE_REFERENCE_HPP
#define _ORG_OSGI_FRAMEWORK_SERVICE_REFERENCE_HPP

#include "javolution/lang/Object.hpp"
#include "org/osgi/framework/Bundle.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class ServiceReference_API;
            typedef Type::Handle<ServiceReference_API> ServiceReference;
         }
    }
}

/**
 * This interface represents a reference to a service.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/ServiceReference.html">
 *       OSGi - ServiceReference</a>
 * @version 1.0
 */
class org::osgi::framework::ServiceReference_API  : public virtual javolution::lang::Object_API {
public:

    /**
     * Returns the bundle that registered the service referenced by this ServiceReference object.
     * This method must return <code>Type::Null</code> when the service has been
     * unregistered. This can be used to determine if the service has been
     * unregistered.
     */
    virtual Bundle getBundle() const = 0;

};
#endif
