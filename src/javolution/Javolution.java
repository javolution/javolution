/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import j2me.lang.CharSequence;

import java.io.PrintStream;
import javolution.context.LogContext;
import javolution.text.Text;
import javolution.text.TextBuilder;

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
        Out.println("Javolution - Java(TM) Solution for Real-Time and Embedded Systems");
        Out.println("Version " + VERSION + " (http://javolution.org)");
        Out.println("");
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
        Out.println("Usage: java -jar javolution.jar [arg]");
        Out.println("where arg is one of:");
        Out.println("    version (to show version information only)");
        Out.println("    test    (to perform self-tests)");
        Out.println("    perf    (to run benchmark)");
    }

    /**
     * Performs simple tests.
     * 
     * @throws Exception if a problem occurs.
     */
    private static void testing() throws Exception {
        Out.print("Testing...");
        Out.println("");
        // TBD
        Out.println("Success");
    }

    /**
     * Measures performance.
     */
    private static void benchmark() throws Exception {
        LogContext.setDefault(LogContext.STANDARD); // Logs info messages to console. 
        calibrate(); // Calculates timer offset.
        
        Out.println("Run benchmark... (shortest execution times are displayed)");
        Out.println("");
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        //new PerfIO().run();
        new PerfContext().run();
        new PerfText().run();
        new PerfStream().run();
        new PerfXML().run();
        new PerfUtil().run();

        Out.println("More performance analysis in future versions...");
    }
    
    private static void calibrate() {
        Javolution test = new Javolution();
        for (int i=0; i < 1000; i++) {
            test.startTime();
            test.keepBestTime(1);
        }
        _TimerOffset = test._picoDuration / 1000;
       
    }
    static long _TimerOffset = 0;

    /**
     * Prints an object to <code>System.out</code> and then terminates the line. 
     * 
     * @param obj the object to be displayed.
     */
    public void println(Object obj) {
        Out.println(obj);
    }

    /**
     * Prints an object to current print stream. 
     * 
     * @param obj the object to be displayed.
     */
    public void print(Object obj) {
        Out.print(obj);
    }

    /**
     * Starts timer.
     */
    public void startTime() {
        if (_time != 0) throw new Error("Timer not reset");
        _time = nanoTime();
    }

    /**
     * Sets the output stream. 
     * 
     * @param out the print stream.
     */
    public static void setOutputStream(PrintStream out) {
        Out = out;
    }

    /**
     * Ends measuring time and keeps the best time.
     * 
     * @param iterations the number iterations performed since 
     *        {@link #startTime}.
     */
    public void keepBestTime(int iterations) {
        long nanoSeconds = nanoTime() - _time - _TimerOffset;
        long picoDuration = (nanoSeconds * 1000 / iterations);
        if (picoDuration < _picoDuration) {
            _picoDuration = picoDuration;
        }
        _time = 0;
    }
    private long _picoDuration = Long.MAX_VALUE;
    
    /**
     * Ends measuring time and returns the best time.
     * 
     * @param iterations the number iterations performed since 
     *        {@link #startTime}.
     */
    public String endTime() {
        long divisor;
        String unit;
        if (_picoDuration > 1000 * 1000 * 1000 * 1000L) { // 1 s
            unit = " s";
            divisor = 1000 * 1000 * 1000 * 1000L;
        } else if (_picoDuration > 1000 * 1000 * 1000L) {
            unit = " ms";
            divisor = 1000 * 1000 * 1000L;
        } else if (_picoDuration > 1000 * 1000L) {
            unit = " us";
            divisor = 1000 * 1000L;
        } else {
            unit = " ns";
            divisor = 1000L;
        }
        TextBuilder tb = TextBuilder.newInstance();
        tb.append(_picoDuration / divisor);
        int fracDigits = 4 - tb.length(); // 4 digits precision.
        tb.append(".");
        for (int i = 0, j = 10; i < fracDigits; i++, j *= 10) {
            tb.append((_picoDuration * j / divisor) % 10);
        }
        _picoDuration = Long.MAX_VALUE;
        return tb.append(unit).toString();
    }

    private static long _time;

    private static long nanoTime() {
        /*@JVM-1.5+@        
        if (true) return System.nanoTime();
        /**/
       return System.currentTimeMillis() * 1000000;
    }

    //////////////////////////////////////
    // Utilities for Javolution use only.
    //

    /**
     * Returns the class having the specified name; for 
     * backward compatibility with CLDC 1.0 (cannot use .class as exception 
     * java.lang.NoClassDefFoundError does not exist for that platform).
     */
    public static Class j2meGetClass(String name) {
        Class cls = null;
        try {
            cls = Class.forName(name); // Caller class loader.
        } catch (ClassNotFoundException e0) { // Try context class loader.
            /*@JVM-1.4+@
             try {
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
             cls = Class.forName(name, true, cl);
             } catch (ClassNotFoundException e1) { // Try system class loader.
             ClassLoader cl = ClassLoader.getSystemClassLoader();
             try {
             cls = Class.forName(name, true, cl);
             } catch (ClassNotFoundException e) {
             }
             }
             /**/
        }
        if (cls == null)
            throw new JavolutionError("Class " + name + " not found");
        return cls;
    }

    /**
     * Converts the specified String as CharSequence (String is a 
     * CharSequence only for J2SE 1.4+).
     * 
     * @param str the String to convert.
     * @return <code>this</code> or a text wrapper.
     */
    public static CharSequence j2meToCharSeq(Object str) {
        return (str instanceof CharSequence) ? (CharSequence) str
                : (str == null) ? null : Text.valueOf((String) str);
    }
}