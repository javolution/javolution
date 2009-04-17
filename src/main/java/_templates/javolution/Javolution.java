/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution;

import _templates.java.lang.CharSequence;
import _templates.javolution.context.LogContext;
import _templates.javolution.testing.TestContext;
import _templates.javolution.testing.TimeContext;
import _templates.javolution.text.Text;
import _templates.javolution.text.TextBuilder;


/**
 * <p> This class contains the library {@link #main} method for
 *     versionning, self-tests, and performance analysis.</p>
 * <p> It is also the base class for the library benchmarks and 
 *     self-tests.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, September 14, 2004
 */
public class Javolution {

    /**
     * Holds the version information.
     */
    public final static String VERSION = "@VERSION@";

    /**
     * Default constructor.
     */
    protected Javolution() {
    }

    /**
     * The library {@link #main} method.
     * The archive <code>javolution.jar</code> is auto-executable.
     * [code]
     *    java -jar javolution.jar version <i>(show version information)</i>
     *    java -jar javolution.jar test <i>(perform self-tests)</i>
     *    java -jar javolution.jar time <i>(run benchmark)</i>
     * [/code]
     * Configurable are read from system properties.
     * 
     *
     * @param  args the option arguments.
     * @throws Exception if a problem occurs.
     */
    public static void main(String[] args) throws Exception {
        LogContext.enter(LogContext.SYSTEM_OUT); // Output results to System.out
        try {
            LogContext.info("Javolution - Java(TM) Solution for Real-Time and Embedded Systems");
            LogContext.info("Version " + VERSION + " (http://javolution.org)");
            LogContext.info("");
            if (args.length > 0) {
                if (args[0].equals("version"))
                    return;
                if (args[0].equals("test")) {
                    TestContext.enter();
                    try {
                        builtInTests();
                    } finally {
                        TestContext.exit();
                    }
                    return;
                }
                if (args[0].equals("time")) {
                    TimeContext.enter();
                    try {
                        builtInTests();
                    } finally {
                        TimeContext.exit();
                    }
                    return;
                }
            }
            LogContext.info("Usage: java -jar javolution.jar [arg]");
            LogContext.info("where arg is one of:");
            LogContext.info("    version (to show version information only)");
            LogContext.info("    test    (to validate, runs in TestContext)");
            LogContext.info("    time    (to benchmark, runs in TimeContext)");
        } finally {
            LogContext.exit(LogContext.SYSTEM_OUT);
        }
    }

    /**
     * Performs Built-In-Tests.
     */
    private static void builtInTests() throws Exception {

        /*@JVM-1.4+@
        LogContext.info("Load Configurable Parameters from System.getProperties()...");
        javolution.lang.Configurable.read(System.getProperties());
        LogContext.info("");
        /**/

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        TestContext.run(new ContextTestSuite());
        TestContext.run(new TypeFormatTestSuite());
     
        TestContext.info("");
        TestContext.info("More tests coming soon...");
    }

    ///////////////////////////////////////////////
    // Utilities for J2ME Backward Compatibility //
    ///////////////////////////////////////////////
    /**
     * Converts the specified String as CharSequence (String is a 
     * CharSequence only for J2SE 1.4+).
     * 
     * @param str the String to convert.
     * @return <code>this</code> or a text wrapper.
     */
    public static CharSequence j2meToCharSeq(Object str) {
        /*@JVM-1.4+@
        return (CharSequence) str;
        }
        private static Text dummy(Object str) { // Never used.
        /**/
        return str == null ? null : Text.valueOf(str);
    }
}
