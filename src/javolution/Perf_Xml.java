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
import java.io.IOException;
import java.util.Vector;

import j2me.io.ObjectInputStream;
import j2me.io.ObjectOutputStream;
import j2me.io.ObjectInput;
import j2me.io.ObjectOutput;
import j2me.lang.UnsupportedOperationException;
import j2me.nio.ByteBuffer;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
import javolution.xml.CharacterData;
import javolution.xml.ObjectReader;
import javolution.xml.ObjectWriter;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

/**
 * <p> This class holds {@link javolution.xml} benchmark.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle </a>
 * @version 2.0, November 26, 2004
 */
final class Perf_Xml extends Javolution implements Runnable {

    private static final int OBJECT_SIZE = 10000; // Nbr of string tables per object.

    private static final int BYTE_BUFFER_SIZE = 300 * OBJECT_SIZE + 2000;

    /**
     * Executes benchmark.
     */
    public void run() {
        println("/////////////////////////////");
        println("// Package: javolution.xml //");
        println("/////////////////////////////");

        // Create a dummy object (vector).
        Vector v = new Vector(OBJECT_SIZE);
        for (int i = 0; i < OBJECT_SIZE; i++) {
            FastTable ft = new FastTable();
            ft.add("This is the first String");
            ft.add("This is the second String");
            ft.add("This is the third String");
            v.addElement(ft);
        }
        // Adds miscellaneous data.
        v.addElement(null);
        v.addElement(TextBuilder.newInstance().append(Long.MAX_VALUE));
        v.addElement(Text.valueOf(Long.MAX_VALUE, 16));
        String str = "<<< Some character data >>>";
        CharacterData charData = CharacterData.valueOf(Text.valueOf(str));
        v.addElement(charData);
        FastMap fm = new FastMap();
        fm.setKeyComparator(FastComparator.REHASH);
        fm.setValueComparator(FastComparator.IDENTITY);

        fm.put("ONE", "1");
        fm.put("TWO", "2");
        fm.put("THREE", "3");
        v.addElement(fm);
        FastList fl = new FastList();
        fl.add("FIRST");
        fl.add("SECOND");
        fl.add("THIRD");
        fl.add("...");
        fl.add("...");
        fl.add("...");
        v.addElement(fl);
        FastSet fs = new FastSet();
        fs.add("ALPHA");
        fs.add("BETA");
        fs.add("ALPHA");
        v.addElement(fs);
        FastTable ft = new FastTable();
        ft.setValueComparator(FastComparator.LEXICAL);
        ft.add("UN");
        ft.add("DEUX");
        ft.add("TROIS");
        v.addElement(ft);

        // Example of xml format for Vector with circular reference support.
        XmlFormat vectorXml = new XmlFormat() {
            public Object allocate(XmlElement xml) {
                return new Vector(xml.getAttribute("size", 0));
            }

            public void format(Object obj, XmlElement xml) {
                Vector vector = (Vector) obj;
                xml.setAttribute("size", vector.size());
                for (int i = 0; i < vector.size(); i++) {
                    xml.add(vector.elementAt(i));
                }
            }

            public Object parse(XmlElement xml) {
                Vector vector = (Vector) xml.object(); // Preallocated instance.
                while (xml.hasNext()) {
                    vector.addElement(xml.getNext());
                }
                return vector;
            }
        };
        XmlFormat.setFormat(v.getClass(), vectorXml);

        println("");
        println("-- Java(TM) Serialization --");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(
                    BYTE_BUFFER_SIZE);
            ObjectOutput oo = new ObjectOutputStream(out);
            print("Write Time: ");
            startTime();
            oo.writeObject(v);
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
            for (int i = 0; i < v.size(); i++) {
                Object e0 = v.elementAt(i);
                Object e1 = ((Vector) readObject).elementAt(i);
                if (((e0 != null) && !e0.equals(e1))
                        || ((e0 == null) && (e1 != null)))
                    throw new Error("SERIALIZATION ERROR");
            }
        } catch (UnsupportedOperationException e) {
            println("NOT SUPPORTED (J2SE 1.4+ build required)");
        } catch (Throwable e) {
            throw new JavolutionError(e);
        }

        println("");
        println("-- XML Serialization (I/O Stream) --");
        try {
            ObjectWriter ow = new ObjectWriter();
            //ow.setReferencesEnabled(true);
            //ow.setExpandReferences(true);
            //ow.setPackagePrefix("", "java.lang");
            ByteArrayOutputStream out = new ByteArrayOutputStream(
                    BYTE_BUFFER_SIZE);
            print("Write Time: ");
            startTime();
            ow.write(v, out);
            println(endTime(1));
            //System.out.println(out); 

            ObjectReader or = new ObjectReader();
            ByteArrayInputStream in = new ByteArrayInputStream(out
                    .toByteArray());
            print("Read Time: ");
            startTime();
            Object readObject = or.read(in);
            println(endTime(1));
            //System.out.println(readObject); 
            for (int i = 0; i < v.size(); i++) {
                Object e0 = v.elementAt(i);
                Object e1 = ((Vector) readObject).elementAt(i);
                if (((e0 != null) && !e0.equals(e1))
                        || ((e0 == null) && (e1 != null)))
                    throw new Error("SERIALIZATION ERROR");
            }
        } catch (IOException e) {
            throw new JavolutionError(e);
        }

        println("");
        println("-- XML Serialization (NIO ByteBuffer) --");
        try {
            ObjectWriter ow = new ObjectWriter();
            ByteBuffer bb = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE);
            //ow.setReferencesEnabled(true);
            //ow.setExpandReferences(true);
            //ow.setPackagePrefix("", "java.lang");
            print("Write Time: ");
            startTime();
            ow.write(v, bb);
            println(endTime(1));
            ObjectReader or = new ObjectReader();
            bb.flip();
            print("Read Time: ");
            startTime();
            Object readObject = or.read(bb);
            println(endTime(1));
            for (int i = 0; i < v.size(); i++) {
                Object e0 = v.elementAt(i);
                Object e1 = ((Vector) readObject).elementAt(i);
                if (((e0 != null) && !e0.equals(e1))
                        || ((e0 == null) && (e1 != null)))
                    throw new Error("SERIALIZATION ERROR");
            }
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
        println("");
    }
}