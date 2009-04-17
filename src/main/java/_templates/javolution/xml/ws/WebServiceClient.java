/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml.ws;

import java.io.IOException;

import _templates.java.lang.CharSequence;
import _templates.java.lang.UnsupportedOperationException;
import _templates.javolution.io.AppendableWriter;
import _templates.javolution.io.UTF8StreamWriter;
import _templates.javolution.text.Text;
import _templates.javolution.text.TextBuilder;
import _templates.javolution.xml.XMLObjectReader;
import _templates.javolution.xml.XMLObjectWriter;
import _templates.javolution.xml.stream.XMLStreamException;
import _templates.javolution.xml.stream.XMLStreamReader;
import _templates.javolution.xml.stream.XMLStreamWriter;

/**
 * <p> This class provides a simple web service client capable of leveraging  
 *     Javolution {@link _templates.javolution.xml XML marshalling/unmarshalling}.</p>
 *     
 * <p> Sub-classes may work from WSDL files, {@link _templates.javolution.xml.XMLFormat
 *     XMLFormat} or directly with the XML streams (StAX). For example:[code]
 *     private static class HelloWorld extends WebServiceClient  {
 *         protected void writeRequest(XMLObjectWriter out) throws XMLStreamException {
 *             XMLStreamWriter xml = out.getStreamWriter();
 *             xml.writeDefaultNamespace("http://www.openuri.org/");
 *             xml.writeEmptyElement("helloWorld"); // Operation name.
 *         }
 *         protected void readResponse(XMLObjectReader in) throws XMLStreamException {
 *             XMLStreamReader xml = in.getStreamReader();
 *             xml.require(START_ELEMENT, "http://www.openuri.org/", "string");
 *             xml.next(); // Move to character content.
 *             System.out.println(xml.getText());
 *         }
 *     }
 *     WebServiceClient ws = new HelloWorld().setAddress("http://acme.com:80/HelloWorld.jws");
 *     ws.invoke();
 *     
 *      > Hello World!
 *     [/code]</p>
 *     
 * <p><b>Note:</b> At this moment, this class is supported only on the J2SE 
 *                 platform. Soon, it will also be supported on mobile devices
 *                 through the CLDC/MIDP Generic Connection framework.</p>
 *           
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.2, September 16, 2007
 */
public abstract class WebServiceClient {

    /**
     * Holds standard SOAP envelope prefix.
     */
    public static final String ENVELOPE_PREFIX = "env";

    /**
     * Holds standard SOAP envelope namespace.
     */
    public static final String ENVELOPE_URI = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * Holds the URL (J2SE).
     */
    Object _url;

    /**
     * Default constructor (address not set).
     */
    public WebServiceClient() {
        /*@JVM-1.4+@
        if (true) return;
        /**/
        throw new UnsupportedOperationException("J2ME Not Supported Yet");
    }

    /**
     * Sets the address of this web service.
     * 
     * @param address the service full address. 
     */
    public WebServiceClient setAddress(String address) {
        /*@JVM-1.4+@    
        try {
            _url = new java.net.URL(address);
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL: " + address);
        }
        /**/
        return this;
    }

    /**
     * Invokes the web service.
     */
    public void invoke() throws IOException, XMLStreamException {
        try {
            // Formats the request message (we cannot write directly to 
            // the output stream because the http request requires the length.
            _out.setOutput(_buffer);
            _writer.setOutput(_out);
            final XMLStreamWriter xmlOut = _writer.getStreamWriter();
            xmlOut.setPrefix(csq(ENVELOPE_PREFIX), csq(ENVELOPE_URI));
            xmlOut.writeStartElement(csq(ENVELOPE_URI), csq("Envelope"));
            xmlOut.writeNamespace(csq(ENVELOPE_PREFIX), csq(ENVELOPE_URI));
            xmlOut.writeStartElement(csq(ENVELOPE_URI), csq("Header"));
            xmlOut.writeEndElement();
            xmlOut.writeStartElement(csq(ENVELOPE_URI), csq("Body"));
            writeRequest(_writer);
            _writer.close();

            // Sends the request.
            if (_url == null)
                throw new IOException("URL not set");
            /*@JVM-1.4+@    
            java.net.HttpURLConnection http = (java.net.HttpURLConnection) 
               ((java.net.URL)_url).openConnection();
            http.setRequestProperty("Content-Length", String.valueOf(_buffer
                    .length()));
            http.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            // httpConn.setRequestProperty("SOAPAction", "");
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setDoInput(true);
            _utf8Writer.setOutput(http.getOutputStream());
            /**/
            _buffer.print(_utf8Writer);
            _utf8Writer.close();

            // Reads the response.
            /*@JVM-1.4+@    
            _reader.setInput(http.getInputStream());
            /**/
            final XMLStreamReader xmlIn = _reader.getStreamReader();
            while (xmlIn.hasNext()) {
                if ((xmlIn.next() == XMLStreamReader.START_ELEMENT)
                        && xmlIn.getLocalName().equals("Body")
                        && xmlIn.getNamespaceURI().equals(ENVELOPE_URI)) {
                    // Found body, position reader to next element.
                    xmlIn.next();
                    readResponse(_reader);
                    break;
                }
            }

        } finally {
            _reader.close();
            _writer.reset();
            _out.reset();
            _buffer.reset();
            _utf8Writer.reset();
            _reader.reset();
        }
    }
    private final TextBuilder _buffer = new TextBuilder();
    private final AppendableWriter _out = new AppendableWriter();
    private final XMLObjectWriter _writer = new XMLObjectWriter();
    private final UTF8StreamWriter _utf8Writer = new UTF8StreamWriter();
    private  final XMLObjectReader _reader = new XMLObjectReader();
    /**/
    
    /**
     * Writes the web service request (SOAP body).
     * 
     * @param out the XML object writer.
     */
    protected abstract void writeRequest(XMLObjectWriter out)
            throws XMLStreamException;

    /**
     * Reads the web service response (SOAP body). The default implementation
     * writes the body XML events to <code>System.out</code>.
     * 
     * @param in the XML object reader.
     */
    protected void readResponse(XMLObjectReader in) throws XMLStreamException {
        final XMLStreamReader xml = in.getStreamReader();
        while (xml.hasNext()) {
            switch (xml.next()) {
            case XMLStreamReader.START_DOCUMENT:
                System.out.println("Start Document");
                break;
            case XMLStreamReader.END_DOCUMENT:
                System.out.println("End Document.");
                break;
            case XMLStreamReader.START_ELEMENT:
                System.out.println("Start Element: " + xml.getLocalName() + "("
                        + xml.getNamespaceURI() + ")");
                for (int i = 0, n = xml.getAttributeCount(); i < n; i++) {
                    System.out.println("   Attribute: "
                            + xml.getAttributeLocalName(i) + "("
                            + xml.getAttributeNamespace(i) + "), Value: "
                            + xml.getAttributeValue(i));
                }
                break;
            case XMLStreamReader.END_ELEMENT:
                if (xml.getLocalName().equals("Body")
                        && xml.getNamespaceURI().equals(ENVELOPE_URI))
                    return; // End body.
                System.out.println("End Element: " + xml.getLocalName() + "("
                        + xml.getNamespaceURI() + ")");
                break;
            case XMLStreamReader.CHARACTERS:
                System.out.println("Characters: " + xml.getText());
                break;
            case XMLStreamReader.CDATA:
                System.out.println("CDATA: " + xml.getText());
                break;
            case XMLStreamReader.COMMENT:
                System.out.println("Comment: " + xml.getText());
                break;
            case XMLStreamReader.SPACE:
                System.out.println("Space");
                break;
            default:
                System.out.println(xml);
            }
        }

    }  
    
    // For J2ME compatiblity.
    private static final CharSequence csq(Object string) {
        return (string instanceof CharSequence) ? (CharSequence) string:
            Text.valueOf(string);
        
    }
}
