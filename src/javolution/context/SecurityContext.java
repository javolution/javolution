/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

/**
 * <p> This class represents a high-level security context (low level 
 *     security being addressed by the system security manager).</p>
 *     
 * <p> Applications may extend this base class to address specific security
 *     requirements. For example:[code]
 *     // This class defines custom policy with regards to database access. 
 *     public abstract class DatabaseAccess extends SecurityContext  {
 *         public static boolean isReadAllowed(Table table) {
 *             SecurityContext policy = SecurityContext.current();
 *             return (policy instanceof DatabaseAccess.Permission) ?
 *                 ((DatabaseAccess.Permission)policy).isReadable(table) : false;
 *         }
 *         public interface Permission { 
 *             boolean isReadable(Table table);
 *             boolean isWritable(Table table);
 *         }
 *     }[/code]</p>
 *     
 * <p> The use of interfaces (such as <code>Permission</code> above) makes 
 *     it easy for custom policies to support any security actions.
 *     For example:[code]
 *     class Policy extends SecurityContext implements DatabaseAccess.Permission, FileAccess.Permission {
 *          public boolean isReadable(Table table) { 
 *              return !table.isPrivate();
 *          }
 *          public boolean isWritable(Table table) { 
 *              return Session.getSession().getUser().isAdministrator();
 *          }
 *          public boolean isReadable(File file) { 
 *              return true;
 *          }
 *          public boolean isWritable(File file) { 
 *              return false;
 *          }
 *     }
 *     ...
 *     Policy myPolicy = new Policy();
 *     SecurityContext.enter(myPolicy);
 *     try {
 *         ...
 *         DatabaseAccess.isReadAllowed(table);   
 *         ...
 *         FileAccess.isWriteAllowed(file);
 *         ...
 *     } finally {
 *         SecurityContext.exit(myPolicy);
 *     }[/code]</p>    
 *     
 * <p> The only permission managed by the root {@link SecurityContext} class
 *     is the permission to {@link #isReplaceable replace} the current security
 *     context (<code>true</code> by default).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.0, April 18, 2007
 */
public abstract class SecurityContext extends Context {

    /**
     * Holds the default security context.
     */
    private static volatile SecurityContext _Default 
        = new SecurityContext() { };

    /**
     * Default constructor.
     */
    protected SecurityContext() {
    }

    /**
     * Returns the current security context (or {@link #getDefault()}
     * if the current thread has not entered any security context).
     *
     * @return the current security context.
     */
    public static/*SecurityContext*/Context current() {
        for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof SecurityContext)
                return (SecurityContext) ctx;
        }
        return SecurityContext._Default;
    }

    /**
     * Returns the default security context for new threads.
     * 
     * @return the default security context.
     */
    public static SecurityContext getDefault() {
        return SecurityContext._Default;
    }

    /**
     * Sets the specified security context as default (throws 
     * {@link SecurityException} if the {@link #getDefault() default}
     * security context is not {@link #isReplaceable replaceable}).
     * 
     * @param context the default security context.
     * @throws SecurityException if the default security context is not 
     *        replaceable.
     */
    public static void setDefault(SecurityContext context) throws SecurityException {
        if (!SecurityContext._Default.isReplaceable()) 
            throw new SecurityException("Default Security Context not Replaceable");
        SecurityContext._Default = context;
    }

    // Implements Context abstract method.
    protected final void enterAction() {
        // Checks if the previous security context is replaceable.
        SecurityContext previousPolicy = SecurityContext._Default;
        for (Context ctx = this.getOuter(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof SecurityContext) {
                previousPolicy = (SecurityContext) ctx;
                break;
            }
        }
        if (!previousPolicy.isReplaceable()) 
            throw new SecurityException("Current Security Context not Replaceable");
    }

    // Implements Context abstract method.
    protected final void exitAction() {
        // Do nothing.
    }

    /**
     * Indicates if this security context can be replaced by another one 
     * (default <code>true</code>). Applications may override this method
     * to return <code>false</code> and prevent untrusted code to override
     * the current security policy. 
     * 
     * @return <code>true</code> if a new security context can be entered;
     *         <code>false</code> otherwise.
     */
    public boolean isReplaceable () {
        return true;
    }    
}

