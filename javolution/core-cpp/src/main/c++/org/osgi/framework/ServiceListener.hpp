/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_SERVICE_LISTENER_HPP
#define _ORG_OSGI_FRAMEWORK_SERVICE_LISTENER_HPP

#include "javolution/lang/Object.hpp"
#include "org/osgi/framework/ServiceEvent.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class ServiceListener_API;
            typedef Type::Handle<ServiceListener_API> ServiceListener;
        }
    }
}

/**
 * This interface represents a listener that may be implemented by
 * a bundle developer. When a ServiceEvent is fired, it is
 * synchronously delivered to a ServiceListener. The Framework may
 *  deliver ServiceEvent objects to a ServiceListener out of order
 *  and may concurrently call and/or reenter a ServiceListener.
 *
 * A ServiceListener object is registered with the Framework using
 * the BundleContext.addServiceListener method. ServiceListener
 *  objects are called with a ServiceEvent object when a service
 *  is registered, modified, or is in the process of unregistering.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/ServiceListener.html">
 *       OSGi - ServiceListener</a>
 * @version 1.0
 */
class org::osgi::framework::ServiceListener_API  : public virtual javolution::lang::Object_API {
public:
 
    /**
     *  Receives notification that a service has had a lifecycle change.
     *
     * @param serviceEvent The service event.
     */
     virtual void serviceChanged(ServiceEvent const& serviceEvent) = 0;

};

#endif
