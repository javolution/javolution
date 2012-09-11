/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _ORG_OSGI_FRAMEWORK_BUNDLE_CONTEXT_HPP
#define _ORG_OSGI_FRAMEWORK_BUNDLE_CONTEXT_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/util/Dictionary.hpp"
#include "org/osgi/framework/ServiceListener.hpp"
#include "org/osgi/framework/ServiceRegistration.hpp"
#include "org/osgi/framework/ServiceReference.hpp"
#include "org/osgi/framework/Bundle.hpp"
#include "org/osgi/framework/Filter.hpp"


namespace org {
    namespace osgi {
        namespace framework {
            class BundleContext_API;
            typedef Type::Handle<BundleContext_API> BundleContext;
        }
    }
}

/**
 * This interface represents a bundle's execution context within
 * the Framework. The context is used to grant access to other
 * methods so that this bundle can interact with the Framework.
 *
 * BundleContext methods allow a bundle to:<ul>
 *   <li>Subscribe to events published by the Framework.</li>
 *   <li>Register service objects with the Framework service registry.</li>
 *   <li>Retrieve ServiceReferences from the Framework service registry.</li>
 *   <li>Get and release service objects for a referenced service.</li>
 *   <li>Install new bundles in the Framework.</li>
 *   <li>Get the list of bundles installed in the Framework.</li>
 *   <li>Get the Bundle object for a bundle.</li>
 *   <li>Create File objects for files in a persistent storage area provided for the bundle by the Framework.</li>
 * </ul>
 *
 * @see  <a href="http://www.osgi.org/javadoc/r4v42/org/osgi/framework/BundleContext.html">
 *       OSGi - BundleContext</a>
 * @version 1.0
 */
class org::osgi::framework::BundleContext_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Returns the Bundle object associated with this BundleContext.
     */
    virtual Bundle getBundle() const = 0;

    /**
     * Registers the specified service object with the specified
     * properties under the specified class names into the framework.
     *
     * @param className the class name of the service which is registered.
     * @param service the pointer to the service object.
     * @param properties the properties object which describes the service object
     *        or <code>Dictionary.NULL</code> if none.
     * @return a service registration object for use by the bundle registering
     *         the service.
     */
    virtual ServiceRegistration registerService(javolution::lang::String className,
            javolution::lang::Object service, javolution::util::Dictionary properties) = 0;

    /**
     * Adds the specified ServiceListener object with the specified filter
     * to the context bundle's list of listeners. See Filter for a description
     * of the filter syntax. ServiceListener objects are notified when a service
     * has a lifecycle state change. If the context bundle's list of listeners
     * already contains a listener l such that (l==listener), then this method
     * replaces that listener's filter (which may be null) with the specified
     * one (which may be null).
     *
     * @param serviceListener the service listener object.
     * @param filter the filter criteria.
     */
    virtual void addServiceListener(ServiceListener serviceListener, javolution::lang::String filter) = 0;

    /**
     * Unregisters a service listener object.
     *
     * @param serviceListener the service listener object.
     */
    virtual void removeServiceListener(ServiceListener serviceListener) = 0;

    /**
     * Returns an array of ServiceReference objects. The returned array of
     * ServiceReference objects contains services that were registered under
     * the specified class and match the specified filter expression if any.
     *
     * @param clazz the class name with which the service was registered or <code>Type::Null</code> for all services.
     * @param filterExpression the filter expression or <code>Type::Null</code> for all services.
     * @return an array of ServiceReference objects or  <code>Type::Null</code>if no services are registered which satisfy the search.
     */
    virtual Type::Array<ServiceReference> getServiceReferences(javolution::lang::String clazz, javolution::lang::String filterExpression) const = 0;

    /**
     * Returns the service object referenced by the specified ServiceReference object.
     *
     * @param reference the service reference.
     * @return the actual service or <code>Object.NULL</code>
     */
    virtual javolution::lang::Object getService(ServiceReference reference) = 0;

    /**
     * Releases the service object referenced by the specified ServiceReference
     *  object.
     *
     * @param reference the service reference.
     * @return the actual service or <code>Object.NULL</code>
     */
    virtual Type::boolean ungetService(ServiceReference reference) = 0;

    /**
     * Creates a Filter object. This Filter object may be used to match a
     * ServiceReference object or a Dictionary object.
     *
     * @param the filter expression.
     * @return A Filter object encapsulating the filter string.
     */
    virtual Filter createFilter(javolution::lang::String filter) const = 0;

    /**
     * Returns a list of all installed bundles.
     */
    virtual Type::Array<Bundle> getBundles() const = 0;

};

#endif
