/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_CONSTANTS_HPP
#define _ORG_OSGI_FRAMEWORK_CONSTANTS_HPP

#include "javolution/lang/String.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class Constants_API;
        }
    }
}

/**
 * This interface defines standard names for the OSGi environment system
 * properties, service properties, and Manifest header attribute keys.
 * The values associated with these keys are of type String, unless otherwise
 * indicated.represents qn installed bundle in the Framework.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/Constants.html">
 *       OSGi - Constants</a>
 * @version 1.0
 */
class org::osgi::framework::Constants_API  {

	Constants_API() {} // Private constructor (utility class)

public:

    /**
     * Manifest header identifying the bundle's name.
     * The attribute value may be retrieved from the Dictionary object
     * returned by the Bundle.getHeaders method.
     */
    JAVOLUTION_DLL static const javolution::lang::String BUNDLE_NAME;

    /**
     * Manifest header identifying the bundle's version.
     * The attribute value may be retrieved from the Dictionary object returned
     * by the Bundle.getHeaders method.
     */
    JAVOLUTION_DLL static const javolution::lang::String BUNDLE_VERSION;

    /**
     * Manifest header identifying the bundle's vendor.
     * The attribute value may be retrieved from the Dictionary object returned
     * by the Bundle.getHeaders method.
     */
    JAVOLUTION_DLL static const javolution::lang::String BUNDLE_VENDOR;

    /**
     * Service property identifying all of the class names under which a service
     * was registered in the Framework. The value of this property must be of
     * type String.
     */
    JAVOLUTION_DLL static const javolution::lang::String OBJECTCLASS;
        
};

#endif
