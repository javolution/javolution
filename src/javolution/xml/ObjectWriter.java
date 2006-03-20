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
 *    [code]
 *        ObjectWriter ow = new ObjectWriter();
 *        ...
 *        ow.write(obj, contentHandler); // SAX2 Events.
 *     <i>or</i> ow.write(obj, writer);         // Writer encoding.
 *     <i>or</i> ow.write(obj, outputStream);   // UTF-8 stream.
 *     <i>or</i> ow.write(obj, byteBuffer);     // UTF-8 NIO ByteBuffer.
 *     [/code]</p>
 *     
 * <p> Namespaces are supported and may be associated to Java packages 
 *     in order to reduce the size of the xml generated (and to increase 
 *     readability). For example, the following code creates an 
 *     <code>ObjectWriter</code> using the default namespace for all 
 *     <code>org.jscience.physics.quantities.*</code> classes and the 
 *     <code>math</code> prefix for the 
 *     <code>org.jscience.mathematics.matrices.*</code> classes.
 *     [code]
 *        ObjectWriter ow = new ObjectWriter();
 *        ow.setPackagePrefix("", "org.jscience.physics.quantities");
 *        ow.setPackagePrefix("math", "org.jscience.mathematics.matrices");
 *     [/code]
 *     Here is an example of the xml data produced by such a writer:[code]
 *     <math:Matrix xmlns:j="http://javolution.org" 
 *                  xmlns="java:org.jscience.physics.quantities"
 *                  xmlns:math="java:org.jscience.mathematics.matrices"
 *                  row="2" column="2">
 *        <Mass value="2.3" unit="mg"/>
 *        <Pressure value="0.2" unit="Pa"/>
 *        <Force value="20.0" unit="µN"/>
 *        <Length value="3.0" unit="ft"/>
 *     </math:Matrix>[/code]</pre></p>
 *
 * <p> For more control over the xml document generated (e.g. indentation, 
 *     prolog, etc.), applications may use the 
 *     {@link #write(Object, ContentHandler)} method in conjonction with
 *     a custom {@link WriterHandler}. For example:[code]
 *        OutputStream out = new FileOutputStream("C:/document.xml");
 *        Writer writer = new Utf8StreamWriter().setOuptutStream(out); // UTF-8 encoding.
 *        WriterHandler handler = new WriterHandler().setWriter(writer);
 *        handler.setIndent("\t"); // Indents with tabs.
 *        handler.setProlog("<?xml version=\"1.0\" encoding=\"UTF-8\"/>");
 *        ...
 *        ow.write(obj, handler);[/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, October 13, 2005
 */
public class ObjectWriter /*<T>*/ implements Reusable {

    /**
     * Holds Javolution prefix ("j").
     */
    static final Text JAVOLUTION_PREFIX = Text.intern("j");

    /**
     * Holds Javolution uri.
     */
    static final Text JAVOLUTION_URI = Text.intern("http://javolution.org");

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
     * Holds the element name of the root object.
     */
    private String _rootName;

    /**
     * Indicates if cross references are written out.
     */
    private boolean _areReferencesEnabled = false;

    /**
     * Indicates if references are expanded.
     */
    private boolean _expandReferences = false;

    /**
     * Indicates if class identifiers are written out.
     */
    private boolean _isClassIdentifierEnabled = true;

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
    public void write(Object/*T*/ obj, Writer writer) throws IOException {
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
    public void write(Object/*T*/ obj, OutputStream out) throws IOException {
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
    public void write(Object/*T*/ obj, ByteBuffer byteBuffer)
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
    public void write(Object/*T*/ obj, ContentHandler handler)
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
            _xml._areReferencesEnabled = _areReferencesEnabled;
            _xml._expandReferences = _expandReferences;
            _xml._isClassIdentifierEnabled = _isClassIdentifierEnabled;
            if( _rootName != null ) {
                _xml.add(obj,_rootName);
            } else { 
                _xml.add(obj);
            }
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
        _areReferencesEnabled = false;
        _expandReferences = false;
        _isClassIdentifierEnabled = true;
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

    /**
     * Enables/disables xml elements cross references (default 
     * <code>false</code>).
     * When enabled, identifiers attributes are added during serialization; 
     * the name of these attributes is defined by {@link XmlFormat#identifier}.
     * 
     * @param enabled <code>true</code> if an unique identifier attribute is
     *        added to objects being serialized; <code>false</code> otherwise.
     */
    public void setReferencesEnabled(boolean enabled) {
        _areReferencesEnabled = enabled;
    }

    /**
     * Controls whether or not references are expanced (default 
     * <code>false</code>). References are not expanded if currently 
     * being expanded (to avoid infinite recursion).
     * 
     * @param value <code>true</code> to expand references;
     *        <code>false</code> otherwise.
     * @see   XmlFormat#identifier 
     */
    public void setExpandReferences(boolean value) {
        _expandReferences  = value;
    }

    /**
     * Enables/disables class identifier attributes (default <code>true<code>).
     * Disabling the class identifier should only be done if the serialized
     * objects does not need to be deserialized (e.g. pure xml formatting).
     * 
     * @param enabled <code>true</code> to allow for an additional "j:class"
     *        attribute; <code>false</code> otherwise. 
     * @see   XmlElement#add(Object)
     * @see   XmlElement#add(Object, String)
     */
    public void setClassIdentifierEnabled(boolean enabled) {
        _isClassIdentifierEnabled = enabled;
    }

    /**
     * Sets the element name or the root object.
     * 
     * @param name the name of the root element.
     */
    public void setRootName(String name) {
        _rootName = name;
    }
}