/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_JAVOLUTION_ACTIVATOR_HPP
#define _JAVOLUTION_OSGI_JAVOLUTION_ACTIVATOR_HPP

#include "org/osgi/framework/BundleActivator.hpp"
#include "org/osgi/framework/BundleContext.hpp"

// Should be updated when the major version of the library changes (the tests will check it).
#define JAVOLUTION_MAJOR_VERSION "6.0"

namespace javolution {
    namespace osgi {
        class JavolutionActivator_API;
        typedef Type::Handle<JavolutionActivator_API> JavolutionActivator;
    }
}

/**
 * OSGi bundle activator for the Software Development Kit (JAVOLUTION).
 *
 * @version 1.3
 */
class javolution::osgi::JavolutionActivator_API : public virtual org::osgi::framework::BundleActivator_API {
public:

    /**
     * Creates a new JAVOLUTION OSGi bundle activator.
     *
     * @return the activator instance.
     */
    inline static JavolutionActivator newInstance() {
    	// Identifying information which can be retrieved using the Linux command 'what'
    	char ident[] = "@(#)JAVOLUTION Version: " JAVOLUTION_MAJOR_VERSION;
    	checkVersion(JAVOLUTION_MAJOR_VERSION, ident);
        return JavolutionActivator(new JavolutionActivator_API());
    }

    // Implements BundleActivator.
    JAVOLUTION_DLL void start(org::osgi::framework::BundleContext context);

    // Implements BundleActivator.
    JAVOLUTION_DLL void stop(org::osgi::framework::BundleContext context);

    /**
     * Default constructor (private, class final).
     */
    JavolutionActivator_API() {
    }

private:

    // Check potential mismatch between header major version and runtime versions.
    JAVOLUTION_DLL static void checkVersion(javolution::lang::String majorVersion, char* ident);

};

#endif
