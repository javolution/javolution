/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.javolution.internal.osgi;

import java.io.File;
import java.lang.CharSequence;

import org.javolution.context.LogContext;
import org.javolution.lang.Configurable;
import org.javolution.lang.ConfigurableService;
import org.javolution.util.StandardLog;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.*;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class implements Javolution OSGi bundle activator.
 */
public class BundleActivatorImpl implements BundleActivator {

    private ServiceTracker logServiceTracker;
    private ServiceRegistration configurableServiceRegistration;

    public class OSGiLog extends StandardLog {
         protected void logDebug(CharSequence message) {
             LogService logService = (LogService) logServiceTracker.getService();
             if (logService != null) {
                 logService.log(LogService.LOG_DEBUG, message.toString());
             } else { // Reverts to default.
                 super.logDebug(message);
             }
         }
         protected void logInfo(CharSequence message) {
             LogService logService = (LogService) logServiceTracker.getService();
             if (logService != null) {
                 logService.log(LogService.LOG_INFO, message.toString());
             } else { // Reverts to default.
                 super.logInfo(message);
             }
         }
         protected void logWarning(CharSequence message) {
             LogService logService = (LogService) logServiceTracker.getService();
             if (logService != null) {
                 logService.log(LogService.LOG_WARNING, message.toString());
             } else { // Reverts to default.
                 super.logWarning(message);
             }
         }
         protected void logError(Throwable error, CharSequence message) {
             LogService logService = (LogService) logServiceTracker.getService();
             if (logService != null) {
                 logService.log(LogService.LOG_ERROR, message.toString(), error);
             } else { // Reverts to default.
                 super.logError(error, message);
             }
         }
    }

    public void start(BundleContext bc) throws Exception {
        // Tracks log services.
        logServiceTracker = new ServiceTracker(bc, org.osgi.service.log.LogService.class.getName(), null);
        logServiceTracker.open();

        // Configure the default LogContext to be the OSGiLog instance.
        Configurable.configure(LogContext.DEFAULT, OSGiLog.class);

        // Publish Javolution Configuration service.
        ConfigurableService cs = new ConfigurableService("JavolutionConfigurableService");
        configurableServiceRegistration = bc.registerService(ManagedService.class.getName(), cs, cs.getProperties());
    }

    public void stop(BundleContext bc) throws Exception {
        if (configurableServiceRegistration != null) {
            configurableServiceRegistration.unregister();
            configurableServiceRegistration = null;
        }
    }

}
