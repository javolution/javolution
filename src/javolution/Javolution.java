/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
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

        new Perf_Io().run();
        new Perf_Lang().run();
        new Perf_Realtime().run();
        new Perf_Util().run();
        new Perf_Xml().run();

        println("");
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
        long nanoSeconds = nanoTime() - _time;
        long nanoDuration = nanoSeconds / iterations;
        if (nanoDuration > 10000000) { // 10 ms
            System.out.println(nanoDuration / 1000000 + "ms");
        } else if (nanoDuration > 10000) { // 10 µs
            System.out.println(nanoDuration / 1000 + "µs");
        } else {
            System.out.println(nanoDuration + "ns");
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

}