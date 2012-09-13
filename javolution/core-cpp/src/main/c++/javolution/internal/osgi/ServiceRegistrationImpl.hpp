/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_INTERNAL_OSGI_SERVICE_REGISTRATION_IMPL_HPP
#define _JAVOLUTION_INTERNAL_OSGI_SERVICE_REGISTRATION_IMPL_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/UnsupportedOperationException.hpp"
#include "org/osgi/framework/BundleContext.hpp"
#include "org/osgi/framework/Constants.hpp"
#include "org/osgi/framework/ServiceRegistration.hpp"
#include "javolution/internal/osgi/BundleImpl.hpp"
#include "javolution/internal/osgi/ServiceReferenceImpl.hpp"

using namespace javolution::lang;
using namespace javolution::util;
using namespace javolution::internal::osgi;
using namespace org::osgi::framework;

namespace javolution {
    namespace internal {
        namespace osgi {
            class ServiceRegistrationImpl_API;
            typedef Type::Handle<ServiceRegistrationImpl_API> ServiceRegistrationImpl;
        }
    }
}

/**
 * Service registration implementation (value type).
 *
 * @version 1.0
 */
class javolution::internal::osgi::ServiceRegistrationImpl_API : public virtual ServiceRegistration_API {
public: // Internal classes can have public members visibility.

	ServiceReferenceImpl _serviceReference;

    ServiceRegistrationImpl_API(ServiceReferenceImpl serviceReference) {
        _serviceReference = serviceReference;
    }

    ServiceReference getReference() const {
        return _serviceReference;
    }

    void unregister() {
        // Fire event to listeners from all bundles.
        ServiceEvent serviceEvent = new ServiceEvent_API(ServiceEvent_API::UNREGISTERING, _serviceReference);
        Type::dynamic_handle_cast<BundleImpl_API>(_serviceReference->_bundle)->_osgi->fireServiceEvent(serviceEvent);
        Type::dynamic_handle_cast<BundleImpl_API>(_serviceReference->_bundle)->_serviceReferences->remove(_serviceReference);
        _serviceReference->_bundle = Type::Null; // No more active.
    }

};
#endif
