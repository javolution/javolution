/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_INTERNAL_OSGI_BUNDLE_CONTEXT_IMPL_HPP
#define _JAVOLUTION_INTERNAL_OSGI_BUNDLE_CONTEXT_IMPL_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "org/osgi/framework/BundleContext.hpp"
#include "org/osgi/framework/Bundle.hpp"
#include "org/osgi/framework/InvalidSyntaxException.hpp"

#include "javolution/log/Logging.hpp"
#include "javolution/internal/osgi/OSGiImpl.hpp"
#include "javolution/internal/osgi/ServiceReferenceImpl.hpp"
#include "javolution/internal/osgi/BundleImpl.hpp"
#include "javolution/internal/osgi/ServiceRegistrationImpl.hpp"
#include "javolution/internal/osgi/FilterImpl.hpp"

using namespace javolution::lang;
using namespace javolution::util;
using namespace javolution::log;
using namespace org::osgi::framework;
using namespace javolution::internal::osgi;

namespace javolution {
    namespace internal {
        namespace osgi {
            class BundleContextImpl_API;
            typedef Type::Handle<BundleContextImpl_API> BundleContextImpl;
        }
    }
}

/**
 * BundleContext implementation (forwards users action to BundleImpl and OSGi).
 *
 * @version 1.0
 */
class javolution::internal::osgi::BundleContextImpl_API : public virtual BundleContext_API {
public: // Internal classes can have public members visibility.

    OSGiImpl _osgi;
    BundleImpl _bundle;

    BundleContextImpl_API(OSGiImpl const& osgi, BundleImpl const& bundle) {
        _osgi = osgi;
        _bundle = bundle;
    }

    Bundle getBundle() const {
        return _bundle;
    }

    void addServiceListener(ServiceListener const& sl, String const& filter) {
        _bundle->_serviceListeners->add(sl);
        _bundle->_serviceListenerFilters->add(filter);
    }

    void removeServiceListener(ServiceListener const& sl) {
        Type::int32 index = _bundle->_serviceListeners->indexOf(sl);
        _bundle->_serviceListeners->remove(index);
        _bundle->_serviceListenerFilters->remove(index);
    }

    ServiceRegistration registerService(String const& serviceName, Object const& service, Dictionary const&) {
        ServiceReferenceImpl serviceReference = new ServiceReferenceImpl_API(_bundle, serviceName, service);
        _bundle->_serviceReferences->add(serviceReference);
        // Fire service event.
        ServiceEvent serviceEvent = new ServiceEvent_API(ServiceEvent_API::REGISTERED, serviceReference);
        _osgi->fireServiceEvent(serviceEvent);
        return new ServiceRegistrationImpl_API(serviceReference);
    }

    Type::Array<ServiceReference> getServiceReferences(String const& clazz, String const& filterExpression) const throw (InvalidSyntaxException) {
        Filter filter = (filterExpression != Type::Null) ? createFilter(filterExpression) : Type::Null;
        // Searches all bundles.
        FastTable<ServiceReference> services = new FastTable_API<ServiceReference > ();
        for (int i = 0; i < _osgi->_bundles->size(); i++) {
            BundleImpl bundle = Type::dynamic_handle_cast<BundleImpl_API > (_osgi->_bundles->get(i));
            for (int j = 0; j < bundle->_serviceReferences->size(); j++) {
                ServiceReferenceImpl serviceReference = bundle->_serviceReferences->get(j);
                if (!serviceReference->_serviceName->equals(clazz))
                    continue; // No match.
                if ((filter != Type::Null) && (!filter->match(serviceReference)))
                    continue; // No match.
                services->add(serviceReference);
            }
        }
        Type::int32 count = services->size();
        if (count == 0) return Type::Null;
        return services->toArray(Type::Array<ServiceReference > (count));
    }

    Object getService(ServiceReference const& sr) {
        ServiceReferenceImpl sri = Type::dynamic_handle_cast<ServiceReferenceImpl_API > (sr);
        return sri->_service;
    }

    Filter createFilter(javolution::lang::String const& filter) const {
        return new FilterImpl_API(filter);
    }

    Type::boolean ungetService(ServiceReference const&) {
        return false;
    }

    Type::Array<Bundle> getBundles() const {
        Type::int32 count = _osgi->_bundles->size();
        return _osgi->_bundles->toArray(Type::Array<Bundle > (count));
    }

    ServiceReference getServiceReference(javolution::lang::String const& clazz) const {
        try {
            Type::Array<ServiceReference> refs = getServiceReferences(clazz, Type::Null);
            return (refs == Type::Null) ? Type::Null : refs[0];
        } catch (InvalidSyntaxException e) {
            Logging_API::error(L"JAVOLUTION", L"Invalid Syntax", e);
        }
        return Type::Null;
    }
};
#endif
