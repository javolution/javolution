/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_BUNDLE_HPP
#define _ORG_OSGI_FRAMEWORK_BUNDLE_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/util/Dictionary.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class Bundle_API;
            typedef Type::Handle<Bundle_API> Bundle;
            class BundleContext_API; // Forward reference (to break cycle).
            typedef Type::Handle<BundleContext_API> BundleContext;
        }
    }
}

/**
 * This interface represents qn installed bundle in the Framework.
 * A Bundle object is the access point to define the lifecycle of an
 * installed bundle. Each bundle installed in the OSGi environment must
 * have an associated Bundle object.A bundle must have a unique identity,
 * a long, chosen by the Framework. This identity must not change during
 * the lifecycle of a bundle, even when the bundle is updated.
 * Uninstalling and then reinstalling the bundle must create a new unique
 * identity. A bundle can be in one of six states:
 * UNINSTALLED, INSTALLED, RESOLVED, STARTING, STOPPING, ACTIVE.
 * Values assigned to these states have no specified ordering; they represent
 * bit values that may be ORed together to determine if a bundle is in one
 * of the valid states.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/Bundle.html">
 *       OSGi - Bundle</a>
 * @version 1.0
 */
class org::osgi::framework::Bundle_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Definition of the states.
     */
    static const Type::int32 UNINSTALLED = 1;

    static const Type::int32 INSTALLED = 2;

    static const Type::int32 RESOLVED = 4;

    static const Type::int32 STARTING = 8;

    static const Type::int32 STOPPING = 16;

    static const Type::int32 ACTIVE = 32;
    
    /**
     * Returns this bundle bundle context.
     */
    virtual BundleContext getBundleContext() const = 0;

    /**
     * Returns this bundle symbolic name.
     */
    virtual javolution::lang::String getSymbolicName() const = 0;

    /**
     * Returns this bundle's state.
     */
    virtual Type::int32 getState() const = 0;

    /**
     * Starts this bundle.
     */
    virtual void start() = 0;

    /**
     * Stops this bundle.
     */
    virtual void stop() = 0;

    /**
     * Returns this bundle header information.
     */
    virtual javolution::util::Dictionary getHeaders() const = 0;

};

#endif
