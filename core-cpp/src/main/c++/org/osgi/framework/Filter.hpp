/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_FILTER_HPP
#define _ORG_OSGI_FRAMEWORK_FILTER_HPP

#include "javolution/lang/Object.hpp"
#include "org/osgi/framework/ServiceReference.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class Filter_API;
            typedef Type::Handle<Filter_API> Filter;
        }
    }
}

/**
 * This interface represents an RFC 1960-based Filter.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/Filter.html">
 *       OSGi - Filter</a>
 * @version 1.0
 */
class org::osgi::framework::Filter_API  : public virtual javolution::lang::Object_API {
public:

    /**
     * Returns <code>true</code> if the service's properties match this Filter;
     * <code>false</code> otherwise.
     */
     virtual Type::boolean match(ServiceReference const& reference) const = 0;

};

#endif
