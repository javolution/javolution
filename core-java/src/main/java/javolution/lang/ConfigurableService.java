/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.lang;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javolution.context.LogContext;
import javolution.text.TextFormat;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 *  <p> This class represents the service responsible for the reconfiguration
 *      of {@link Configurable} elements.</p>

 *  <p> Bundles should make their OSGi container aware of their configurable
 *      services by registering them as ManagedService:
 *      [code]
 *      public class MyBundleActivator implements BundleActivator {
 *          ServiceRegistration registration;
 *          public void start(BundleContext context) throws Exception {
 *              ConfigurableService cs = new ConfigurableService("MyBundleConfigurableService"); // PID for Javolution is "JavolutionConfigurableService"
 *              registration = context.registerService(ManagedService.class.getName(), cs, cs.getProperties());
 *          }
 *          public void stop(BundleContext context) throws Exception {
 *              if (registration != null) {
 *                  registration.unregister();
 *                  registration = null;
 *              }
 *          }
 *      }[/code]
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.6, July 28, 2011
 */
public class ConfigurableService implements ManagedService {

    private Dictionary properties = new Hashtable();

    /**
     * Creates a configurable service having the specified PID identifier.
     *
     * @param pid the service pid (e.g. "MyBundleConfigurableService");
     */
    public ConfigurableService(String pid) {
        properties.put(Constants.SERVICE_PID, pid);
    }

    /**
     * Returns the properties for that managed service (holds the service.pid).
     *
     * @return the associated properties.
     */
    public Dictionary getProperties() {
        return properties;
    }

    public void updated(Dictionary dctnr) throws ConfigurationException {
        Enumeration e = dctnr.keys();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String textValue = (String) dctnr.get(name);
            Configurable cfg = Configurable.getInstance(name);
            if (cfg == null)
                continue;
            // Use the default value to retrieve the configurable type
            // and the associated textual format.
            Class type = cfg.getDefault().getClass();
            TextFormat format = TextFormat.getInstance(type);
            if (!format.isParsingSupported()) {
                LogContext.error("Cannot find suitable TextFormat to parse instances of " + type);
                continue;
            }
            Object newValue = format.parse(Configurable.toCsq(textValue));
            Configurable.configure(cfg, newValue);
        }
    }
}
