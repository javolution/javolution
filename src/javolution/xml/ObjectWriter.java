/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import j2me.nio.ByteBuffer;
import j2me.util.Iterator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.xml.sax.SAXException;

import javolution.io.Utf8ByteBufferWriter;
import javolution.io.Utf8StreamWriter;
import javolution.lang.Reusable;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.xml.sax.ContentHandler;
import javolution.xml.sax.WriterHandler;

/**
 * <p> This class takes an object and formats it to XML (SAX2 events or stream). 
 *     Objects written using this facility may be read using
 *     the {@link ObjectReader} class.</p>
 *     
 * <p> Namespaces are supported (including default namespace).</p>
 * 
 * <p> For example, the following code creates an <code>ObjectWriter</code>
 *     using the default namespace for all <code>java.lang.*</code> classes and 
 *     the <code>math</code> prefix for the <code>org.jscience.mathematics.*
 *     </code> classes.
 *     <pre>
 *        ObjectWriter ow = new ObjectWriter();
 *        ow.setNamespace("", "java.lang"); // Default namespace.
 *        ow.setNamespace("math", "org.jscience.mathematics");
 *        ...
 *        ow.write(obj, contentHandler); // SAX2 Events.
 *     <i>or</i> ow.write(obj, writer);         // Writer encoding.
 *     <i>or</i> ow.write(obj, outputStream);   // UTF-8 stream.
 *     <i>or</i> ow.write(obj, byteBuffer);     // UTF-8 NIO ByteBuffer.
 *     </pre></p>
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
 *        ow.write(obj, handler);</pre>
 *     </p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.2, January 8, 2005
 */
public class ObjectWriter implements Reusable {

    /**
     * The counter to use to generate id automatically.
     */
    private int _idCount;

    /**
     * Holds the object to id (Text) mapping.
     */
    private final FastMap _objectToId
        = new FastMap().setKeyComparator(FastComparator.IDENTITY);

    /**
     * Holds namespaces association (prefix followed by package).
     */
    private FastList _namespaces = new FastList();
    
    /**
     * Holds the class info mapping (Class to ClassInfo).
     */
    private final FastMap _classInfo = new FastMap();
    
    /**
     * Holds the stack of XML elements (nesting limited to 32).
     */
    private final XmlElement[] _stack = new XmlElement[32];

    /**
     * Holds the stream writer.
     */
    private final Utf8StreamWriter _utf8StreamWriter = new Utf8StreamWriter();

    /**
     * Holds the byte buffer writer.
     */
    private final Utf8ByteBufferWriter _utf8ByteBufferWriter 
        = new Utf8ByteBufferWriter();

    /**
     * Holds the writer handler for stream output.
     */
    private final WriterHandler _writerHandler = new WriterHandler();

    /**
     * Default constructor.
     */
    public ObjectWriter() {
        _stack[0] = new XmlElement();
        for (int i = 1; i < _stack.length; i++) {
            _stack[i] = new XmlElement();
            _stack[i]._parent = _stack[i - 1];
        }
    }

    /**
     * Maps a namespace to a Java package. The specified prefix is used to 
     * shorten the tag name of the object being serialized. For example:
     * <code>setNamespace("math", "org.jscience.mathematics")</code> associates
     * the namespace prefix <code>math</code> with the namespace name
     * <code>java:org.jscience.mathematics</code>. Classes within the package
     * <code>org.jscience.mathematics.*</code> now use the <code>math</code>
     * prefix. The default namespace (represented by the prefix <code>""</code>)
     * can be set as well (in which cases a "root" prefix is created for 
     * classes without namespace).
     *
     * @param  prefix the namespace prefix or <code>""</code> to set the default
     *         namespace.
     * @param  packageName of the package associated to the specified prefix.
     */
    public void setNamespace(String prefix, String packageName) {
        if (prefix.length() == 0) { // Default namespace being set.
            // Creates a prefix for root.
            setNamespace("root", "");
        }
        _namespaces.add(prefix);
        _namespaces.add(packageName);
    }
    
    /**
     * Resets all internal data maintained by this writer including any 
     * namespace associations; objects previously written will not be
     * referred to, they will be send again.
     */
    public void reset() {
        _objectToId.clear();
        _namespaces.clear();
        _idCount = 0;
        
        // Clears class infos.
        for (Iterator i=_classInfo.fastValueIterator(); i.hasNext();) {
            ((ClassInfo) i.next()).reset();
        }
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
    public void write(Object obj, Writer writer) throws IOException {
        try {
            _writerHandler.setWriter(writer);
            write(obj, _writerHandler);
        } catch (SAXException e) {
            if (e.getException() instanceof IOException) {
                 throw (IOException)e.getException();   
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
    public void write(Object obj, OutputStream out) throws IOException {
        try {
            _utf8StreamWriter.setOutputStream(out);
            _writerHandler.setWriter(_utf8StreamWriter);
            write(obj, _writerHandler);
        } catch (SAXException e) {
            if (e.getException() instanceof IOException) {
                 throw (IOException)e.getException();   
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
    public void write(Object obj, ByteBuffer byteBuffer) throws IOException {
        try {
            _utf8ByteBufferWriter.setByteBuffer(byteBuffer);
            _writerHandler.setWriter(_utf8ByteBufferWriter);
            write(obj, _writerHandler);
        } catch (SAXException e) {
            if (e.getException() instanceof IOException) {
                 throw (IOException)e.getException();   
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
    public void write(Object obj, ContentHandler handler) throws SAXException {
        handler.startDocument();
        try {
            for (Iterator i=_namespaces.fastIterator(); i.hasNext();) {
                TextBuilder prefix 
                    = TextBuilder.newInstance().append(i.next());
                TextBuilder uri 
                    = TextBuilder.newInstance().append("java:").append(i.next());
                handler.startPrefixMapping(prefix, uri);
            }
            writeElement(obj, handler, 0);
        } finally {
            for (Iterator i=_namespaces.fastIterator(); i.hasNext();) {
                TextBuilder prefix 
                   = TextBuilder.newInstance().append(i.next());
                Object pkg = i.next();
                handler.endPrefixMapping(prefix);
            }
            handler.endDocument();
        }
    }
    
    
    private void writeElement(Object obj, ContentHandler handler, int level) throws SAXException {
        
        // Replaces null value with Null object.
        if (obj == null) {
            writeElement(XmlFormat.NULL, handler, level);
            return;
        }
        
        // Checks for Character Data
        if (obj instanceof CharacterData) {
            CharacterData cd = (CharacterData) obj;
            handler.characters(cd.toArray(), 0, cd.length());
            return;
        }
        
        // Retrieves info for the class.
        Class cl = obj.getClass();
        ClassInfo ci = (ClassInfo) _classInfo.get(cl);
        if (ci == null) { // First occurence of this class ever.
            ci = new ClassInfo();
            _classInfo.put(cl, ci);
        }
        if (ci.format == null) { // Sets info from current namespace settings. 
            ci.format = XmlFormat.getInstance(cl);
            ci.formatId = ci.format.identifier(false);
            String name = XmlFormat.nameFor(cl);
            
            // Search for an uri matching the package name.
            int maxPkgLength = -1;
            for (Iterator i= _namespaces.fastIterator(); i.hasNext();) {
                String prefix = (String) i.next();
                String pkg = (String) i.next();
                final int pkgLength = pkg.length();
                if (name.startsWith(pkg) && (pkgLength > maxPkgLength)) {
                     ci.uri = JAVA.plus(Text.valueOf(pkg));
                     ci.localName = (pkgLength > 0) ? 
                             Text.valueOf(name).subtext(pkgLength + 1) :
                             Text.valueOf(name);    
                     ci.qName = (prefix.length() > 0) ? 
                             Text.valueOf(prefix).plus(SEMICOLON).plus(ci.localName) :
                             ci.localName;
                     maxPkgLength = pkgLength;
                }
            }
            if (maxPkgLength < 0) { // Default namespace "".
                ci.uri = Text.EMPTY;
                ci.localName = Text.valueOf(name);
                ci.qName = ci.localName;
            }
        }
        
        // Formats.
        final XmlElement xml = _stack[level];
        xml._objectClass = cl;
        if (ci.formatId != null) { // Identifier attribute must be present.
            Text idValue = (Text) _objectToId.get(obj);
            if (idValue != null) { // Already referenced.
                xml.setAttribute(ci.format.identifier(true), idValue);
            } else { // First object occurence.
                ci.format.format(obj, xml);
                Object userId = xml.getAttribute(ci.formatId);
                idValue = (userId == null) ? 
                        TextBuilder.newInstance().append(++_idCount).toText() :
                        TextBuilder.newInstance().append(userId).toText();
                _objectToId.put(obj, idValue);
                xml.setAttribute(ci.formatId, idValue);
            }
        } else { // No object identifier.
            ci.format.format(obj, xml);
        }
        handler.startElement(ci.uri, ci.localName, ci.qName, xml.getAttributes());

        // Writes nested elements.
        for (Iterator i = xml._content.fastIterator(); i.hasNext();) {
             Object child = i.next();
             writeElement(child, handler, level + 1);
        }

        handler.endElement(ci.uri, ci.localName, ci.qName);

        // Clears XmlElement for reuse.
        xml.reset();
    }
    private static final Text JAVA = Text.valueOf("java:").intern();
    private static final Text SEMICOLON = Text.valueOf(":").intern();

    /*
     * This class represents the current representation of a particular class.
     */
    private static final class ClassInfo {
         XmlFormat format;
         String formatId;
         Text uri; // e.g. java:org.jscience.mathematics.numbers
         Text qName; // e.g. num:Complex
         Text localName; // e.g. Complex
         void reset() {
             format = null;
             formatId = null;
             uri = null;
             qName = null;
             localName = null;
         }
    }
}