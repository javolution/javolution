/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.realtime;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javolution.JavolutionError;
import javolution.io.Utf8StreamReader;
import javolution.io.Utf8StreamWriter;
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;
import javolution.util.Reflection;

import j2me.io.File;
import j2me.io.FileInputStream;
import j2me.io.FileNotFoundException;
import j2me.io.FileOutputStream;

/**
 * <p> This class represents a view of the current {@link ObjectFactory 
 *     object factories} allocation profile; it facilitates class initialization
 *     and object pre-allocation based on previous or current allocation 
 *     profile.</p>
 *     
 * <p> Preallocations can be performed at start-up (using the allocation profile
 *     saved from previous executions) or at any appropriate time. For example:
 *     <pre>
 *     void main(String[]) {
 *         AllocationProfile.load(); // Loads allocation profile from previous executions.
 *         Music music = getMusicToPlay();
 *         while (music != null) {
 *             AllocationProfile.preallocate();
 *             play(music); // Real-time and fast (no object creation and no gc).
 *         }
 *         AllocationProfile.save(); // Saves allocation profile (optional).
 *     }</pre>
 *     
 * <p> Threads executing in a {@link PoolContext} do not have to continuously 
 *     preallocate as their objects are automatically recycled.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 20, 2004
 */
public final class AllocationProfile {

    /**
     * Holds UTF8 reader.
     */
    private static final Utf8StreamReader READER = new Utf8StreamReader();

    /**
     * Holds UTF8 writer.
     */
    private static final Utf8StreamWriter WRITER = new Utf8StreamWriter();

    /**
     * Holds the overflow handler.
     */
    volatile static Runnable OverflowHandler;
    
    /**
     * Default constructor.
     */
    private AllocationProfile() {
    }

    /**
     * Loads the allocation profile previously {@link #save saved}
     * (equivalent to <code>load(new FileInputStream("profile.txt"))</code>).
     * 
     * @throws j2me.lang.UnsupportedOperationException on J2ME platform.
     * @see #load(InputStream)
     */
    public static void load() {
        File file = new File("profile.txt");
        if (file.exists()) {
            try {
                load(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new JavolutionError(e);
            }
        } else {
            System.err.println("Default allocation profile not found.");
        }
    }

    /**
     * Loads the allocation profile from the specified input stream and  
     * initialize classes having {@link ObjectFactory} instances (same 
     * classes and same order as it occured when the allocation profile was 
     * saved).
     * 
     * @param in the input stream holding the allocation profile.
     */
    public static synchronized void load(InputStream in) {
        try {
            READER.setInputStream(in);
            int state = READ_FACTORY;
            while (true) {
                int i = READER.read();
                switch (state) {
                case READ_FACTORY:
                    if (i > ' ') {
                        Factory.append((char) i);
                        state = FACTORY;
                    }
                    break;
                case FACTORY:
                    if (i > ' ') {
                        Factory.append((char) i);
                    } else {
                        state = READ_COUNTER;
                    }
                    break;
                case READ_COUNTER:
                    if (i > ' ') {
                        Counter.append((char) i);
                        state = COUNTER;
                    }
                    break;
                case COUNTER:
                    if (i > ' ') {
                        Counter.append((char) i);
                    } else {
                        String factoryName = Factory.toString();
                        Class factoryClass = Reflection.getClass(factoryName);
                        
                        // Initializes container class.
                        int sep = factoryName.lastIndexOf('$');
                        Reflection.getClass(factoryName.substring(0, sep));

                        boolean found = false;
                        for (int j = 0; j < ObjectFactory.Count;) {
                            ObjectFactory of = ObjectFactory.INSTANCES[j++];
                            if (of.getClass() == factoryClass) {
                                of._allocatedCount = TypeFormat.parseInt(Counter);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            throw new Error("Factory class: " + factoryClass
                                    + " not found");
                        }
                        Factory.reset();
                        Counter.reset();
                        state = READ_FACTORY;
                    }
                    break;
                }
                if (i < 0) {
                    break; // End of stream.
                }
            }
        } catch (Throwable e) {
            throw new JavolutionError(e);
        } finally {
            READER.reset();
            Factory.reset();
            Counter.reset();
        }
    }

    private static final TextBuilder Factory = new TextBuilder();

    private static final TextBuilder Counter = new TextBuilder();

    private static final int READ_FACTORY = 0;

    private static final int FACTORY = 1;

    private static final int READ_COUNTER = 2;

    private static final int COUNTER = 3;

    /**
     * Saves the current  allocation profile (equivalent to 
     * <code>save(new FileOutputStream("profile.txt"))</code>).
     * 
     * @throws j2me.lang.UnsupportedOperationException on J2ME platform.
     * @see #save(OutputStream)
     */
    public static void save() {
        File file = new File("profile.txt");
        try {
            save(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new JavolutionError(e);
        }
    }

    /**
     * Saves the current allocation profile to the specified output source
     * destination.
     * 
     * @param out the target destination.
     */
    public static synchronized void save(OutputStream out) {
        try {
            WRITER.setOutputStream(out);
            for (int i = 0; i < ObjectFactory.Count;) {
                ObjectFactory of = ObjectFactory.INSTANCES[i++];
                WRITER.write(of.getClass().getName());
                WRITER.write(' ');
                int maxCount = of._allocatedCount > of._preallocatedCount ?
                        of._allocatedCount : of._preallocatedCount;
                Counter.append(maxCount);
                WRITER.write(Counter);
                WRITER.write('\n');
                Counter.reset();
            }
            WRITER.close();
        } catch (Throwable e) {
                throw new JavolutionError(e);
        } finally {
            WRITER.reset();
            Counter.reset();
        }
    }

    /**
     * Preallocates object based upon the current allocation profile.
     * In case of repetitive preallocations, as many objects are created 
     * as it has been consumed since the last preallocation.
     */
    public static synchronized void preallocate() {
        for (int i = 0; i < ObjectFactory.Count;) {
            ObjectFactory.INSTANCES[i++].preallocate();
        }
    }
    
    /**
     * Sets the handler to call when a factory exceeds its quota of 
     * pre-allocated object. The default handler ignores such event.
     */  
    public static void setOverflowHandler(Runnable handler) {
        AllocationProfile.OverflowHandler = handler;
    }

    /**
     * Prints the current allocation profile statistics.
     *  
     * @param out the stream to use for output (e.g. <code>System.out</code>)
     */
    public static void print(PrintStream out) {
        synchronized (out) {
            out.println("Current Allocation Profile:");
            for (int i = 0; i < ObjectFactory.Count;) {
                ObjectFactory of = ObjectFactory.INSTANCES[i++];
                out.print("The factory " + of.getClass().getName());
                out.print(" has allocated " + of._allocatedCount);
                out.println(" objects out of " + of._preallocatedCount);
            }
            out.println();
        }        
    }

}