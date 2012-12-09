/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;

/**
 *  <p> This class represents a light OSGi framework which can be
 *      used to run bundles as "standard" java applications. This can 
 *      be particularly useful for testing/debugging purpose.
 *      This requires that the bundle activator class is a public class 
 *      [code]
 *      public void main(String[] args) throws Exception {
 *           OSGi osgi = OSGi.newInstance();
 *           osgi.start("Javolution bundle", new javolution.osgi.JavolutionActivator());
 *           osgi.start("Foo bundle", new org.foo.osgi.FooActivator());
 *           Thread.sleep(10000);
 *           osgi.stop("bundle1");
 *           osgi.stop("bundle2");
 *       }
 *       [/code]
 * </p>
 *
 */
public abstract class OSGi {

    /**
     * Default constructor.
     */
    protected OSGi() {
    }

    /**
     * Returns a new OSGi instance.
     */
    public static OSGi newInstance() {
        return new OSGiImpl();
    }

    /**
     * Starts a bundle having the specified identifier and activator.
     *
     * @param bundleId the identifier of the bundle.
     * @param activator the activator for the bundle.
     */
    public abstract void start(String bundleId, BundleActivator activator);

    /**
     * Stops a bundle of this OSGi framework.
     *
     * @param bundleId the identifier of the bundle.
     */
    public abstract void stop(String bundleId);

    /**
     * Returns a bundle from this OSGi framework.
     *
     * @param bundleId the identifier of the bundle.
     */
    public abstract Bundle getBundle(String bundleId);

}
