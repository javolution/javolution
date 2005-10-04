/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.lang.CharSequence;
import j2me.nio.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.xml.sax.SAXException;

import javolution.io.Utf8ByteBufferWriter;
import javolution.io.Utf8StreamWriter;
import javolution.lang.Reusable;
import javolution.lang.Text;
import javolution.util.FastTable;
import javolution.xml.sax.ContentHandler;
import javolution.xml.sax.WriterHandler;

/**
 * <p> This class takes an object and formats it to XML (SAX2 events or stream).
 *    <pre>
 *        ObjectWriter ow = new ObjectWriter();
 *        ...
 *        ow.write(obj, contentHandler); // SAX2 Events.
 *     <i>or</i> ow.write(obj, writer);         // Writer encoding.
 *     <i>or</i> ow.write(obj, outputStream);   // UTF-8 stream.
 *     <i>or</i> ow.write(obj, byteBuffer);     // UTF-8 NIO ByteBuffer.
 *     </pre></p>
 *     
 * <p> Namespaces are supported and may be associated to Java packages 
 *     in order to reduce the size of the xml generated (and to increase 
 *     readability). For example, the following code creates an 
 *     <code>ObjectWriter</code> using the default namespace for all 
 *     <code>org.jscience.physics.quantities.*</code> classes and the 
 *     <code>math</code> prefix for the 
 *     <code>org.jscience.mathematics.matrices.*</code> classes.
 *     <pre>
 *        ObjectWriter ow = new ObjectWriter();
 *        ow.setPackagePrefix("", "org.jscience.physics.quantities");
 *        ow.setPackagePrefix("math", "org.jscience.mathematics.matrices");
 *     </pre>
 *     Here is an example of the xml data produced by such a writer:
 *     <pre>
 *     &lt;math:Matrix xmlns:j="http://javolution.org" 
 *                     xmlns="java:org.jscience.physics.quantities"
 *                     xmlns:math="java:org.jscience.mathematics.matrices"
 *                     row="2" column="2">
 *        &lt;Mass value="2.3" unit="mg"/&gt;
 *        &lt;Pressure value="0.2" unit="Pa"/&gt;
 *        &lt;Force value="20.0" unit="µN"/&gt;
 *        &lt;Length value="3.0" unit="ft"/&gt;
 *     &lt;/math:Matrix&gt;</pre></p>
 *
 * <p> For more control over the xml document generated (e.g. indentation, 
 *     prolog, etc.), applications may use the 
 *     {@link #write(Object, ContentHandler)} method in conjonction with
 *     a custom {@link WriterHandler}. For example:<pre>
 *        OutputStream out = new FileOutputStream("C:/document.xml");
 *        Writer writer = new Utf8StreamWriter().setOuptutStream(out); // UTF-8 encoding.
 *        WriterHandler handler = new WriterHandler().setWriter(writer);
 *        handler.setIndent("\t"); // Indents with tabs.
 *        handler.setProlog("&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;");
 *        ...
 *        ow.write(obj, handler);</pre></p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.5, August 29, 2005
 */
public class ObjectWriter/*<T>*/implements Reusable {

    /**
     * Holds Javolution prefix ("j").
     */
    static final Text JAVOLUTION_PREFIX = Text.valueOf("j").intern();

    /**
     * Holds Javolution uri.
     */
    static final Text JAVOLUTION_URI = Text.valueOf(
            "http://javolution.org").intern();

    /**
     * Holds the stream writer.
     */
    private final Utf8StreamWriter _utf8StreamWriter = new Utf8StreamWriter();

    /**
     * Holds the byte buffer writer.
     */
    private final Utf8ByteBufferWriter _utf8ByteBufferWriter = new Utf8ByteBufferWriter();

    /**
     * Holds the writer handler for stream output.
     */
    private final WriterHandler _writerHandler = new WriterHandler();

    /**
     * Holds the writer namespaces (CharSequence prefix/uri pairs).
     */
    private FastTable _namespaces = new FastTable();

    /**
     * Hold the xml element used when formatting.
     */
    private final XmlElement _xml = new XmlElement(null); // Formatting.

    /**
     * Default constructor.
     */
    public ObjectWriter() {
    }

    /**
     * Sets the document namespaces for this writer.
     *
     * @param  prefix the namespace prefix or an empty sequence to set 
     *         the default namespace.
     * @param  uri the namespace uri.
     * @throws IllegalArgumentException if the prefix is "j" (reserved for 
     *         the "http://javolution.org" uri).
     */
    public void setNamespace(String prefix, String uri) {
        if ((prefix.length() == 1) && (prefix.charAt(0) == 'j'))
            throw new IllegalArgumentException("Prefix: \"j\" is reserved.");
        _namespaces.addLast(toCharSeq(prefix));
        _namespaces.addLast(toCharSeq(uri));
        if (prefix.length() == 0) { // Default namespace mapped
            // Use javolution uri for all classes without namespace
            // (default namespace cannot be used anymore).
            _xml._packagePrefixes.addLast("j");
            _xml._packagePrefixes.addLast("");
        }
    }

    /**
     * Maps a namespace to a Java package. The specified prefix is used to 
     * shorten the class name of the object being serialized.
     *
     * @param  prefix the namespace prefix or empty sequence to set 
     *         the default namespace.
     * @param  packageName of the package associated to the specified prefix.
     * @throws IllegalArgumentException if the prefix is "j" (reserved for 
     *         the "http://javolution.org" uri).
     */
    public void setPackagePrefix(String prefix, String packageName) {
        setNamespace(prefix, "java:" + packageName);
        _xml._packagePrefixes.addLast(prefix);
        _xml._packagePrefixes.addLast(packageName);
    }

    /**
     * Writes the specified object to the given writer in XML format.
     * The writer is closed after serialization. To serialize multiple 
     * objects over a persistent connection {@link XmlOutputStream}
     * should be used instead.
     *
     * @param  obj the object to format.
     * @param  writer the writer to write to.
     * @throws IOException if there's any problem writing.
     */
    public void write(Object/*T*/obj, Writer writer) throws IOException {
        try {
            _writerHandler.setWriter(writer);
            write(obj, _writerHandler);
        } catch (SAXException e) {
            if (e.getException() instanceof IOException) {
                throw (IOException) e.getException();
            }
        } finally {
            _writerHandler.reset();
        }
    }

    /**
     * Writes the specified object to the given output stream in XML format. 
     * The characters are written using UTF-8 encoding. 
     * The output streamwriter is closed after serialization. To serialize 
     * multiple objects over a persistent connection {@link XmlOutputStream}
     * should be used instead.
     *
     * @param  obj the object to format.
     * @param  out the output stream to write to.
     * @throws IOException if there's any problem writing.
     */
    public void write(Object/*T*/obj, OutputStream out) throws IOException {
        try {
            _utf8StreamWriter.setOutputStream(out);
            _writerHandler.setWriter(_utf8StreamWriter);
            write(obj, _writerHandler);
        } catch (SAXException e) {
            if (e.getException() instanceof IOException) {
                throw (IOException) e.getException();
            }
        } finally {
            _utf8StreamWriter.reset();
            _writerHandler.reset();
        }
    }

    /**
     * Writes the specified object to the given byte buffer in XML format. 
     * The characters are written using UTF-8 encoding.
     *
     * @param  obj the object to format.
     * @param  byteBuffer the byte buffer to write to.
     * @throws IOException if there's any problem writing.
     */
    public void write(Object/*T*/obj, ByteBuffer byteBuffer)
            throws IOException {
        try {
            _utf8ByteBufferWriter.setByteBuffer(byteBuffer);
            _writerHandler.setWriter(_utf8ByteBufferWriter);
            write(obj, _writerHandler);
        } catch (SAXException e) {
            if (e.getException() instanceof IOException) {
                throw (IOException) e.getException();
            }
        } finally {
            _utf8ByteBufferWriter.reset();
            _writerHandler.reset();
        }
    }

    /**
     * Generates the SAX events corresponding to the serialization of the 
     * specified object.
     *
     * @param  obj the object to format.
     * @param  handler the SAX event handler.
     */
    public void write(Object/*T*/obj, ContentHandler handler)
            throws SAXException {
        handler.startDocument();
        try {
            handler.startPrefixMapping(JAVOLUTION_PREFIX, JAVOLUTION_URI);
            for (int i=0; i < _namespaces.size();) {
                CharSequence prefix = (CharSequence) _namespaces.get(i++);
                CharSequence uri = (CharSequence) _namespaces.get(i++);
                handler.startPrefixMapping(prefix, uri);
            }
            
            _xml._formatHandler = handler;
            _xml.add(obj);
            
        } finally {
            handler.endPrefixMapping(JAVOLUTION_PREFIX);
            for (int i=0; i < _namespaces.size();) {
                CharSequence prefix = (CharSequence) _namespaces.get(i++);
                i++;
                handler.endPrefixMapping(prefix);
            }
            handler.endDocument();
            _xml.reset();
        }
    }

    /**
     * Resets all internal data maintained by this writer including any 
     * namespace associations; objects previously written will not be
     * referred to, they will be send again.
     */
    public void reset() {
        _xml.reset();
        _namespaces.clear();
        _xml._packagePrefixes.clear();
    }

    /**
     * Converts a String to a CharSequence (for J2ME compatibility)
     * 
     * @param str the String to convert.
     * @return the corresponding CharSequence instance.
     */
    private CharSequence toCharSeq(Object str) {
        if (str instanceof CharSequence)
            return (CharSequence) str;
        return Text.valueOf((String) str);
    }
}