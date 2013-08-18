/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_UTIL_TRACKER_SERVICE_TRACKER_HPP
#define _ORG_OSGI_UTIL_TRACKER_SERVICE_TRACKER_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "javolution/util/FastMap.hpp"
#include "org/osgi/framework/ServiceEvent.hpp"
#include "org/osgi/framework/BundleContext.hpp"
#include "org/osgi/framework/Constants.hpp"
#include "org/osgi/framework/ServiceListener.hpp"
#include "org/osgi/framework/Filter.hpp"
#include "org/osgi/util/tracker/ServiceTrackerCustomizer.hpp"

namespace org {
    namespace osgi {
        namespace util {
            namespace tracker {
                class ServiceTracker_API;
                typedef Type::Handle<ServiceTracker_API> ServiceTracker;
            }
        }
    }
}

/**
 * This class simplifies using services from the Framework's service registry.
 *
 * A service tracker is constructed with search criteria and a ServiceTrackerCustomizer object.
 *
 * A service tracker can use a ServiceTrackerCustomizer to customize the service objects to be tracked.
 * The servicetracker can then be opened to begin tracking all services in the Framework's service
 * registry that match the specified search criteria. The ServiceTracker correctly handles all of
 * the details of listening to ServiceEvents and getting and ungetting services.
 *
 * The <code>getServiceReferences</code> method can be called to get references to the services
 * being tracked. The <code>getService</code> and <code>getServices</code> methods can be called
 * to get the service objects for the tracked service.
 *
 * The ServiceTracker class is thread-safe. It does not call a ServiceTrackerCustomizer while holding
 * any locks. ServiceTrackerCustomizer implementations must also be thread-safe.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/util/tracker/ServiceTracker.html">
 *       OSGi - ServiceTracker</a>
 * @version 1.0
 */
class org::osgi::util::tracker::ServiceTracker_API : public virtual org::osgi::util::tracker::ServiceTrackerCustomizer_API {
    /**
     * The bundle context used by this service tracker.
     */
    org::osgi::framework::BundleContext _context;

    /**
     * The filter used by this service tracker which specifies the search
     * criteria for the services to track.
     */
    org::osgi::framework::Filter _filter;

    /**
     * The service filter for this service tracker.
     */
    javolution::lang::String _serviceName;

    /**
     * The customizer (this service tracker is the customizer if none is specified at creation).
     */
    ServiceTrackerCustomizer _customizer;

    /**
     * Holds the services being tracked (service reference to service map).
     */
    javolution::util::FastMap<org::osgi::framework::ServiceReference, javolution::lang::Object> _trackedServices;

    /**
     * Holds the service listener has a regular pointer to avoid cycles.
     * (the service listener holds an handle to this service tracker).
     */
    org::osgi::framework::ServiceListener _serviceListener;

    /**
     * Holds the tracking count.
     */
    volatile Type::int32 _trackingCount;

    /**
     * Holds cached service instance (optimization).
     */
    javolution::lang::Object _cachedService;

    /**
     * Inner class to listen to services (has access to private members of ServiceTracker)
     */
    class ServiceListenerImpl;

protected:

    /**
     * Constructor for ServiceTracker.
     */
    ServiceTracker_API(org::osgi::framework::BundleContext const& context,
            javolution::lang::String const& serviceName, ServiceTrackerCustomizer const& customizer) {
        _context = context;
        _serviceName = serviceName;
        _customizer = customizer;
        if (_customizer == Type::Null) {
            _customizer.set(this); // Do not increment the counter on 'this' to allow for its
        } // deletion when there is no more external references to it.
        javolution::lang::StringBuilder listenerFilter = javolution::lang::StringBuilder_API::newInstance();
        listenerFilter->append('(')->append(org::osgi::framework::Constants_API::OBJECTCLASS)
                ->append('=')->append(serviceName)->append(')');
        _filter = _context->createFilter(listenerFilter->toString());
        _trackedServices = new javolution::util::FastMap_API<org::osgi::framework::ServiceReference, javolution::lang::Object > ();
        _trackingCount = -1;
    }

public:

    /**
     * Creates instances of class <code>ServiceTracker</code>.
     *
     * @param context the bundle context.
     * @param serviceName the name of the service the service tracker listens for.
     * @param tcustomizer The customizer object to call when services are added,
     *        modified, or removed in this service tracker. If customizer is
     *        <code>Type::Null</code>, then this ServiceTracker will be used as the
     *        ServiceTrackerCustomizer and this service tracker will call the
     *        ServiceTrackerCustomizer methods on itself.
     */
    static ServiceTracker newInstance(org::osgi::framework::BundleContext const& context,
            javolution::lang::String const& serviceName, ServiceTrackerCustomizer const& customizer) {
        return new ServiceTracker_API(context, serviceName, customizer);
    }

    /**
     * Opens this service tracker and begin tracking services.
     */
    JAVOLUTION_DLL virtual void open();

    /**
     * Closes this service tracker.
     * This method should be called when this service tracker
     * should end the tracking of services.
     */
    JAVOLUTION_DLL virtual void close();

    /**
     * Returns the service object for one of the services being
     * tracked by this service tracker.
     * If any services are being tracked, this implementation returns the
     * result of calling <code>getService(getServiceReference())</code>.
     *
     * @return the service object or <code>Type::Null</code>
     *         if no services is being tracked.
     */
    JAVOLUTION_DLL virtual javolution::lang::Object getService();

    /**
     * Returns the service object for the specified ServiceReference if the
     * specified referenced service is being tracked by this service tracker.
     *
     * @param serviceReference the reference to the desired service.
     * @return a service object or <code>Type::Null</code> if the service
     *         referenced by the specified argument is not being tracked.
     */
    JAVOLUTION_DLL virtual javolution::lang::Object getService(org::osgi::framework::ServiceReference const& serviceReference);

    /**
     * Returns a ServiceReference for one of the services being tracked by this
     * service tracker.
     *
     * @return A ServiceReference or <code>Type::Null</code> if no services are
     *         being tracked.
     */
    JAVOLUTION_DLL virtual org::osgi::framework::ServiceReference getServiceReference();

    /**
     * Return an array of ServiceReference for all services being tracked by
     * this service tracker.
     *
     * @return an array of ServiceReferences or <code>Type::Null</code> if no
     *         services is being tracked.
     */
    JAVOLUTION_DLL virtual Type::Array<org::osgi::framework::ServiceReference> getServiceReferences();

    /**
     * Return an array of service objects for all services being tracked by
     * this service tracker.
     * This implementation calls <code>getServiceReferences()</code> to get the
     * list of references for the tracked services and then calls
     * <code>getService(ServiceReference)</code> for each reference to get
     * the tracked service object.
     *
     * @return an array of service objects or <code>Type::Null</code>
     */
    JAVOLUTION_DLL virtual Type::Array<javolution::lang::Object> getServices();

    /**
     * Return the number of services being tracked by this service tracker.
     *
     * @return the number of services being tracked.
     */
    virtual Type::int32 size() const {
        return _trackedServices->size();
    }

    /**
     * Returns the tracking count for this ServiceTracker. The tracking count
     * is initialized to 0 when this service tracker is opened.
     * Every time a service is added, modified or removed from this
     * service tracker, the tracking count is incremented.
     * The tracking count can be used to determine if this service tracker has
     * added, modified or removed a service by comparing a tracking count value
     * previously collected with the current tracking count value. If the value
     * has not changed, then no service has been added, modified or removed
     * from this service tracker since the previous tracking count was collected.
     *
     * @return The tracking count for this service tracker or <code>-1</code>
     *         if this service tracker is not open.
     */
    virtual Type::int32 getTrackingCount() const {
        return _trackingCount;
    }

    /**
     * Implements ServiceTrackerCustomizer.
     */
    virtual javolution::lang::Object addingService(org::osgi::framework::ServiceReference const& serviceReference) {
        return _context->getService(serviceReference); // Default behavior (no translation)
    }

    /**
     * Implements ServiceTrackerCustomizer.
     */
    virtual void modifiedService(org::osgi::framework::ServiceReference const& serviceReference, javolution::lang::Object const& service) {
        // Do nothing.
    }

    /**
     * Implements ServiceTrackerCustomizer.
     */
    virtual void removedService(org::osgi::framework::ServiceReference const& serviceReference, javolution::lang::Object const& service) {
        _context->ungetService(serviceReference);
    }

};

#endif
