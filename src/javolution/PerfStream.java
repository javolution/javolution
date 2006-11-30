/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * <p> This class holds {@link javolution.xml.stream} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, September 4, 2004
 */
final class PerfStream extends Javolution implements Runnable {

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    ByteArrayInputStream in;

    /** 
     * Executes benchmark.
     */
    public void run() {
        println("////////////////////////////////////");
        println("// Package: javolution.xml.stream //");
        println("////////////////////////////////////");
        println("");

        try {
            /*@JVM-1.6+@
            print("StAX XMLStreamWriter (namespace repairing disabled): ");
            for (int i = 0; i < 1100; i++) {
                if (i == 100)
                    startTime(); // To avoid counting warm-up time.
                staxStreamWriter(false);
                out.reset();
            }
            println(endTime(1000));
            /**/

            /*@JVM-1.4+@
            print("Javolution XMLStreamWriter (namespace repairing disabled): ");
            for (int i = 0; i < 1100; i++) {
                if (i == 100)
                    startTime(); // To avoid counting warm-up time.
                javoStreamWriter(false);
                out.reset();
            }
            println(endTime(1000));
            /**/

            /*@JVM-1.6+@
            print("StAX XMLStreamWriter (namespace repairing enabled): ");
            for (int i = 0; i < 1100; i++) {
                if (i == 100)
                    startTime(); // To avoid counting warm-up time.
                staxStreamWriter(true);
                out.reset();
            }
            println(endTime(1000));
            /**/

            /*@JVM-1.4+@
            print("Javolution XMLStreamWriter (namespace repairing enabled): ");
            for (int i = 0; i < 1100; i++) {
                if (i == 100)
                    startTime(); // To avoid counting warm-up time.
                javoStreamWriter(true);
                out.reset();
            }
            println(endTime(1000));
            /**/

            /**/
            /*@JVM-1.6+@
            print("StAX XMLStreamReader: ");
            staxStreamWriter(false);
            in = new ByteArrayInputStream(out.toByteArray());
            out.reset();
            for (int i = 0; i < 1100; i++) {
                if (i == 100)
                    startTime(); // To avoid counting warm-up time.
                staxStreamReader();
                in.reset();
            }
            println(endTime(1000));
            /**/

            /*@JVM-1.4+@
            print("Javolution XMLStreamReader: ");
            javoStreamWriter(false);
            in = new ByteArrayInputStream(out.toByteArray());
            out.reset();
            for (int i = 0; i < 1100; i++) {
                if (i == 100)
                    startTime(); // To avoid counting warm-up time.
                javoStreamReader();
                in.reset();
            }
            println(endTime(1000));
            /**/

            println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**/
    }

    /*@JVM-1.6+@
    private void staxStreamWriter(boolean repairingNamespaces) throws Exception {
        javax.xml.stream.XMLOutputFactory outFactory = javax.xml.stream.XMLOutputFactory.newInstance();
        outFactory.setProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES, new Boolean(repairingNamespaces));
        javax.xml.stream.XMLStreamWriter writer = outFactory.createXMLStreamWriter(out);

        // Block identical for StAX or Javolution.
        writer.writeStartDocument();
        writer.setPrefix("ns0", "http://javolution.org/namespace/0");
        writer.setPrefix("ns1", "http://javolution.org/namespace/1");
        writer.setPrefix("ns2", "http://javolution.org/namespace/2");
        writer.setPrefix("ns3", "http://javolution.org/namespace/3");
        writer.writeStartElement("http://javolution.org/namespace/0", "Element_0");
        if (!repairingNamespaces) {
            writer.writeNamespace("ns0", "http://javolution.org/namespace/0");
            writer.writeNamespace("ns1", "http://javolution.org/namespace/1");
            writer.writeNamespace("ns2", "http://javolution.org/namespace/2");
            writer.writeNamespace("ns3", "http://javolution.org/namespace/3");
        }
        for (int i = 0; i < 10; i++) {
            writer.writeStartElement("http://javolution.org/namespace/1",
                    "Element_1");
            for (int j = 0; j < 10; j++) {
                writer.writeStartElement("http://javolution.org/namespace/2",
                        "Element_2");
                for (int k = 0; k < 10; k++) {
                    writer.writeEmptyElement("http://javolution.org/namespace/3",
                            "Element_3");
                    writer.writeAttribute("http://javolution.org/namespace/1",
                            "Attribute_1", "Value_1");
                    writer.writeAttribute("http://javolution.org/namespace/2",
                            "Attribute_2", "Value_2");
                    writer.writeAttribute("http://javolution.org/namespace/3",
                            "Attribute_3", "Value_3");
                    writer.writeCData("This is a block of CDATA being written");
                    writer.writeCharacters("Here characters (&,<,>) are escaped");
                    writer.writeCData("And finally a last CDATA block to be merged (coalescing performed)");
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndDocument();
        writer.close();
    }
    private void staxStreamReader() throws Exception {
        javax.xml.stream.XMLInputFactory inFactory = javax.xml.stream.XMLInputFactory.newInstance();
        inFactory.setProperty(javax.xml.stream.XMLInputFactory.IS_COALESCING, new Boolean(true));
        javax.xml.stream.XMLStreamReader reader = inFactory.createXMLStreamReader(in);

        // Block identical for StAX or Javolution.
        while (reader.hasNext()) {
            reader.next();
            if (reader.isStartElement()) {
                if (reader.getLocalName().equals("Element_3")) {
                    if (!reader.getNamespaceURI().equals("http://javolution.org/namespace/3"))
                        throw new Error();
                    if (!reader.getAttributeValue("http://javolution.org/namespace/1", "Attribute_1").equals("Value_1"))
                        throw new Error();
                    if (!reader.getAttributeValue("http://javolution.org/namespace/2", "Attribute_2").equals("Value_2"))
                        throw new Error();
                    if (!reader.getAttributeValue("http://javolution.org/namespace/3", "Attribute_3").equals("Value_3"))
                        throw new Error();
                }
            } else if (reader.isCharacters()) {
                if (reader.getText().length() < 80)
                    throw new Error();
            }
        }
        reader.close();
    }
    /**/
    
    /*@JVM-1.4+@
    private void javoStreamWriter(boolean repairingNamespaces) throws Exception {
        javolution.xml.stream.XMLOutputFactory outFactory = javolution.xml.stream.XMLOutputFactory.newInstance();
        outFactory.setProperty(javolution.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES, new Boolean(repairingNamespaces));
        javolution.xml.stream.XMLStreamWriter writer = outFactory.createXMLStreamWriter(out);

        // Block identical for StAX or Javolution.
        writer.writeStartDocument();
        writer.setPrefix("ns0", "http://javolution.org/namespace/0");
        writer.setPrefix("ns1", "http://javolution.org/namespace/1");
        writer.setPrefix("ns2", "http://javolution.org/namespace/2");
        writer.setPrefix("ns3", "http://javolution.org/namespace/3");
        writer.writeStartElement("http://javolution.org/namespace/0", "Element_0");
        if (!repairingNamespaces) {
            writer.writeNamespace("ns0", "http://javolution.org/namespace/0");
            writer.writeNamespace("ns1", "http://javolution.org/namespace/1");
            writer.writeNamespace("ns2", "http://javolution.org/namespace/2");
            writer.writeNamespace("ns3", "http://javolution.org/namespace/3");
        }
        for (int i = 0; i < 10; i++) {
            writer.writeStartElement("http://javolution.org/namespace/1",
                    "Element_1");
            for (int j = 0; j < 10; j++) {
                writer.writeStartElement("http://javolution.org/namespace/2",
                        "Element_2");
                for (int k = 0; k < 10; k++) {
                    writer.writeEmptyElement("http://javolution.org/namespace/3",
                            "Element_3");
                    writer.writeAttribute("http://javolution.org/namespace/1",
                            "Attribute_1", "Value_1");
                    writer.writeAttribute("http://javolution.org/namespace/2",
                            "Attribute_2", "Value_2");
                    writer.writeAttribute("http://javolution.org/namespace/3",
                            "Attribute_3", "Value_3");
                    writer.writeCData("This is a block of CDATA being written");
                    writer.writeCharacters("Here characters (&,<,>) are escaped");
                    writer.writeCData("And finally a last CDATA block to be merged (coalescing performed)");
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndDocument();
        writer.close();
    }
    private void javoStreamReader() throws Exception {
        javolution.xml.stream.XMLInputFactory inFactory = javolution.xml.stream.XMLInputFactory.newInstance();
        inFactory.setProperty(javolution.xml.stream.XMLInputFactory.IS_COALESCING, new Boolean(true));
        javolution.xml.stream.XMLStreamReader reader = inFactory.createXMLStreamReader(in);

        // Block identical for StAX or Javolution.
        while (reader.hasNext()) {
            reader.next();
            if (reader.isStartElement()) {
                if (reader.getLocalName().equals("Element_3")) {
                    if (!reader.getNamespaceURI().equals("http://javolution.org/namespace/3"))
                        throw new Error();
                    if (!reader.getAttributeValue("http://javolution.org/namespace/1", "Attribute_1").equals("Value_1"))
                        throw new Error();
                    if (!reader.getAttributeValue("http://javolution.org/namespace/2", "Attribute_2").equals("Value_2"))
                        throw new Error();
                    if (!reader.getAttributeValue("http://javolution.org/namespace/3", "Attribute_3").equals("Value_3"))
                        throw new Error();
                }
            } else if (reader.isCharacters()) {
                if (reader.getText().length() < 80)
                    throw new Error();
            }
        }
        reader.close();
    }
    /**/
};
