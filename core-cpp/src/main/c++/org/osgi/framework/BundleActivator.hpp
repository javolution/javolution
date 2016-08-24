/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_BUNDLE_ACTIVATOR_HPP
#define _ORG_OSGI_FRAMEWORK_BUNDLE_ACTIVATOR_HPP

#include "javolution/lang/Object.hpp"
#include "org/osgi/framework/BundleContext.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class BundleActivator_API;
            typedef Type::Handle<BundleActivator_API> BundleActivator;
       }
    }
}

/**
 * This interface customizes the starting and stopping of a bundle.
 * BundleActivator is an interface that may be implemented when a
 * bundle is started or stopped. The Framework can create instances
 * of a bundle's BundleActivator as required. If an instance's
 * BundleActivator.start method executes successfully, it is guaranteed
 * that the same instance's BundleActivator.stop method will be called
 * when the bundle is to be stopped. The Framework must not concurrently
 * call a BundleActivator object.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/BundleActivator.html">
 *       OSGi - BundleActivator</a>
 * @version 1.0
 */
class org::osgi::framework::BundleActivator_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Starts the bundle activator instance and passes a bundle context
     * object which provides methods for registering services, service listeners
     * etc.
     *
     * @param context The bundle context.
     * @throws javolution::lang::Exception (not declared due to Visual C++ warning C4290)
     */
    virtual void start(BundleContext const& context) = 0;

    /**
     * Stops the bundle activator instance.
     *
     * @param context The bundle context.
     * @throws javolution::lang::Exception (not declared due to Visual C++ warning C4290)
     */
    virtual void stop(BundleContext const& context) = 0;

};

#endif
