/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_INTERNAL_OSGI_SERVICE_REFERENCE_IMPL_HPP
#define _JAVOLUTION_INTERNAL_OSGI_SERVICE_REFERENCE_IMPL_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "org/osgi/framework/Bundle.hpp"
#include "org/osgi/framework/ServiceReference.hpp"

namespace javolution {
    namespace internal {
        namespace osgi {
            class ServiceReferenceImpl_API;
            typedef Type::Handle<ServiceReferenceImpl_API> ServiceReferenceImpl;
            class BundleContextImpl_API;
            class BundleImpl_API;
            class FilterImpl_API;
            class OSGiImpl_API;
        }
    }
}
using namespace javolution::lang;
using namespace javolution::util;
using namespace javolution::internal::osgi;
using namespace org::osgi::framework;

/**
 * Service reference implementation (value type).
 *
 * @version 1.0
 */
class javolution::internal::osgi::ServiceReferenceImpl_API : public virtual ServiceReference_API {
public: // Internal classes can have public members visibility.

    Bundle _bundle;
    String _serviceName;
    Object _service;

    ServiceReferenceImpl_API(Bundle bundle, String serviceName, Object service) {
        _bundle = bundle;
        _serviceName = serviceName;
        _service = service;
    }

    Bundle getBundle() const {
        return _bundle;
    }

    // Overrides.
    String toString() const {
        return StringBuilder_API::newInstance()
                ->append("Service ")->append(_serviceName)
                ->append(" from ")->append(_bundle)->toString();
    }
};
#endif
