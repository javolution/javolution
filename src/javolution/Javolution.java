/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.util.Reflection;

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
     * <pre>
     *    java -jar javolution.jar version <i>(show version information)</i>
     *    java -jar javolution.jar test <i>(perform self-tests)</i>
     *    java -jar javolution.jar perf <i>(run benchmark)</i>
     * </pre>
     *
     * @param  args the option arguments.
     * @throws Exception if a problem occurs.
     */
    public static void main(String[] args) throws Exception {
        println("Javolution - Java(TM) Solution for Real-Time and Embedded Systems");
        println("Version " + VERSION + " (http://javolution.org)");
        println("");
        if (args.length > 0) {
            if (args[0].equals("version")) {
                return;
            } else if (args[0].equals("test")) {
                testing();
                return;
            } else if (args[0].equals("perf")) {
                benchmark();
                return;
            }
        }
        println("Usage: java -jar javolution.jar [arg]");
        println("where arg is one of:");
        println("    version (to show version information only)");
        println("    test    (to perform self-tests)");
        println("    perf    (to run benchmark)");
    }

    /**
     * Performs simple tests.
     * 
     * @throws Exception if a problem occurs.
     */
    private static void testing() throws Exception {
        print("Testing...");
        println("");
        println("Success");
    }

    /**
     * Measures performance.
     */
    private static void benchmark() throws Exception {
        println("Benchmark...");
        println("");
        
        println("////////////////////////////");
        println("// Package: javolution.io //");
        println("////////////////////////////");
        new Perf_Io().run();
        
        println("//////////////////////////////");
        println("// Package: javolution.lang //");
        println("//////////////////////////////");
        new Perf_Lang().run();
        
        println("//////////////////////////////////");
        println("// Package: javolution.realtime //");
        println("//////////////////////////////////");
        new Perf_Realtime().run();
        
        println("//////////////////////////////");
        println("// Package: javolution.util //");
        println("//////////////////////////////");
        new Perf_Util().run();
        
        println("/////////////////////////////");
        println("// Package: javolution.xml //");
        println("/////////////////////////////");
        new Perf_Xml().run();
        
        println("More performance analysis in future versions...");
    }

    /**
     * Prints an object to <code>System.out</code> and then terminates the line. 
     * 
     * @param obj the object to be displayed.
     */
    public static void println(Object obj) {
        System.out.println(obj);
    }

    /**
     * Prints an object to <code>System.out</code> (no line termination). 
     * 
     * @param obj the object to be displayed.
     */
    public static void print(Object obj) {
        System.out.print(obj);
    }

    /**
     * Starts measuring time.
     */
    public static void startTime() {
        _time = nanoTime();
    }

    /**
     * Ends measuring time and display the execution time per iteration.
     * 
     * @param iterations the number iterations performed since 
     *        {@link #startTime}.
     */
    public static void endTime(int iterations) {
        long milliSeconds = ((nanoTime() - _time) + 500000) / 1000000;
        double duration = ((double) milliSeconds) / iterations;
        if (duration < 1e-3) {
            System.out.println(((float) (duration * 1e6)) + "ns");
        } else if (duration < 1) {
            System.out.println(((float) (duration * 1e3)) + "µs");
        } else {
            System.out.println(((float) duration) + "ms");
        }
    }

    private static long _time;

    private static long nanoTime() {
        if (NANO_TIME_METHOD != null) { // JRE 1.5+
            Long time = (Long) NANO_TIME_METHOD.invoke(null);
            return time.longValue();
        } else { // Use the less accurate time in milliseconds.
            return System.currentTimeMillis() * 1000000;
        }
    }

    private static final Reflection.Method NANO_TIME_METHOD = Reflection
            .getMethod("java.lang.System.nanoTime()");

    /**
     *  Signals that a serious problem (bug ?!) has been detected 
     *  within the library.
     */
    public static final class InternalError extends Error {

        /**
         * Creates an error message with the specified message 
         * and cause.
         * 
         * @param  message the detail message.
         * @param  cause the cause or <code>null</code> if the cause 
         *         is nonexistent or unknown.
         * @throws Error (always) 
         */
        public InternalError(String message) {
            super(message);
        }

        /**
         * Creates an error message with the specified message 
         * and cause. The cause stack trace is printed to the
         * current error stream (System.err).
         * 
         * @param  message the detailed message.
         * @param  cause the cause of this error.
         */
        public InternalError(String message, Throwable cause) {
            super(message);
            cause.printStackTrace();
        }

        /**
         * Creates an error message with the specified cause 
         * The cause stack trace is printed to the current error
         * stream (System.err).
         * 
         * @param  cause the cause of this error.
         */
        public InternalError(Throwable cause) {
            cause.printStackTrace();
        }

        private static final long serialVersionUID = 3257291335412299833L;
    }

}