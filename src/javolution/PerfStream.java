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

    static final int N = 1; // Number of elements (in/out).
    
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
            for (int i=0; i < 1000; i++) {
                startTime(); 
                staxStreamWriter(false); 
                keepBestTime(1);
                out.reset();
            }
            println(endTime());
            /**/

            /*@JVM-1.4+@        
            print("Javolution XMLStreamWriter (namespace repairing disabled): ");
            for (int i=0; i < 1000; i++) {
                startTime(); 
                javoStreamWriter(false); 
                keepBestTime(1);
                out.reset();
            }
            println(endTime());
            /**/

            /*@JVM-1.6+@        
            print("StAX XMLStreamWriter (namespace repairing enabled): ");
            for (int i=0; i < 1000; i++) {
                startTime(); 
                staxStreamWriter(true); 
                keepBestTime(1);
                out.reset();
            }
            println(endTime());
            /**/

            /*@JVM-1.4+@        
            print("Javolution XMLStreamWriter (namespace repairing enabled): ");
            for (int i=0; i < 1000; i++) {
                startTime(); 
                javoStreamWriter(true); 
                keepBestTime(1);
                out.reset();
            }
            println(endTime());
            /**/

            /*@JVM-1.6+@        
            print("StAX XMLStreamReader: ");
            for (int i=0; i < 1000; i++) {
                staxCreateInput();
                startTime(); 
                staxStreamReader();
                keepBestTime(1);
            }
            println(endTime());
            /**/

            /*@JVM-1.4+@        
            print("Javolution XMLStreamReader: ");
            for (int i=0; i < 1000; i++) {
                javoCreateInput();
                startTime(); 
                javoStreamReader();
                keepBestTime(1);
            }
            println(endTime());
            /**/

            println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@JVM-1.6+@
    private void staxStreamWriter(boolean repairingNamespaces) throws Exception {
        javax.xml.stream.XMLOutputFactory outFactory = javax.xml.stream.XMLOutputFactory.newInstance();
        outFactory.setProperty(javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES, new Boolean(repairingNamespaces));
        javax.xml.stream.XMLStreamWriter writer = outFactory.createXMLStreamWriter(out, "UTF-8");

        // Block identical for StAX or Javolution.
        //
        writer.writeStartDocument("UTF-8", "1.0");
        writer.setPrefix("ns0", "http://javolution.org/namespace/root");
        writer.setPrefix("ns1", "http://javolution.org/namespace/first");
        writer.setPrefix("ns2", "http://javolution.org/namespace/second");
        writer.setPrefix("ns3", "http://javolution.org/namespace/third");
        writer.writeStartElement("http://javolution.org/namespace/root", "Element_0");
        if (!repairingNamespaces) {
            writer.writeNamespace("ns0", "http://javolution.org/namespace/root");
            writer.writeNamespace("ns1", "http://javolution.org/namespace/first");
            writer.writeNamespace("ns2", "http://javolution.org/namespace/second");
            writer.writeNamespace("ns3", "http://javolution.org/namespace/third");
        }
        for (int i = 0; i < N; i++) {
            writer.writeStartElement("http://javolution.org/namespace/first",
                    "Element_1");
            for (int j = 0; j < 10; j++) {
                writer.writeStartElement("http://javolution.org/namespace/second",
                        "Element_2");
                for (int k = 0; k < 10; k++) {
                    writer.writeEmptyElement("http://javolution.org/namespace/third",
                            "Element_3");
                    writer.writeAttribute("http://javolution.org/namespace/first",
                            "Attribute_1", "Value of first attribute");
                    writer.writeAttribute("http://javolution.org/namespace/second",
                            "Attribute_2", "Value of second attribute");
                    writer.writeAttribute("http://javolution.org/namespace/third",
                            "Attribute_3", "Value of third attribute");
                    writer.writeCData("This is a block of CDATA being written");
                    writer.writeCharacters("Here characters (&,<,>) are escaped");
                    writer.writeCData("And finally a last CDATA block to be merged (coalescing performed)");
                    writer.writeStartElement("http://javolution.org/namespace/root", "TextOnlyElement");
                    writer.writeCharacters("Here are some characters"); // 24 characters
                    writer.writeComment("Comments are ignored !!");
                    writer.writeCharacters("More characters to be coalesced"); // 31 characters
                    writer.writeCData("The final text should be 97 character long"); // 42 characters
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndDocument();
        writer.close();
        //
        // End Block.
    }
    /**/
    
    /*@JVM-1.4+@        
    private void javoStreamWriter(boolean repairingNamespaces) throws Exception {
        javolution.xml.stream.XMLOutputFactory outFactory = javolution.xml.stream.XMLOutputFactory.newInstance();
        outFactory.setProperty(javolution.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES, new Boolean(repairingNamespaces));
        javolution.xml.stream.XMLStreamWriter writer = outFactory.createXMLStreamWriter(out, "UTF-8");

        // Block identical for StAX or Javolution.
        //
        writer.writeStartDocument();
        writer.setPrefix("ns0", "http://javolution.org/namespace/root");
        writer.setPrefix("ns1", "http://javolution.org/namespace/first");
        writer.setPrefix("ns2", "http://javolution.org/namespace/second");
        writer.setPrefix("ns3", "http://javolution.org/namespace/third");
        writer.writeStartElement("http://javolution.org/namespace/root", "Element_0");
        if (!repairingNamespaces) {
            writer.writeNamespace("ns0", "http://javolution.org/namespace/root");
            writer.writeNamespace("ns1", "http://javolution.org/namespace/first");
            writer.writeNamespace("ns2", "http://javolution.org/namespace/second");
            writer.writeNamespace("ns3", "http://javolution.org/namespace/third");
        }
        for (int i = 0; i < N; i++) {
            writer.writeStartElement("http://javolution.org/namespace/first",
                    "Element_1");
            for (int j = 0; j < 10; j++) {
                writer.writeStartElement("http://javolution.org/namespace/second",
                        "Element_2");
                for (int k = 0; k < 10; k++) {
                    writer.writeEmptyElement("http://javolution.org/namespace/third",
                            "Element_3");
                    writer.writeAttribute("http://javolution.org/namespace/first",
                            "Attribute_1", "Value of first attribute");
                    writer.writeAttribute("http://javolution.org/namespace/second",
                            "Attribute_2", "Value of second attribute");
                    writer.writeAttribute("http://javolution.org/namespace/third",
                            "Attribute_3", "Value of third attribute");
                    writer.writeCData("This is a block of CDATA being written");
                    writer.writeCharacters("Here characters (&,<,>) are escaped");
                    writer.writeCData("And finally a last CDATA block to be merged (coalescing performed)");
                    writer.writeStartElement("http://javolution.org/namespace/root",
                            "TextOnlyElement");
                    writer.writeCharacters("Here are some characters"); // 24 characters
                    writer.writeComment("Comments are ignored");
                    writer.writeCharacters("More characters to be coalesced"); // 31 characters
                    writer.writeCData("The final text should be 97 character long"); // 42 characters
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndDocument();
        writer.close();
        //
        // End Block.
    }
    /**/
    
    /*@JVM-1.6+@
    private void staxStreamReader() throws Exception {
        javax.xml.stream.XMLInputFactory inFactory = javax.xml.stream.XMLInputFactory.newInstance();
        inFactory.setProperty(javax.xml.stream.XMLInputFactory.IS_COALESCING, new Boolean(true));
        javax.xml.stream.XMLStreamReader reader = inFactory.createXMLStreamReader(in);
        final int startElement = javax.xml.stream.XMLStreamConstants.START_ELEMENT;

        // Block identical for StAX or Javolution.
        //
        if (!reader.getEncoding().equals("UTF-8")) throw new Error("Wrong encoding");
        while (reader.hasNext()) {
            reader.next();
            if (reader.isStartElement()) {
                if (reader.getLocalName().equals("Element_3")) {
                    if (!reader.getNamespaceURI().equals("http://javolution.org/namespace/third"))
                        throw new Error();
                    if (reader.getAttributeValue("http://javolution.org/namespace/first", "Attribute_1") == null)
                        throw new Error();
                    if (reader.getAttributeValue("http://javolution.org/namespace/second", "Attribute_2") == null)
                        throw new Error();
                    if (reader.getAttributeValue("http://javolution.org/namespace/third", "Attribute_3") == null)
                        throw new Error();
                    reader.next();
                    if (!reader.isEndElement()) throw new Error();
                    reader.next();
                    if (!reader.isCharacters() || (reader.getText().length() < 80))
                         throw new Error(); // Coalescing not performed.
                    reader.next();
                    reader.require(startElement, "http://javolution.org/namespace/root", "TextOnlyElement");
                    int textLength = reader.getElementText().length();
                    if (textLength != 97) throw new Error("Length: " + textLength);      
                } 
            }
        }
        reader.close(); 
        //
        // End Block.
    }
    private void staxCreateInput() throws Exception {
        in = null;
        staxStreamWriter(false);
        System.gc();
        in = new ByteArrayInputStream(out.toByteArray());
        out.reset();
    }
    /**/
    
    /*@JVM-1.4+@        
    private void javoStreamReader() throws Exception {
        javolution.xml.stream.XMLInputFactory inFactory = javolution.xml.stream.XMLInputFactory.newInstance();
        inFactory.setProperty(javolution.xml.stream.XMLInputFactory.IS_COALESCING, new Boolean(true));
        javolution.xml.stream.XMLStreamReader reader = inFactory.createXMLStreamReader(in);
        final int startElement = javolution.xml.stream.XMLStreamConstants.START_ELEMENT;

        // Block identical for StAX or Javolution.
        //
        if (!reader.getEncoding().equals("UTF-8")) throw new Error("Wrong encoding");
        while (reader.hasNext()) {
            reader.next();
            if (reader.isStartElement()) {
                if (reader.getLocalName().equals("Element_3")) {
                    if (!reader.getNamespaceURI().equals("http://javolution.org/namespace/third"))
                        throw new Error();
                    if (reader.getAttributeValue("http://javolution.org/namespace/first", "Attribute_1") == null)
                        throw new Error();
                    if (reader.getAttributeValue("http://javolution.org/namespace/second", "Attribute_2") == null)
                        throw new Error();
                    if (reader.getAttributeValue("http://javolution.org/namespace/third", "Attribute_3") == null)
                        throw new Error();
                    reader.next();
                    if (!reader.isEndElement()) throw new Error();
                    reader.next();
                    if (!reader.isCharacters() || (reader.getText().length() < 80))
                         throw new Error(reader.getText().toString()); // Coalescing not performed.
                    reader.next();
                    reader.require(startElement, "http://javolution.org/namespace/root", "TextOnlyElement");
                    int textLength = reader.getElementText().length();
                    if (textLength != 97) throw new Error("Length: " + textLength);      
                } 
            }
        }
        reader.close(); 
        //
        // End Block.
    }
    private void javoCreateInput() throws Exception {
         in = null;
         javoStreamWriter(false);
         System.gc();
         in = new ByteArrayInputStream(out.toByteArray());
         out.reset();
    }
    /**/  

};
