/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;
import j2me.lang.UnsupportedOperationException;

/**
 * <p> This class represents a local context; it is used to define locally 
 *     scoped environment settings.</p>
 * <p> These {@link LocalContext.Variable settings} are inherited by 
 *     {@link ConcurrentContext concurrent} executions when performed
 *     within the scope of the local context. For example:<pre>
 *     LocalContext.enter(); // Enters local context scope.
 *     try {
 *         MY_VAR.setValue(myValue);
 *         ConcurrentContext.enter();
 *         try {
 *             // Concurrent executions inherit local setting of MY_VAR
 *             ConcurrentContext.execute(...);
 *             ConcurrentContext.execute(...); 
 *         } finally {
 *             ConcurrentContext.exit();
 *         }
 *     } finally {
 *         LocalContext.exit(); // End of local context scope.
 *     }
 *     static final LocalContext.Variable MY_VAR = new LocalContext.Variable();
 *     </pre></p>
 * <p> Settings outside of a {@link LocalContext} scope, affects all threads
 *     (global default value). For example:<pre>
 *     public static final LocalContext.Variable DEBUG = new LocalContext.Variable();
 *     public static void main(String[] args) {
 *          DEBUG.setValue(new Boolean(true)); // Affects all threads.
 *          ...
 *     }</pre></p>
 * <p> Locally scoped settings are typically wrapped by a static method.
 *     For example:<pre>
 *        LargeInteger.setModulus(m); // Performs integer operations modulo m.
 *        Length.showAs(NonSI.INCH); // Shows length in inches.
 *        RelativisticModel.select(); // Uses relativistic physical model.
 *        QuantityFormat.getInstance(); // Returns current quantity format.
 *        XmlFormat.setInstance(f, Foo.class); // Sets XML format for Foo class.
 * </pre>
 *     </p>   
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public final class LocalContext extends Context {

    /**
     * Holds the local values.
     */
    private final Object[] _values = new Object[Variable.MAX];

    /**
     * Default constructor.
     */
    LocalContext() {
    }

    /**
     * Enters a {@link LocalContext}.
     */
    public static void enter() {
        LocalContext ctx = (LocalContext) push(LOCAL_CONTEXT_CLASS);
        if (ctx == null) {
            ctx = new LocalContext();
            push(ctx);
        }
    }
    private static final Class LOCAL_CONTEXT_CLASS = new LocalContext().getClass();

    /**
     * Exits the current {@link LocalContext}.
     *
     * @throws ClassCastException if the current context is not a
     *         {@link LocalContext}.
     */
    public static void exit() {
        LocalContext ctx = (LocalContext) pop();
        // Clears the local variables.
        System.arraycopy(_NULL_VALUES, 0, ctx._values, 0, Variable._Count);
    }
    private static final Object[] _NULL_VALUES = new Object[Variable.MAX];

    // Implements abstract method.
    protected void dispose() {
        // No resource allocated.
    }
    
    /**
     * <p> This class represents a {@link LocalContext} variable. The number of 
     *     instances of this class is voluntarely limited (see <a href=
     *     "{@docRoot}/overview-summary.html#configuration">Javolution's 
     *     Configuration</a> for details). Instances of this class should be 
     *     either <code>static</code> or member of persistent objects.</p>
     * <p> Accessing a local context variable is fast and does not necessitate
     *     internal synchronization or the use of <code>volatile</code> memory.
     *     Because a local context is visible only to the thread owner or to 
     *     {@link ConcurrentThread concurrent threads} which have been 
     *     synchronized, the Java memory model guarantees that local memory 
     *     (e.g. cache) has been flushed to main memory and the local context 
     *     settings are always inherited by inner {@link Context contexts}.</p>
     */
    public static class Variable {

        /**
         * Holds the maximum number of {@link LocalContext.Variable} (system 
         * property <code>"org.javolution.variables"</code>, default <code>1024</code>).
         */
        public static final int MAX;
        static {
            String str = System.getProperty("org.javolution.variables");
            MAX = (str != null) ? Integer.parseInt(str) : 1024;
        }

        /**
         * Holds the current number of variables.
         */
        private static volatile int _Count = 0;

        /**
         * Holds the default value for this variable.
         */
        private volatile Object _defaultValue;

        /**
         * Holds the index of this variable in a local context.
         */
        private final int _ctxIndex;

        /**
         * Default constructor (default variable's value is <code>null</code>).
         */
        public Variable() {
            this(null);
        }

        /**
         * Creates a context variable having the specified default value.
         * 
         * @param defaultValue the default value or root value of this variable.
         */
        public Variable(Object defaultValue) {
            _ctxIndex = index();
            _defaultValue = defaultValue;
        }
        private static synchronized int index() {
            if (_Count < MAX) {
                return _Count++;
            } else {
                throw new UnsupportedOperationException(
                        "Maximum number of variables  (system property "
                                + "\"javolution.variables\", value " + MAX
                                + ") has been reached");
            }
        }

        /**
         * Returns the context-local value of this variable.
         * The first outer {@link LocalContext} is searched first, then
         * all outer {@link LocalContext} are recursively searched up to the
         * global root context which contains the default values.
         *
         * @return the context-local value.
         */
        public Object getValue() {
            for (Context ctx = Context.currentContext(); ctx != null; ctx = ctx
                    .getOuter()) {
                if (ctx instanceof LocalContext) {
                    Object value = ((LocalContext) ctx)._values[_ctxIndex];
                    if (value != null) {
                        return value;
                    }
                }
            }
            // Not found, returns default value.
            return _defaultValue;
        }

        /**
         * Sets the context-local value of this variable.
         *
         * @param  value the new context-local value or <code>null</code>
         *         to inherit the value from outer context (if any).
         */
        public void setValue(Object value) {
            for (Context ctx = Context.currentContext(); ctx != null; ctx = ctx
                    .getOuter()) {
                if (ctx instanceof LocalContext) {
                    ((LocalContext) ctx)._values[_ctxIndex] = value;
                    return;
                }
            }
            // No local context, sets default value.
            _defaultValue = value;
        }
    }
}