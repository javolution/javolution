/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/internal/osgi/OSGiImpl.hpp"
#include "javolution/lang/Exception.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "org/osgi/framework/Constants.hpp"
#include "javolution/log/Logging.hpp"
#include "javolution/internal/osgi/BundleImpl.hpp"
#include "javolution/internal/osgi/BundleContextImpl.hpp"

using namespace std;
using namespace javolution::lang;
using namespace javolution::util;
using namespace org::osgi::framework;
using namespace javolution::log;
using namespace javolution::internal::osgi;

void OSGiImpl_API::start(String symbolicName, BundleActivator activator) {
    try {
        BundleImpl bundle = dynamic_cast<BundleImpl_API*>(getBundle(symbolicName).get());
        if (bundle == Type::Null) { // Creates the bundle.
            bundle = new BundleImpl_API(this, symbolicName, activator);
            _bundles->add(bundle);
        }
        bundle->_context = new BundleContextImpl_API(this, bundle);
        bundle->start();
    } catch (Throwable error) {
        Logging_API::error(L"JAVOLUTION", error);
    }
}

void OSGiImpl_API::stop(String symbolicName) {
    try {
        BundleImpl bundle = dynamic_cast<BundleImpl_API*> (getBundle(symbolicName).get());
        if (bundle == Type::Null) throw Exception_API::newInstance("Cannot find bundle " + symbolicName);
        bundle->stop();
        bundle->_context = Type::Null;
    } catch (Throwable error) {
        Logging_API::error(L"JAVOLUTION", error);
    }
}

void OSGiImpl_API::fireServiceEvent(ServiceEvent serviceEvent) {
    ServiceReferenceImpl serviceReference = dynamic_cast<ServiceReferenceImpl_API*>(serviceEvent->getServiceReference().get());
    String serviceName = serviceReference->_serviceName;
    String filter = StringBuilder_API::newInstance()
            ->append('(')->append(Constants_API::OBJECTCLASS)
            ->append('=')->append(serviceName)->append(')')->toString();
    for (int i = 0; i < _bundles->size(); i++) {
        BundleImpl bundle = dynamic_cast<BundleImpl_API*> (_bundles->get(i).get());
        for (int j = 0; j < bundle->_serviceListenerFilters->size(); j++) {
            if (filter->equals(bundle->_serviceListenerFilters->get(j))) {
               ServiceListener listener = bundle->_serviceListeners->get(j);
               listener->serviceChanged(serviceEvent);
            }
        }
    }
}

Bundle OSGiImpl_API::getBundle(String symbolicName) const {
    for (int i = 0; i < _bundles->size(); i++) {
        Bundle bundle = _bundles->get(i);
        if (bundle->getSymbolicName()->equals(symbolicName))
            return bundle;
    }
    return Type::Null;
}
