/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_INTERNAL_OSGI_BUNDLE_IMPL_HPP
#define _JAVOLUTION_INTERNAL_OSGI_BUNDLE_IMPL_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/Exception.hpp"
#include "javolution/util/FastTable.hpp"
#include "javolution/util/FastMap.hpp"
#include "org/osgi/framework/ServiceListener.hpp"
#include "org/osgi/framework/BundleActivator.hpp"
#include "org/osgi/framework/BundleContext.hpp"
#include "org/osgi/framework/Constants.hpp"
#include "javolution/internal/osgi/OSGiImpl.hpp"
#include "javolution/internal/osgi/ServiceReferenceImpl.hpp"

namespace javolution {
    namespace internal {
        namespace osgi {
            class BundleImpl_API;
            typedef Type::Handle<BundleImpl_API> BundleImpl;
            class BundleContextImpl_API;
        }
    }
}
using namespace javolution::util;
using namespace javolution::lang;
using namespace org::osgi::framework;
using namespace javolution::internal::osgi;

/**
 * The bundle implementation.
 *
 * @version 1.0
 */
class javolution::internal::osgi::BundleImpl_API : public virtual Bundle_API {
public: // Internal classes can have public members visibility.

	OSGiImpl _osgi;
    String _symbolicName;
    BundleActivator _activator;
    Type::int32 _state;
    FastTable<ServiceReferenceImpl> _serviceReferences;
    FastTable<ServiceListener> _serviceListeners;
    FastTable<String> _serviceListenerFilters;
    BundleContext _context; // Null when not active. This context is set by the framework.

    BundleImpl_API(OSGiImpl const& osgi, String const& symbolicName, BundleActivator const& activator) {
        _osgi = osgi;
        _symbolicName = symbolicName;
        _activator = activator;
        _state = Bundle_API::RESOLVED;
        _serviceReferences = new FastTable_API<ServiceReferenceImpl>();
        _serviceListeners = new FastTable_API<ServiceListener>();
        _serviceListenerFilters = new FastTable_API<String>();
    }

    BundleContext getBundleContext() const {
        return _context;
    }

    String getSymbolicName() const {
        return _symbolicName;
    }

    Type::int32 getState() const {
        return _state;
    }

    void start() {
        if (_state != Bundle_API::RESOLVED)
            throw Exception_API::newInstance("Bundle " + _symbolicName + " not in a resolved state");
        _state = Bundle_API::STARTING;
        try {
            _activator->start(_context);
            _state = Bundle_API::ACTIVE;
        } catch (Exception error) {
            /* If the BundleActivator is invalid or throws an exception then:
                 - This bundle's state is set to STOPPING.
                 - A bundle event of type BundleEvent.STOPPING is fired.
                 - Any services registered by this bundle must be unregistered.
                 - Any services used by this bundle must be released.
                 - Any listeners registered by this bundle must be removed.
                 - This bundle's state is set to RESOLVED.
                 - A bundle event of type BundleEvent.STOPPED is fired.
                 - A BundleException is then thrown.
             */
            _state = Bundle_API::STOPPING;
            unregisterServices();
            unregisterListeners();
            _state = Bundle_API::RESOLVED;
            _context = Type::Null;
            throw Exception_API::newInstance("Cannot start bundle " + _symbolicName);
        }
    }

    void stop() {
        /*  If this bundle's state is not STARTING or ACTIVE then this method returns immediately.
            - This bundle's state is set to STOPPING.
            - A bundle event of type BundleEvent.STOPPING is fired.
            - If this bundle's state was ACTIVE prior to setting the state to STOPPING,
              the BundleActivator.stop(org.osgi.framework.BundleContext) method of this bundle's BundleActivator, if one is specified, is called.
            - If that method throws an exception, this method must continue to stop this bundle and a BundleException must be thrown after
              completion of the remaining steps.
            - Any services registered by this bundle must be unregistered.
            - Any services used by this bundle must be released.
            - Any listeners registered by this bundle must be removed.
            - If this bundle's state is UNINSTALLED, because this bundle was uninstalled while the BundleActivator.stop method was running, a BundleException must be thrown.
            - This bundle's state is set to RESOLVED.
            - A bundle event of type BundleEvent.STOPPED is fired.
         */
        if (_state != Bundle_API::ACTIVE)
            return;
        _state = Bundle_API::STOPPING;
        Exception error = Type::Null;
        try {
            _activator->stop(_context);
        } catch (Exception e) {
            error = e;
        }
        unregisterServices();
        unregisterListeners();
        _state = Bundle_API::RESOLVED;
        if (error != Type::Null) throw error; // Rethrow after cleanup (no finally block in C++)
    }

    Dictionary getHeaders() const {
        Dictionary headers = new Dictionary_API();
        headers->put(Constants_API::BUNDLE_NAME, _symbolicName);
        headers->put(Constants_API::BUNDLE_VENDOR, "javolution.org");
        headers->put(Constants_API::BUNDLE_VERSION, "N/A");
        return headers;
    }

private:

    // Unregisters all services from this bundle.
    void unregisterServices() {
        for (int i = 0; i < _serviceReferences->size(); i++) {
            ServiceReferenceImpl serviceReference = _serviceReferences->get(i);
            // Fire event to listeners from all bundles.
            ServiceEvent serviceEvent = new ServiceEvent_API(ServiceEvent_API::UNREGISTERING, serviceReference);
            _osgi->fireServiceEvent(serviceEvent);
            serviceReference->_bundle = Type::Null; // No more active.
        }
        _serviceReferences->clear();
    }

    // Unregisters all listeners from this bundle.
    void unregisterListeners() {
        _serviceListeners->clear();
        _serviceListenerFilters->clear();
    }
};
#endif
