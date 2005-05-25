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
import javolution.lang.PersistentReference;
import javolution.lang.Reusable;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.realtime.ObjectFactory;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
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
 * @version 3.3, May 13, 2005
 */
public class ObjectWriter/*<T>*/ implements Reusable {

    /**
     * Holds the configurable nominal length for the CDATA buffer.
     */
    private static final PersistentReference CDATA_SIZE = new PersistentReference(
            "javolution.xml.ObjectWriter#CDATA_SIZE", new Integer(256));

    /**
     * Holds Javolution prefix ("j").
     */
    private static final Text J = Text.valueOf("j").intern();

    /**
     * Holds Javolution prefix declaration ("xmlns:j").
     */
    private static final Text XMLNS_J = Text.valueOf("xmlns:j").intern();

    /**
     * The "xmlns" prefix is reserved, and is used to declare other prefixes.
     */
    private static final Text XMLNS = Text.valueOf("xmlns").intern();

    /**
     * Holds the namespace that the "xmlns" prefix refers to.
     */
    private static final Text XMLNS_URI = Text.valueOf(
            "http://www.w3.org/2000/xmlns/").intern();

    /**
     * Holds Javolution uri.
     */
    public static final Text JAVOLUTION_URI = Text.valueOf(
            "http://javolution.org").intern();

    /**
     * Holds the Java scheme for package identification.
     */
    private static final Text JAVA_ = Text.valueOf("java:").intern();

    /**
     * Holds the class identifier local name.
     */
    private static final Text CLASS = Text.valueOf("class").intern();

    /**
     * Holds the class identifier prefix.
     */
    private static final Text J_CLASS = Text.valueOf("j:class").intern();

    /**
     * Defines bit flag that causes the "j:class" attribute to always be included
     * every element; ordinarily, this attribute would not be output as a SAX
     * event if an alias was defined.
     */
    public static int OUTPUT_CLASS_NAME = 0x01;

    /**
     * Defines bit flag that causes "xmlns:<prefix>" attributes to be output
     * with every element; sometimes this is required for SAX sources that
     * are used for transformations.
     */
    public static int OUTPUT_XMLNS_ATTRIBUTES = 0x02;

    /**
     * Defines bit flag the prevents ID and IDREF attributes from being output,
     * even if they are defined by an <code>XmlFormat</code> instance.
     */
    public static int OUTPUT_IDENTIFIERS = 0x04;

    /**
     * Defines bit flag that causes the IDREF attribute to be output only if
     * infinite recursion would occur. This feature is very useful if you would like
     * to process the serialization object using XSLT, and would like all IDREF
     * objects to be expanded in the SAX stream.  This flag has no effect if 
     * OUTPUT_IDENTIFIERS is not set.
     */
    public static int AVOID_OUTPUT_IDREF = 0x08;

    /**
     * Holds bit flag that defines default output features
     */
    public static int DEFAULT_FEATURES = OUTPUT_IDENTIFIERS;

    /**
     * Holds prefix-package pairs (String).
     */
    private FastList _namespaces = new FastList();

    /**
     * Holds the current features.
     */
    private int _features = DEFAULT_FEATURES;

    /**
     * Holds the root name.
     */
    private CharSequence _rootName;

    /**
     * Holds the package associated to default namespace.
     */
    private String _defaultPkg = "";

    /**
     * Holds the class info mapping (Class to ClassInfo).
     */
    private final FastMap _classInfo = new FastMap();

    /**
     * Holds the stack of XML elements.
     */
    private final FastTable _stack = new FastTable();

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
     * Holds the cdata buffer for characters notification.
     */
    private char[] _cdata = (char[]) new char[((Integer) CDATA_SIZE.get())
            .intValue()];

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
     * Default constructor.
     */
    public ObjectWriter() {
        _stack.add(new XmlElement());
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
     * Sets the features bit flags that controls output.
     *
     * @param features a combination of bit flags.
     */
    public void setFeature(int features) {
        _features = features;
    }

    /**
     * Sets the element name for the root element (default <code>null</code>
     * the element name is the class name).
     *
     * @param rootName the name of the root element.
     */
    public void setRootName(CharSequence rootName) {
        _rootName = rootName;
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
            handler.startPrefixMapping(J, JAVOLUTION_URI);
            for (FastList.Node n = _namespaces.headNode(), end = _namespaces
                    .tailNode(); (n = n.getNextNode()) != end;) {
                Object prefix = n.getValue();
                String pkg = (String) (n = n.getNextNode()).getValue();
                Text uri = JAVA_.concat(Text.valueOf(pkg));
                handler.startPrefixMapping(toCharSeq(prefix), uri);
            }
            writeElement(obj, handler, 0, _rootName);
        } finally {
            handler.endPrefixMapping(J);
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
            final int length = charData.length();
            if (length > _cdata.length) { // Resizes.
                char[] tmp = new char[_cdata.length * 2];
                System.arraycopy(_cdata, 0, tmp, 0, _cdata.length);
                _cdata = tmp;
                CDATA_SIZE.set(new Integer(_cdata.length));
            }
            charData.getChars(0, length, _cdata, 0);
            handler.characters(_cdata, 0, length);
            return;
        }

        // Retrieves info for the class (info changes when namespace changes).
        Class clazz = obj.getClass();
        ClassInfo ci = (ClassInfo) _classInfo.get(clazz);
        if (ci == null) { // First occurence of this class ever.
            ci = (ClassInfo) ClassInfo.FACTORY.newObject();
            _classInfo.put(clazz, ci);
        }
        if (ci.format == null) {
            ci.className = clazz.getName();
            ci.alias = XmlFormat.nameFor(clazz);
            ci.format = XmlFormat.getInstance(clazz);

            // Search for a package for the className (or alias).
            String prefix = null;
            String pkg = "";
            for (FastList.Node n = _namespaces.headNode(), end = _namespaces
                    .tailNode(); (n = n.getNextNode()) != end;) {
                String pkgStr = (String) (n = n.getNextNode()).getValue();
                if (ci.alias.startsWith(pkgStr)
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
                ci.uri.append(JAVA_).append(pkg);
            }

            // Sets local name.
            if (pkg.length() == 0) {
                ci.localName.append(ci.alias);
            } else { // Remove package prefix from class name.
                ci.localName.append(ci.alias, pkg.length() + 1, ci.alias
                        .length());
            }

            // Sets qualified name.
            if (prefix.length() == 0) { // Default namespace.
                ci.qName.append(ci.localName);
            } else {
                ci.qName.append(prefix).append(":").append(ci.localName);
            }
        }

        // Formats.
        if (level >= _stack.size()) {
            XmlElement tmp = (XmlElement) XmlElement.FACTORY.newObject();
            tmp._parent = (XmlElement) _stack.get(level - 1);
            _stack.add(tmp);
        }
        XmlElement xml = (XmlElement) _stack.get(level);
        xml._object = obj;
        xml._objectClass = clazz;
        if ((_features & OUTPUT_IDENTIFIERS) != 0 && ci.format._idName != null) { // Identifier attribute must be present.
            CharSequence idValue = (CharSequence) _objectToId.get(obj);
            if (idValue != null) { // Already formatted
                if (((_features & AVOID_OUTPUT_IDREF) == 0) || xml.isRecursion()) { // Write the reference.
                    xml.setAttribute(ci.format._idRef.toString(), idValue);
                } else { // Reoutput
                    ci.format.format(obj, xml);
                }
            } else { // First object occurence.
                ci.format.format(obj, xml);

                // Sets idValue if not set already
                idValue = xml.getAttribute(ci.format._idName.toString());
                if (idValue == null) { // Generates idValue.
                    idValue = newTextBuilder().append(++_idCount);
                    xml.setAttribute(ci.format._idName.toString(), idValue);
                }
                _objectToId.put(obj, idValue);
            }
        } else { // No object identifier.
            // The following test is commented out (to expensive).
            // if (visited(obj))
            //    throw new SAXException("Circular reference to object " + obj);
            ci.format.format(obj, xml);
        }

        boolean jPrefix = false;
        if ((_features & OUTPUT_CLASS_NAME) != 0) { // Always output class name
            xml._attributes.addAttribute(JAVOLUTION_URI, CLASS, J, J_CLASS,
                    "CDATA", toCharSeq(ci.className));
            jPrefix = true;
        } else if (tagName != null) { // Output alias or class name
            xml._attributes.addAttribute(JAVOLUTION_URI, CLASS, J, J_CLASS,
                    "CDATA", toCharSeq(ci.alias));
            jPrefix = true;
        }

        if ((_features & OUTPUT_XMLNS_ATTRIBUTES) != 0) {
            // TODO: should check if any of the attributes already added (such as j:id or 
            // j:idref) has been used.
            if (jPrefix) {
                xml._attributes.addAttribute(XMLNS_URI, J, XMLNS, XMLNS_J,
                        "CDATA", JAVOLUTION_URI);
            }
            // TODO: should build list of arbitrary prefixes used by user, and
            // output XMLNS attributes if requested
        }

        // Writes start tag.
        if (tagName != null) { // Custom name
            handler.startElement(Text.EMPTY, tagName, tagName, xml._attributes);
        } else {
            handler.startElement(ci.uri, ci.localName, ci.qName,
                    xml._attributes);
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
        _textBuilderPool.addAll(_objectToId.values());
        _objectToId.clear();
        _namespaces.clear();
        _defaultPkg = "";
        _idCount = 0;
        _features = DEFAULT_FEATURES;
        _rootName = null;
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

        String className; // The class name.

        String alias; // The class name, possibly an alias (when obfuscating).

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