/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_SERVICE_EVENT_HPP
#define _ORG_OSGI_FRAMEWORK_SERVICE_EVENT_HPP

#include "javolution/lang/Object.hpp"
#include "org/osgi/framework/ServiceReference.hpp"

namespace org {
    namespace osgi {
        namespace framework {
            class ServiceEvent_API;
            typedef Type::Handle<ServiceEvent_API> ServiceEvent;
        }
    }
}

/**
 * This interface represents an event from the Framework describing
 * a service lifecycle change.
 *
 * ServiceEvent objects are delivered to ServiceListeners and
 * AllServiceListeners when a change occurs in this service's
 * lifecycle. A type code is used to identify the event type
 * for future extendability.
 *
 * OSGi Alliance reserves the right to extend the set of types.
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/ServiceEvent.html">
 *       OSGi - ServiceEvent</a>
 * @version 1.0
 */
class org::osgi::framework::ServiceEvent_API : public virtual javolution::lang::Object_API {
public:
	// Macro in Microsoft nb30.h file in collision with enum value.
    #undef REGISTERED

    /**
     * Event types.
     */
    static const Type::int32 REGISTERED = 1;

    static const Type::int32 MODIFIED = 2;

    static const Type::int32 UNREGISTERING = 4;

private:

    /**
     * The type of the event.
     */
    Type::int32 _type;

    /**
     * The service reference which represents the service whose lifecycle changed.
     */
    ServiceReference _reference;

public:

    /**
     * Creates instances of <code>ServiceEvent</code>.
     *
     * @param type the type of the event.
     * @param reference describes the service.
     */
    ServiceEvent_API(Type::int32 type, ServiceReference reference) {
        _type = type;
        _reference = reference;
    }
    /**
     * Returns the type of the event.
     *
     * @return the event type.
     */
    virtual Type::int32 getType() const {
        return _type;
    }

    /**
     * Returns the service reference.
     *
     * @return the <code>ServiceReference</code> object.
     */
    virtual ServiceReference const& getServiceReference() const {
        return _reference;
    }

};

#endif
