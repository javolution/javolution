/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "org/osgi/util/tracker/ServiceTracker.hpp"
#include "javolution/lang/RuntimeException.hpp"

using namespace org::osgi::util::tracker;
using namespace org::osgi::framework;
using namespace javolution::lang;
using namespace javolution::util;

// ServiceTracker Inner class.

class ServiceTracker_API::ServiceListenerImpl : public ServiceListener_API {
    ServiceTracker _thisServiceTracker; // Holds outer reference.

public:

    ServiceListenerImpl(ServiceTracker_API* thisServiceTracker) {
        _thisServiceTracker.set(thisServiceTracker); // Do not increment reference count, this to ensure
        // that the service tracker is deleted when there is
        // no external references to it.
    }

    ~ServiceListenerImpl() {
        _thisServiceTracker.set(0); // Do not decrement reference count.
    }

    void serviceChanged(ServiceEvent const& serviceEvent) {
        ServiceReference serviceReference = serviceEvent->getServiceReference();
        switch (serviceEvent->getType()) {
            case ServiceEvent_API::REGISTERED:
                synchronized(_thisServiceTracker->_trackedServices)
            {
                Object service = _thisServiceTracker->_customizer->addingService(serviceReference);
                // Called before service added (see ServiceTrackerCustomizer spec).
                _thisServiceTracker->_trackedServices->put(serviceReference, service);
                _thisServiceTracker->_cachedService = service;
                _thisServiceTracker->_trackingCount++;
            }
                break;
            case ServiceEvent_API::MODIFIED:
                synchronized(_thisServiceTracker->_trackedServices)
            {
                _thisServiceTracker->_cachedService = Type::Null;
                _thisServiceTracker->_trackingCount++;
                _thisServiceTracker->_customizer->modifiedService(serviceReference, _thisServiceTracker->getService(serviceReference));
                // Called after service modified (see ServiceTrackerCustomizer spec).
            }
                break;
            case ServiceEvent_API::UNREGISTERING:
                synchronized(_thisServiceTracker->_trackedServices)
            {
                _thisServiceTracker->_trackedServices->remove(serviceReference);
                _thisServiceTracker->_cachedService = Type::Null;
                _thisServiceTracker->_trackingCount++;
                _thisServiceTracker->_customizer->removedService(serviceReference, _thisServiceTracker->getService(serviceReference));
                // Called after service removed (see ServiceTrackerCustomizer spec).
            }
                break;
            default:
                throw RuntimeException_API::newInstance(L"Unknown service event");
        }
    }
};

/**
 * Opens this service tracker and begin tracking services.
 */
void ServiceTracker_API::open() {

    // Registers this tracker as listener on all services being published.
    _serviceListener = new ServiceListenerImpl(this); // Inner class.
    _context->addServiceListener(_serviceListener, _filter->toString());

    // Retrieves existing services and start tracking them.
    Type::Array<ServiceReference> serviceReferences = _context->getServiceReferences(_serviceName, Type::Null);

    synchronized(_trackedServices) {
        for (int i = 0; i < serviceReferences.length; i++) {
            Object service = _customizer->addingService(serviceReferences[i]);
            _trackedServices->put(serviceReferences[i], service);
        }
        _trackingCount = 0;
    }
}

/**
 * Closes this service tracker.
 * This method should be called when this service tracker
 * should end the tracking of services.
 */
void ServiceTracker_API::close() {

    // Unregisters this tracker listener.
    if (_serviceListener != Type::Null) {
        _context->removeServiceListener(_serviceListener);
        _serviceListener = Type::Null;
    }
    // Untrack services.

    synchronized(_trackedServices) {
        for (Iterator<Entry<ServiceReference, Object> > i = _trackedServices->entrySet()->iterator(); i->hasNext();) {
            Entry<ServiceReference, Object> entry = i->next();
            Object service = entry->getValue();
            entry->setValue(Type::Null); // Removes the service.
            _customizer->removedService(entry->getKey(), service);
            // Called after service removed (see ServiceTrackerCustomizer spec).
        }
        _trackedServices->clear();
        _cachedService = Type::Null;
        _trackingCount = -1;
    }
}

/**
 * Returns the service object for one of the services being
 * tracked by this service tracker.
 * If any services are being tracked, this implementation returns the
 * result of calling <code>getService(getServiceReference())</code>.
 *
 * @return the service object or <code>Type::Null</code>
 *         if no services is being tracked.
 */
Object ServiceTracker_API::getService() {
    Object service = _cachedService;
    if (service != Type::Null) return service;
    ServiceReference serviceReference = getServiceReference();
    service = (serviceReference != Type::Null) ? getService(serviceReference) : Type::Null;
    return _cachedService = service;
}

/**
 * Returns the service object for the specified ServiceReference if the
 * specified referenced service is being tracked by this service tracker.
 *
 * @param serviceReference the reference to the desired service.
 * @return a service object or <code>Type::Null</code> if the service
 *         referenced by the specified argument is not being tracked.
 */
Object ServiceTracker_API::getService(ServiceReference const& serviceReference) {
    Object service;

    synchronized(_trackedServices) {
        service = _trackedServices->get(serviceReference);
    }
    return service;
}

/**
 * Returns a ServiceReference for one of the services being tracked by this
 * service tracker.
 *
 * @return a ServiceReference or <code>Type::Null</code> if no services are
 *         being tracked.
 */
ServiceReference ServiceTracker_API::getServiceReference() {
    Type::Array<ServiceReference> serviceReferences = getServiceReferences();
    if (serviceReferences == Type::Null) return Type::Null; // No service tracked.
    return serviceReferences[0];
}

/**
 * Returns an array of ServiceReference for all services being tracked by
 * this service tracker.
 *
 * @return an array of ServiceReferences or <code>Type::Null</code> if no
 *         services is being tracked.
 */
Type::Array<ServiceReference> ServiceTracker_API::getServiceReferences() {
    Type::Array<ServiceReference> serviceReferences;

    synchronized(_trackedServices) {
        Type::int32 count = _trackedServices->size();
        if (count == 0) return Type::Null;
        serviceReferences = Type::Array<ServiceReference > (count);
        _trackedServices->keySet()->toArray(serviceReferences);
    }
    return serviceReferences;
}

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
Type::Array<Object> ServiceTracker_API::getServices() {
    Type::Array<Object> services;

    synchronized(_trackedServices) {
        Type::int32 count = _trackedServices->size();
        if (count == 0) return Type::Null;
        services = Type::Array<Object > (count);
        _trackedServices->values()->toArray(services);
    }
    return services;
}

