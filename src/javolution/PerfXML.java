/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import j2me.io.ObjectInputStream;
import j2me.io.ObjectOutputStream;
import j2me.io.ObjectInput;
import j2me.io.ObjectOutput;
import j2me.lang.UnsupportedOperationException;
import j2me.nio.ByteBuffer;
import javolution.io.UTF8ByteBufferReader;
import javolution.io.UTF8ByteBufferWriter;
import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
import javolution.util.Index;
import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;

/**
 * <p> This class holds {@link javolution.xml} benchmark.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 3.6, March 28, 2005
 */
final class PerfXML extends Javolution implements Runnable {

    private static final int ITERATIONS = 1000; // The nbr of iterations.

    private static final int OBJECT_SIZE = 1; // Nbr of tables per object.

    private static final int BUFFER_SIZE = 1400 * OBJECT_SIZE;

    private ByteArrayOutputStream _Stream = new ByteArrayOutputStream(
            BUFFER_SIZE);

    private ByteBuffer _Buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    /**
     * Executes benchmark.
     */
    public void run() {
        println("/////////////////////////////");
        println("// Package: javolution.xml //");
        println("/////////////////////////////");

        println("");
        println("-- Java(TM) Serialization --");
        benchmarkJavaSerialization(); // Warming up.

        println("");
        println("-- XML Serialization (I/O Stream) --");
        benchmarkXmlIoSerialization(); // Warming up.

        println("");
        println("-- XML Serialization (NIO ByteBuffer) --");
        benchmarkXmlNioSerialization(); // Warming up.

        println("");
    }

    private void benchmarkJavaSerialization() {
        try {
            print("Write Time: ");
            for (int i = 0; i < ITERATIONS; i++) {
                ObjectOutput oo = new ObjectOutputStream(_Stream);
                Object data = newData();
                startTime();
                oo.writeObject(data);
                oo.close();
                keepBestTime(1);
                _Stream.reset();
            }
            println(endTime());

            print("Read Time: ");
            for (int i = 0; i < ITERATIONS; i++) {
                // Creates input.
                ObjectOutput oo = new ObjectOutputStream(_Stream);
                Object data = newData();
                oo.writeObject(data);
                oo.close();
                ByteArrayInputStream in = new ByteArrayInputStream(_Stream
                        .toByteArray());
                _Stream.reset();

                ObjectInput oi = new ObjectInputStream(in);
                startTime();
                Object readObject = oi.readObject();
                oi.close();
                keepBestTime(1);                
                if (!data.equals(readObject))
                    throw new Error("SERIALIZATION ERROR");
            }
            println(endTime());
        } catch (UnsupportedOperationException e) {
            println("NOT SUPPORTED (J2SE 1.4+ build required)");
        } catch (Throwable e) {
            throw new JavolutionError(e);
        }
    }

    private void benchmarkXmlIoSerialization() {
        XMLBinding binding = new XMLBinding();
        binding.setAlias(String.class, "String");
        try {
            print("Write Time: ");
            for (int i = 0; i < ITERATIONS; i++) {
                XMLObjectWriter ow = XMLObjectWriter.newInstance(_Stream);
                ow.setBinding(binding);
                Object data = newData();
                startTime();
                ow.write(data);
                ow.close();
                keepBestTime(1);
                _Stream.reset();
            }
            println(endTime());

            print("Read Time: ");
            for (int i = 0; i < ITERATIONS; i++) {
                // Creates input.
                XMLObjectWriter ow = XMLObjectWriter.newInstance(_Stream);
                ow.setBinding(binding);
                Object data = newData();
                ow.write(data);
                ow.close();
                ByteArrayInputStream in = new ByteArrayInputStream(_Stream
                        .toByteArray());
                _Stream.reset();

                XMLObjectReader or = XMLObjectReader.newInstance(in);
                or.setBinding(binding);
                startTime();
                Object readObject = or.read();
                or.close();
                keepBestTime(1);
                if (!data.equals(readObject))
                    throw new Error("SERIALIZATION ERROR");
            }

            println(endTime());

        } catch (UnsupportedOperationException e) {
            println("NOT SUPPORTED (J2SE 1.4+ build required)");
        } catch (Throwable e) {
            throw new JavolutionError(e);
        }
    }

    private void benchmarkXmlNioSerialization() {
        try {
            print("Write Time: ");
            for (int i = 0; i < ITERATIONS; i++) {
                XMLObjectWriter ow = XMLObjectWriter
                        .newInstance(new UTF8ByteBufferWriter()
                                .setOutput(_Buffer));
                Object data = newData();
                startTime();
                ow.write(data);
                ow.close();
                keepBestTime(1);
                _Buffer.clear();
            }
            println(endTime());

            print("Read Time: ");
            for (int i = 0; i < ITERATIONS; i++) {
                // Creates input.
                XMLObjectWriter ow = XMLObjectWriter
                        .newInstance(new UTF8ByteBufferWriter()
                                .setOutput(_Buffer));
                Object data = newData();
                ow.write(data);
                ow.close();
                _Buffer.flip();

                XMLObjectReader or = XMLObjectReader
                        .newInstance(new UTF8ByteBufferReader()
                                .setInput(_Buffer));
                startTime();
                Object readObject = or.read();
                or.close();
                keepBestTime(1);
                if (!data.equals(readObject))
                    throw new Error("SERIALIZATION ERROR");
                _Buffer.clear();
            }
            println(endTime());
        } catch (UnsupportedOperationException e) {
            println("NOT SUPPORTED (J2SE 1.4+ build required)");
        } catch (Throwable e) {
            throw new JavolutionError(e);
        }
    }

    private static Object newData() {
        FastTable v = new FastTable(OBJECT_SIZE);
        for (int i = 0; i < OBJECT_SIZE; i++) {
            FastTable ft = new FastTable();
            ft.add("This is the first String (" + i + ")");
            ft.add("This is the second String (" + i + ")");
            ft.add("This is the third String (" + i + ")");
            v.add(ft);
            v.add(null);
            v.add(TextBuilder.newInstance().append(Long.MAX_VALUE));
            v.add(Text.valueOf(Long.MAX_VALUE, 16));
            FastMap fm = new FastMap();
            fm.setKeyComparator(FastComparator.IDENTITY);
            fm.put(Index.valueOf(i), "INDEX+" + i);
            fm.put(Index.valueOf(-i), "INDEX-" + i);
            v.add(fm);
            // Adds miscellaneous data.
            FastList fl = new FastList();
            fl.add("FIRST" + "(" + i + ")");
            fl.add("SECOND" + "(" + i + ")");
            fl.add("THIRD" + "(" + i + ")");
            fl.add("<&'>>>");
            v.add(fl);
            FastSet fs = new FastSet();
            fs.add(new Integer(MathLib.random(Integer.MIN_VALUE,
                    Integer.MAX_VALUE)));
            fs.add(new Long(MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE)));
            fs.add("".getClass());
            v.add(fs);
        }
        return v;
    }
}