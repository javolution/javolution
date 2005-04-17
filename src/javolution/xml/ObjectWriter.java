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
import javolution.lang.TextBuilder;
import javolution.realtime.ArrayFactory;
import javolution.realtime.ObjectFactory;
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
 * @version 3.2, March 18, 2005
 */
public class ObjectWriter implements Reusable {

    /**
     * Holds Javolution prefix ("j").
     */
    private static final Text JAVOLUTION_PREFIX = Text.valueOf("j").intern();

    /**
     * Holds Javolution uri.
     */
    private static final Text JAVOLUTION_URI = Text.valueOf(
            "http://javolution.org").intern();

    /**
     * Holds the Java scheme for package identification.
     */
    private static final Text JAVA_SCHEME = Text.valueOf("java:").intern();

    /**
     * Holds the factory for the internal chars array.
     */
    private static final ArrayFactory CHARS_FACTORY = new ArrayFactory(64) {
        protected Object create(int length) {
            return new char[length];
        }
    };

    /**
     * Holds prefix-package pairs (String).
     */
    private FastList _namespaces = new FastList();

    /**
     * Holds the package associated to default namespace.
     */
    private String _defaultPkg = "";

    /**
     * Holds the class info mapping (Class to ClassInfo).
     */
    private final FastMap _classInfo = new FastMap();

    /**
     * Holds the stack of XML elements (nesting limited to 32 levels).
     */
    private final XmlElement[] _stack = new XmlElement[32];

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
     * Holds the object to id mapping (persistent).
     */
    private final FastMap _objectToId = new FastMap()
            .setKeyComparator(FastComparator.IDENTITY);

    /**
     * The counter to use to generate id automatically.
     */
    private int _idCount;

    /**
     * Holds the character array for writing CharacterData occurences.
     */
    private char[] _chars = (char[]) CHARS_FACTORY.newObject();

    /**
     * Default constructor.
     */
    public ObjectWriter() {
        _stack[0] = new XmlElement();
    }

    /**
     * Maps a namespace to a Java package. The specified prefix is used to 
     * shorten the tag name of the object being serialized. For example:
     * <code>setNamespace("math", "org.jscience.mathematics")</code> associates
     * the namespace prefix <code>math</code> with the namespace name
     * <code>java:org.jscience.mathematics</code>. Classes within the package
     * <code>org.jscience.mathematics.*</code> now use the <code>math</code>
     * prefix. The default namespace (represented by the prefix <code>""</code>)
     * can be set as well.
     *
     * @param  prefix the namespace prefix or empty sequence to set 
     *         the default namespace.
     * @param  packageName of the package associated to the specified prefix.
     * @throws IllegalArgumentException if the prefix is "j" (reserved for 
     *         the "http://javolution.org" uri).
     */
    public void setNamespace(String prefix, String packageName) {
        if (prefix.equals("j"))
            throw new IllegalArgumentException("Prefix: \"j\" is reserved.");
        if (prefix.length() == 0) { // Default namespace.
            _defaultPkg = packageName;
        }
        _namespaces.add(prefix);
        _namespaces.add(packageName);
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
    public void write(Object obj, OutputStream out) throws IOException {
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
    public void write(Object obj, ByteBuffer byteBuffer) throws IOException {
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
    public void write(Object obj, ContentHandler handler) throws SAXException {
        handler.startDocument();
        try {
            handler.startPrefixMapping(JAVOLUTION_PREFIX, JAVOLUTION_URI);
            for (FastList.Node n = _namespaces.headNode(), end = _namespaces
                    .tailNode(); (n = n.getNextNode()) != end;) {
                Object prefix = n.getValue();
                String pkg = (String) (n = n.getNextNode()).getValue();
                Text uri = JAVA_SCHEME.concat(Text.valueOf(pkg));
                handler.startPrefixMapping(toCharSeq(prefix), uri);
            }
            writeElement(obj, handler, 0, null);
        } finally {
            handler.endPrefixMapping(JAVOLUTION_PREFIX);
            for (FastList.Node n = _namespaces.headNode(), end = _namespaces
                    .tailNode(); (n = n.getNextNode()) != end;) {
                Object prefix = n.getValue();
                n = n.getNextNode(); // Ignores package.
                handler.endPrefixMapping(toCharSeq(prefix));
            }
            handler.endDocument();
        }
    }

    private void writeElement(Object obj, ContentHandler handler, int level,
            CharSequence tagName) throws SAXException {

        // Replaces null value with Null object.
        if (obj == null) {
            writeElement(XmlFormat.NULL, handler, level, null);
            return;
        }

        // Checks for Character Data
        if (obj instanceof CharacterData) {
            CharacterData charData = (CharacterData) obj;
            while (_chars.length < charData.length()) {
                _chars = CHARS_FACTORY.resize(_chars);
            }
            charData.getChars(0, charData.length(), _chars, 0);
            handler.characters(_chars, 0, charData.length());
            return;
        }

        // Retrieves info for the class (info changes when namespace changes).
        Class clazz = obj.getClass();
        ClassInfo ci = (ClassInfo) _classInfo.get(clazz);
        if (ci == null) { // First occurence of this class ever.
            ci = (ClassInfo) ClassInfo.FACTORY.object();
            _classInfo.put(clazz, ci);
        }
        if (ci.className == null) { // Sets info from current namespace settings. 
            ci.className = XmlFormat.nameFor(clazz);
            ci.format = XmlFormat.getInstance(clazz);

            // Search for an package for the className (or alias).
            String prefix = null;
            String pkg = "";
            for (FastList.Node n = _namespaces.headNode(), end = _namespaces
                    .tailNode(); (n = n.getNextNode()) != end;) {
                String pkgStr = (String) (n = n.getNextNode()).getValue();
                if (ci.className.startsWith(pkgStr)
                        && (pkgStr.length() > pkg.length())) {
                    prefix = (String) n.getPreviousNode().getValue();
                    pkg = pkgStr;
                }
            }

            // URI is one of:
            // - "" (default namespace)
            // - "http://javolution.org" 
            // - "java:xxx.yyy.zzz" (xxx.yyy.zzz package name) 
            if (prefix == null) { // Not found.
                if (_defaultPkg.length() == 0) { // Use default namespace.
                    prefix = "";
                } else {
                    prefix = "j";
                    ci.uri.append(JAVOLUTION_URI);
                }
            } else {
                ci.uri.append(JAVA_SCHEME).append(pkg);
            }

            // Sets local name.
            if (pkg.length() == 0) {
                ci.localName.append(ci.className);
            } else { // Remove package prefix from class name.
                ci.localName.append(ci.className, pkg.length() + 1,
                        ci.className.length());
            }

            // Sets qualified name.
            if (prefix.length() == 0) { // Default namespace.
                ci.qName.append(ci.localName);
            } else {
                ci.qName.append(prefix).append(":").append(ci.localName);
            }
        }

        // Formats.
        XmlElement xml = _stack[level];
        if (xml == null) {
            xml = _stack[level] = (XmlElement) XmlElement.FACTORY.newObject();
            xml._parent = _stack[level - 1];
        }
        xml._objectClass = clazz;
        if (ci.format._idName != null) { // Identifier attribute must be present.
            CharSequence idValue = (CharSequence) _objectToId.get(obj);
            if (idValue != null) { // Already formatted, write the reference.
                xml.setAttribute(ci.format.identifier(true), idValue);
            } else { // First object occurence.
                ci.format.format(obj, xml);

                // Sets idValue if not set already
                idValue = xml.getAttribute(ci.format.identifier(false));
                if (idValue == null) { // Generates idValue.
                    idValue = newTextBuilder().append(++_idCount);
                    xml.setAttribute(ci.format.identifier(false), idValue);
                }    
                _objectToId.put(obj, idValue);
            }
        } else { // No object identifier.
            ci.format.format(obj, xml);
        }

        // Writes start tag.
        if (tagName != null) { // Custom name
            xml.setAttribute("j:class", ci.className);
            handler.startElement(Text.EMPTY, tagName, tagName, xml
                    .getAttributes());
        } else {
            handler.startElement(ci.uri, ci.localName, ci.qName, xml
                    .getAttributes());
        }

        // Writes named elements first.
        for (FastMap.Entry e = xml._nameToChild.headEntry(), end = xml._nameToChild
                .tailEntry(); (e = e.getNextEntry()) != end;) {
            writeElement(e.getValue(), handler, level + 1,
                    toCharSeq(e.getKey()));
        }
        //Writes anonymous content.
        for (FastList.Node n = xml._content.headNode(), end = xml._content
                .tailNode(); (n = n.getNextNode()) != end;) {
            writeElement(n.getValue(), handler, level + 1, null);
        }

        // Writes end tag.
        if (tagName != null) {
            handler.endElement(Text.EMPTY, tagName, tagName);
        } else {
            handler.endElement(ci.uri, ci.localName, ci.qName);
        }
        xml.reset();
    }

    /**
     * Resets all internal data maintained by this writer including any 
     * namespace associations; objects previously written will not be
     * referred to, they will be send again.
     */
    public void reset() {
        for (FastMap.Entry e = _classInfo.headEntry(), end = _classInfo
                .tailEntry(); (e = e.getNextEntry()) != end;) {
            ((ClassInfo) e.getValue()).reset(); // Clears class info.
        }
        for (int i = 0; (i < _stack.length) && (_stack[i] != null); i++) {
            _stack[i].reset(); // Ensures that all xml element are reset.
        }
        _textBuilderPool.addAll(_objectToId.values());
        _objectToId.clear();
        _namespaces.clear();
        _defaultPkg = "";
        _idCount = 0;
    }

    /**
     * Holds custom entries.
     */
    private static final class ClassInfo {
        private static ObjectFactory FACTORY = new ObjectFactory() {
            protected Object create() {
                return new ClassInfo();
            }
        };

        String className; // The class name, possibly an alias (when obfuscating).

        XmlFormat format;

        TextBuilder uri = new TextBuilder(); // e.g. java:org.jscience.mathematics.numbers

        TextBuilder qName = new TextBuilder(); // e.g. num:Complex

        TextBuilder localName = new TextBuilder(); // e.g. Complex

        void reset() {
            format = null;
            uri.reset();
            qName.reset();
            localName.reset();
        }
    }

    /**
     * Returns a persistent mutable character sequence from a local pool.
     * 
     * @return a new or recycled text builder instance.
     */
    private TextBuilder newTextBuilder() {
        if (_textBuilderPool.isEmpty())
            return (TextBuilder) TextBuilder.newInstance().moveHeap();
        TextBuilder tb = (TextBuilder) _textBuilderPool.removeLast();
        tb.reset();
        return tb;
    }

    private FastList _textBuilderPool = new FastList();

    
    /**
     * Converts a String to CharSequence (for J2ME compatibility)
     * 
     * @param str the String to convert.
     * @return the corresponding CharSequence instance.
     */
    private static CharSequence toCharSeq(Object str) {
        return (str instanceof CharSequence) ? (CharSequence) str : Text
                .valueOf((String) str);
    }

}