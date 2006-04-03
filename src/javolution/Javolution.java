/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import java.io.PrintStream;

import javolution.lang.Reflection;
import javolution.lang.TextBuilder;

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
     * Holds the current output stream (default System.out).
     */
    private static PrintStream Out = System.out;

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
     *    java -jar javolution.jar perf <i>(run benchmark)</i>
     * [/code]
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
        // TBD
        println("Success");
    }
    
    /**
     * Measures performance.
     */
    private static void benchmark() throws Exception {
        println("Benchmark...");
        println("");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        //new Perf_Io().run();
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
        if (Javolution.Out == null) return;
        Javolution.Out.println(obj);
    }

    /**
     * Prints an object to <code>System.out</code> (no line termination). 
     * 
     * @param obj the object to be displayed.
     */
    public static void print(Object obj) {
        if (Javolution.Out == null) return;
        Javolution.Out.print(obj);
    }

    /**
     * Runs garbage collector then starts measuring time.
     */
    public static void startTime() {
        System.gc();
        try {
            Thread.sleep(500); // Allows gc to do its work.
        } catch (InterruptedException e) {
            throw new JavolutionError(e);
        }
        _time = nanoTime();
    }

    /**
     * Sets the output stream. 
     * 
     * @param out the print stream or <code>null</code> to disable output.
     */
    public static void setOutputStream(PrintStream out) {
        Javolution.Out = out;
    }

    /**
     * Ends measuring time and display the execution time per iteration.
     * 
     * @param iterations the number iterations performed since 
     *        {@link #startTime}.
     */
    public static String endTime(int iterations) {
        long nanoSeconds = nanoTime() - _time;
        long picoDuration = nanoSeconds * 1000 / iterations;
        long divisor;
        String unit;
        if (picoDuration > 1000 * 1000 * 1000 * 1000L) { // 1 s
            unit = " s";
            divisor = 1000 * 1000 * 1000 * 1000L;
        } else if (picoDuration > 1000 * 1000 * 1000L) {
            unit = " ms";
            divisor = 1000 * 1000 * 1000L;
        } else if (picoDuration > 1000 * 1000L) {
            unit = " us";
            divisor = 1000 * 1000L;
        } else {
            unit = " ns";
            divisor = 1000L;
        }
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(picoDuration / divisor);
        int fracDigits = 4 - tb.length(); // 4 digits precision.
        tb.append(".");
        for (int i=0, j=10; i < fracDigits; i++, j *= 10) {
            tb.append((picoDuration * j / divisor) % 10);
        }
        return tb.append(unit).toString();
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