/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_INTERNAL_OSGI_OSGI_IMPL_HPP
#define _JAVOLUTION_INTERNAL_OSGI_OSGI_IMPL_HPP

#include "javolution/util/FastTable.hpp"
#include "org/osgi/framework/ServiceEvent.hpp"
#include "org/osgi/framework/BundleActivator.hpp"
#include "org/osgi/framework/Bundle.hpp"
#include "org/osgi/framework/ServiceListener.hpp"
#include "javolution/osgi/OSGi.hpp"

namespace javolution {
    namespace internal {
        namespace osgi {
            class OSGiImpl_API;
            typedef Type::Handle<OSGiImpl_API> OSGiImpl;
            class BundleContextImpl_API;
            class BundleImpl_API;
        }
    }
}

/**
 * This class represents the OSGi framework.
 *
 * @version 1.0
 */
class javolution::internal::osgi::OSGiImpl_API : public javolution::osgi::OSGi_API {
public: // Internal classes can have public members visibility.

   /**
    * Holds all known bundle.
    */
   javolution::util::FastTable<org::osgi::framework::Bundle> _bundles;

    /**
     * Creates a new instance.
     */
    OSGiImpl_API() {
        _bundles = new javolution::util::FastTable_API<org::osgi::framework::Bundle>();
    }

    // Override
    JAVOLUTION_DLL void start(javolution::lang::String const& symbolicName, org::osgi::framework::BundleActivator const& activator);

    // Override
    JAVOLUTION_DLL void stop(javolution::lang::String const& symbolicName);

    // Override
    JAVOLUTION_DLL org::osgi::framework::Bundle getBundle(javolution::lang::String const& symbolicName) const;

    /**
     * Fires the specified service event (affect all bundles).
     */
    JAVOLUTION_DLL void fireServiceEvent(org::osgi::framework::ServiceEvent const& serviceEvent);

};
#endif
