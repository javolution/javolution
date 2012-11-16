/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_UTIL_TRACKER_SERVICE_TRACKER_CUSTOMIZER_HPP
#define _ORG_OSGI_UTIL_TRACKER_SERVICE_TRACKER_CUSTOMIZER_HPP

#include "javolution/lang/Object.hpp"
#include "org/osgi/framework/ServiceReference.hpp"

namespace org {
    namespace osgi {
        namespace util {
            namespace tracker {
                class ServiceTrackerCustomizer_API;
                typedef Type::Handle<ServiceTrackerCustomizer_API> ServiceTrackerCustomizer;
            }
        }
    }
}

/**
 * This interface allows a ServiceTracker to customize the service
 * objects that are tracked.
 * A ServiceTrackerCustomizer is called when a service is being
 *  added to a ServiceTracker. The ServiceTrackerCustomizer can
 * then return an object for the tracked service.
 *  A ServiceTrackerCustomizer is also called when a tracked
 * service is modified or has been removed from a ServiceTracker.
 *
 * The methods in this interface may be called as the result of
 *  a ServiceEvent being received by a ServiceTracker. Since
 * ServiceEvents are synchronously delivered by the Framework,
 *  it is highly recommended that implementations of these
 *  methods do not register (BundleContext.registerService),
 * modify ( ServiceRegistration.setProperties) or unregister
 * ( ServiceRegistration.unregister) a service while being
 * synchronized on any object.
 *
 * The ServiceTracker class is thread-safe. It does not call
 * a ServiceTrackerCustomizer while holding any locks.
 * ServiceTrackerCustomizer implementations must also be thread-safe.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/util/tracker/ServiceTrackerCustomizer.html">
 *       OSGi - ServiceTrackerCustomizer</a>
 * @version 1.0
 */
class org::osgi::util::tracker::ServiceTrackerCustomizer_API : public virtual javolution::lang::Object_API {
public:

    /**
     * This method is called before a service which matched
     * the search parameters of the ServiceTracker is added
     * to the ServiceTracker. This method should return the
     * service object to be tracked for the specified
     *  ServiceReference. The returned service object is
     * stored in the ServiceTracker and is available from
     * the getService and getServices methods.
     *
     * @param ref The reference to the service being added to the ServiceTracker.
     * @return The service object to be tracked for the specified referenced
     *         service or <code>null</code> if the specified referenced service
     *         should not be tracked.
     */
    virtual javolution::lang::Object addingService(org::osgi::framework::ServiceReference const& serviceReference) = 0;

    /**
     * A service tracked by the ServiceTracker has been modified.
     *  This method is called when a service being tracked by the
     *  ServiceTracker has had it properties modified.
     *
     * @param reference The reference to the service that has been modified.
     * @param service The service object for the specified referenced service.
     */
    virtual void modifiedService(org::osgi::framework::ServiceReference const& reference, javolution::lang::Object const& service) = 0;

    /**
     * This method is called when a service tracked by the ServiceTracker
     * has been removed.
     * This method is called after a service is no longer being
     *  tracked by the ServiceTracker.
     *
     * @param ref The reference to the service being removed from the ServiceTracker.
     * @param service The service object for the specified referenced service.
     */
    virtual void removedService(org::osgi::framework::ServiceReference const& serviceReference, javolution::lang::Object const& service) = 0;

};

#endif
