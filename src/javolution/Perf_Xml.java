/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
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

	private static final int OBJECT_SIZE = 1000; // Nbr of strings per
											     // object.

	private static final int BYTE_BUFFER_SIZE = 50 * OBJECT_SIZE + 200;

	/**
	 * Executes benchmark.
	 */
	public void run() {
		// Create a dummy object.
	    Vector object = new Vector(OBJECT_SIZE);
		for (int i = 0; i < OBJECT_SIZE; i++) {
			object.addElement("This is the string #" + i);
		}

		
		println("");
		println("-- Java(TM) Serialization --");
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(BYTE_BUFFER_SIZE);
    		ObjectOutput oo = new ObjectOutputStream(out);
			print("Write Time: ");
			startTime();
    		oo.writeObject(object);
   		    oo.close();
			endTime(1);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            ObjectInput oi = new ObjectInputStream(in);
            print("Read Time: ");
            startTime();
            Object readObject = oi.readObject();
            oi.close();
            endTime(1);
            if (!object.equals(readObject)) {
                throw new Error("SERIALIZATION ERROR");
            }
		} catch (UnsupportedOperationException e) {
		    println("NOT SUPPORTED");
		} catch (Throwable e) {
			throw new JavolutionError(e);
        }
		
        println("");
        println("-- XML Serialization (I/O Stream) --");
        try {
            // Defines xml for Vector (not a Collection class for the CLDC platform).
            XmlFormat vectorXml = new XmlFormat() {
                public void format(Object obj, XmlElement xml) {
                    Vector v = (Vector)obj;
                    for (int i=0; i < v.size(); i++) {
                        xml.add(v.elementAt(i));
                    }
                }

                public Object parse(XmlElement xml) {
                    Vector v = new Vector(xml.size());
                    for (int i=0; i < xml.size(); i++) {
                        v.addElement(xml.get(i));
                    }
                    return v;
                }
            };
            XmlFormat.setInstance(vectorXml, new Vector().getClass()); 

            ObjectWriter ow = new ObjectWriter();
            ow.setNamespace("", "java.lang");
			ByteArrayOutputStream out = new ByteArrayOutputStream(BYTE_BUFFER_SIZE);
            print("Write Time: ");
            startTime();
            ow.write(object, out);
            endTime(1);
            ObjectReader or = new ObjectReader();
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            print("Read Time: ");
            startTime();
            Object readObject = or.read(in);
            endTime(1);
            if (!object.equals(readObject)) {
                throw new Error("SERIALIZATION ERROR");
            }
		} catch (IOException e) {
			throw new JavolutionError(e);
		}
        
        println("");
		println("-- XML Serialization (NIO ByteBuffer) --");
        try {
            ObjectWriter ow = new ObjectWriter();
            ow.setNamespace("", "java.lang");
			ByteBuffer bb = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE);
            print("Write Time: ");
            startTime();
            ow.write(object, bb);
            endTime(1);
            ObjectReader or = new ObjectReader();
            bb.flip();
            print("Read Time: ");
            startTime();
            Object readObject = or.read(bb);
            endTime(1);
            if (!object.equals(readObject)) {
                throw new Error("SERIALIZATION ERROR");
            }
		} catch (IOException e) {
			throw new JavolutionError(e);
		}

		println("");
	}
}