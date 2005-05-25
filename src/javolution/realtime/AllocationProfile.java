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
import javolution.lang.Reflection;
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;

import j2me.io.File;
import j2me.io.FileInputStream;
import j2me.io.FileNotFoundException;
import j2me.io.FileOutputStream;
import j2me.lang.IllegalStateException;

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
 *         for (Music music = getMusicToPlay(); music != null; music = music.getNext()) {
 *             AllocationProfile.preallocate(); // Delay ok, not started yet.
 *             play(music); // Real-time and fast (no object creation and no gc).
 *         }
 *         AllocationProfile.save(); // Saves allocation profile (optional).
 *     }</pre></p>
 *     
 * <p> Once loaded, the allocation profile is updated automatically during 
 *     program execution. Allocation profiles are typically generated from
 *     test runs (from an initially empty profile) and contain the maximum 
 *     number of allocations between preallocations or before recycling 
 *     for threads executing within a {@link PoolContext}.
 *     Here is an example of profile data (which can be manually edited):<pre>
 *     javolution.util.FastSet$1 20
 *     javolution.util.FastList$1 120
 *     javolution.util.FastList$2 0
 *     javolution.util.FastList$Node$1 5074
 *     javolution.util.FastList$FastListIterator$1 10002
 *     javolution.xml.CharacterData$1 3</pre></p> 
 *          
 * <p> To further decrease "first use" execution time, application might 
 *     also consider using {@link javolution.lang.PersistentReference 
 *     PersistentReference} to hold complex data structures to setup at
 *     start-up.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, March 12, 2005
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
     * Holds class name read/written.
     */
    private static final TextBuilder ClassName = new TextBuilder();

    /**
     * Holds counter read/written.
     */
    private static final TextBuilder Counter = new TextBuilder();

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
     * Enables/disables allocation profiling.
     * 
     * @param isEnabled <code>true</code> if allocation profile is enabled;
     *        <code>false</code> otherwise.
     */
    public static void setEnabled(boolean isEnabled) {
        ObjectFactory.IsAllocationProfileEnabled = isEnabled;
    }

    /**
     * Clears all preallocated object and reset Indicates if allocation profiling is enabled.
     * 
     * @return <code>true</code> if allocation profile is enabled;
     *        <code>false</code> otherwise.
     */
    public static boolean isEnabled() {
        return ObjectFactory.IsAllocationProfileEnabled;
    }

    /**
     * Loads the allocation profile previously {@link #save saved}
     * (equivalent to <code>load(new FileInputStream("profile.txt"))</code>).
     * Allocation profiling is automatically {@link #setEnabled enabled}.
     * 
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
            ObjectFactory.IsAllocationProfileEnabled = true;
            System.err.println("No initial allocation profile.");
        }
    }

    /**
     * Loads the allocation profile from the specified input stream and  
     * initialize classes having {@link ObjectFactory} instances (same 
     * classes and same order as it occured when the allocation profile was 
     * saved). 
     * Allocation profiling is automatically {@link #setEnabled enabled}.
     * 
     * @param in the input stream holding the allocation profile.
     */
    public static synchronized void load(InputStream in) {
        try {
            READER.setInputStream(in);
            for (int c = READER.read(); c >= 0; c = READER.read()) {
                if (c <= ' ')
                    continue; // Ignores any spacing character.

                // Reads class name.
                ClassName.append((char) c);
                for (c = READER.read(); c > ' '; c = READER.read()) {
                    ClassName.append((char) c);
                }

                // Reads counter on the same line (if any).
                while ((c >= 0) && (c != '\n') && (c <= ' ')) {
                    c = READER.read();
                }
                if (c > ' ') { // Find one. 
                    Counter.append((char) c);
                    for (c = READER.read(); c > ' '; c = READER.read()) {
                        Counter.append((char) c);
                    }
                }

                // Process line. 
                String factoryName = ClassName.toString();
                Class factoryClass = Reflection.getClass(factoryName);
                ClassName.reset();

                // Initializes container class.
                int sep = factoryName.lastIndexOf('$');
                Reflection.getClass(factoryName.substring(0, sep));

                // Searched for object factory.
                ObjectFactory factory = null;
                for (int j = 0; j < ObjectFactory.Count; j++) {
                    if (ObjectFactory.INSTANCES[j].getClass() == factoryClass) {
                        factory = ObjectFactory.INSTANCES[j];
                        break;
                    }
                }

                // Setup factory.
                if (factory != null) {
                    if (Counter.length() > 0) {
                        factory._preallocatedCount = TypeFormat
                                .parseInt(Counter);
                        Counter.reset();
                    }
                } else {
                    throw new Error("Factory class: " + factoryClass
                            + " not found");
                }
            }
        } catch (Throwable e) {
            throw new JavolutionError(e);
        } finally {
            READER.reset();
            ClassName.reset();
            Counter.reset();
            ObjectFactory.IsAllocationProfileEnabled = true;
        }
    }

    /**
     * Saves the current  allocation profile (equivalent to 
     * <code>save(new FileOutputStream("profile.txt"))</code>).
     * 
     * @throws IllegalStateException if allocation profiling is disabled.
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
     * @throws IllegalStateException if allocation profiling is disabled.
     */
    public static synchronized void save(OutputStream out) {
        if (!ObjectFactory.IsAllocationProfileEnabled) {
            throw new IllegalStateException("Allocation profiling disabled");
        }
        try {
            WRITER.setOutputStream(out);
            for (int i = 0; i < ObjectFactory.Count; i++) {
                ObjectFactory factory = ObjectFactory.INSTANCES[i];
                WRITER.write(factory.getClass().getName());
                WRITER.write(' ');
                int maxCount = factory._allocatedCount > factory._preallocatedCount ? factory._allocatedCount
                        : factory._preallocatedCount;
                Counter.append(maxCount);
                WRITER.write(Counter);
                Counter.reset();
                WRITER.write('\n');
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
     * 
     * @throws IllegalStateException if allocation profiling is disabled.
     */
    public static synchronized void preallocate() {
        if (ObjectFactory.IsAllocationProfileEnabled) {
            for (int i = 0; i < ObjectFactory.Count;) {
                ObjectFactory.INSTANCES[i++].preallocate();
            }
        } else {
            throw new IllegalStateException("Allocation profiling disabled");
        }
    }

    /**
     * Resets the current allocation profile. Allocation/preallocation
     * counters are reset; all preallocation pools are disposed 
     * (preallocated objects can then be garbage collected).
     * 
     * @throws IllegalStateException if allocation profiling is disabled.
     */
    public static void reset() {
        if (ObjectFactory.IsAllocationProfileEnabled) {
            for (int i = 0; i < ObjectFactory.Count;) {
                ObjectFactory.INSTANCES[i++].reset();
            }
        } else {
            throw new IllegalStateException("Allocation profiling disabled");
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
            if (ObjectFactory.IsAllocationProfileEnabled) {
                out.println("Current Allocation Profile:");
                for (int i = 0; i < ObjectFactory.Count;) {
                    ObjectFactory factory = ObjectFactory.INSTANCES[i++];
                    if (factory._allocatedCount == 0)
                        continue;
                    out.println(factory.getClass().getName()
                            + " has allocated " + factory._allocatedCount + " "
                            + factory._productClass.getName() + " out of "
                            + factory._preallocatedCount);

                }
            } else {
                out.print("Allocation profiling disabled");
            }
            out.println();
        }
    }

}