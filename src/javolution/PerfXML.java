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
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
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

    private static final int OBJECT_SIZE = 1000; // Nbr of tables per object.

    private static final int BYTE_BUFFER_SIZE = 1400 * OBJECT_SIZE;

    /**
     * Executes benchmark.
     */
    public void run() {
        println("/////////////////////////////");
        println("// Package: javolution.xml //");
        println("/////////////////////////////");

        println("");
        println("-- Java(TM) Serialization --");
        setOutputStream(null);
        for (int i=0; i < 10; i++) benchmarkJavaSerialization(); // Warming up.
        setOutputStream(System.out);
        benchmarkJavaSerialization();

        println("");
        println("-- XML Serialization (I/O Stream) --");
        setOutputStream(null);
        for (int i=0; i < 10; i++) benchmarkXmlIoSerialization(); // Warming up.
        setOutputStream(System.out);
        benchmarkXmlIoSerialization();

        println("");
        println("-- XML Serialization (NIO ByteBuffer) --");
        setOutputStream(null);
        for (int i=0; i < 10; i++) benchmarkXmlNioSerialization(); // Warming up.
        setOutputStream(System.out);
        benchmarkXmlNioSerialization();

        println("");
    }

    private void benchmarkJavaSerialization() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(
                    BYTE_BUFFER_SIZE);
            ObjectOutput oo = new ObjectOutputStream(out);
            Object data = newData();
            print("Write Time: ");
            startTime();
            oo.writeObject(data);
            oo.close();
            println(endTime(1));

            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            ObjectInput oi = new ObjectInputStream(in);
            print("Read Time: ");
            startTime();
            Object readObject = oi.readObject();
            oi.close();
            println(endTime(1));
            if (!data.equals(readObject)) 
                    throw new Error("SERIALIZATION ERROR");
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
            ByteArrayOutputStream out = new ByteArrayOutputStream(
                    BYTE_BUFFER_SIZE);
            XMLObjectWriter ow = XMLObjectWriter.newInstance(out);
            ow.setBinding(binding);
            Object data = newData();
            print("Write Time: ");
            startTime();
            ow.write(data);
            ow.close();
            println(endTime(1));
            //System.out.println(out); 

            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
           XMLObjectReader or = XMLObjectReader.newInstance(in);
            or.setBinding(binding);
            print("Read Time: ");
            startTime();
            Object readObject = or.read();
            or.close();
            println(endTime(1));
             //System.out.println(readObject); 
            if (!data.equals(readObject)) 
                throw new Error("SERIALIZATION ERROR");
        } catch (UnsupportedOperationException e) {
            println("NOT SUPPORTED (J2SE 1.4+ build required)");
        } catch (Throwable e) {
            throw new JavolutionError(e);
        }
    }

    private void benchmarkXmlNioSerialization() {
        try {
            ByteBuffer bb = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE);
            XMLObjectWriter ow = XMLObjectWriter.newInstance(new UTF8ByteBufferWriter().setOutput(bb));
            Object data = newData();
            print("Write Time: ");
            startTime();
            ow.write(data);
            ow.close();
            println(endTime(1));
            
            bb.flip();
            XMLObjectReader or = XMLObjectReader.newInstance(new UTF8ByteBufferReader().setInput(bb));
            print("Read Time: ");
            startTime();
            Object readObject = or.read();
            or.close();
            println(endTime(1));
            if (!data.equals(readObject)) 
                throw new Error("SERIALIZATION ERROR");
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
            fm.setKeyComparator(FastComparator.REHASH);
            fm.setValueComparator(FastComparator.IDENTITY);
            fm.put(new String("ONE"), Text.valueOf(1));
            fm.put(new String("TWO"), Text.valueOf(2));
            fm.put(new String("THREE"), Text.valueOf(3));
            v.add(fm);
            // Adds miscellaneous data.
            FastList fl = new FastList();
            fl.add("FIRST");
            fl.add("SECOND");
            fl.add("THIRD");
            fl.add("<...>");
            v.add(fl);
            FastSet fs = new FastSet();
            fs.add("ALPHA");
            fs.add("BETA");
            fs.add("GAMMA");
            v.add(fs);
        }
        return v;
    }
}