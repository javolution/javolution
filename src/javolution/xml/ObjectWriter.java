/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

import javolution.io.Utf8ByteBufferWriter;
import javolution.io.Utf8StreamWriter;
import javolution.lang.Text;
import javolution.realtime.RealtimeObject;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.Reflection;

/**
 * <p> This class takes an object and formats it to a stream as XML. 
 *     Objects written using this facility may be read using
 *     the {@link ObjectReader} class.</p>
 * <p> Namespaces are supported (including default namespace).</p>
 * <p> For example, the following code creates an <code>ObjectWriter</code>
 *     using a default namespace for all classes within the package
 *     <code>org.jscience</code>, excepts for the <code>org.jscience.math</code>
 *     classes which uses the <code>math</code> prefix.
 *    <pre>
 *        ObjectWriter ow = ObjectWriter.newInstance();
 *        ow.setNamespace("", "org.jscience"); // Default namespace.
 *        ow.setNamespace("math", "org.jscience.math");
 *        ...
 *        ow.write(matrix, writer);       // Writer encoding.
 *        ow.write(matrix, outputStream); // UTF-8 stream encoding.
 *        ow.write(matrix, byteBuffer);   // UTF-8 NIO ByteBuffer encoding.
 * 
 *    </pre></p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.1, November 7, 2004
 */
public class ObjectWriter extends RealtimeObject {

    /**
     * Holds the list of prefixes.
     */
    private final FastList _prefixes = new FastList();

    /**
     * Holds the list of packages.
     */
    private final FastList _packages = new FastList();

    /**
     * Holds the document indent (default two-space indent).
     */
    private String _indent = "  ";

    /**
     * Indicates if the prolog has to be written (default <code>false</code>).
     */
    private boolean _isProlog = true;

    /**
     * The counter to use to generate id automatically.
     */
    private int _idCount;

    /**
     * Holds the object to id (CharSequence) mapping.
     */
    private final FastMap _objectToId
        = new FastMap().setKeyComparator(FastMap.KeyComparator.REFERENCE);

    /**
     * Holds the stack of XML elements (nesting limited to 64).
     */
    private final XmlElement[] _stack = new XmlElement[64];

    /**
     * Holds the stream writer.
     */
    private final Utf8StreamWriter _utf8StreamWriter = new Utf8StreamWriter(
            2048);

    /**
     * Holds the byte buffer writer.
     */
    private final Utf8ByteBufferWriter _utf8ByteBufferWriter
         = new Utf8ByteBufferWriter();
    
    /**
     * Returns a new object writer (potentially allocated on the stack).
     * 
     * @return a new or recycled object writer.
     */
    public static ObjectWriter newInstance() {
        return (ObjectWriter) FACTORY.object();
    }

    private static final Factory FACTORY = new Factory() {
        public Object create() {
            return new ObjectWriter();
        }

        public void cleanup(Object obj) {
            ObjectWriter ow = (ObjectWriter) obj;
            ow._prefixes.clear();
            ow._packages.clear();
            ow._objectToId.clear();
            // Default namespace association.
            ow._prefixes.add("");
            ow._packages.add("");
            ow._idCount = 0;
        }
    };

    /**
     * Default constructor.
     */
    ObjectWriter() {
        _stack[0] = new XmlElement();
        for (int i = 1; i < _stack.length; i++) {
            _stack[i] = new XmlElement();
            _stack[i]._parent = _stack[i - 1];
        }
        // Default namespace association.
        _prefixes.add("");
        _packages.add("");
    }

    /**
     * Maps a namespace to a Java package. The specified prefix is used to
     * shorten the tag name of the object being serialized. For example:
     * <code>setNamespace("math", "org.jscience.mathematics")</code> associates
     * the namespace prefix <code>math</code> with the namespace name
     * <code>java:org.jscience.mathematics</code>. Any class within the package
     * <code>org.jscience.mathematics</code> now uses the <code>math</code> 
     * prefix. Any previous association of the specified prefix or package is
     * removed. Applications may set the default namespace using the prefix 
     * <code>""</code>, in which case classes not part of any namespace 
     * (not even the default one) use the explicit <code>"root"</code> prefix.
     *
     * @param  prefix the namespace prefix or <code>""</code> to set the default
     *         namespace.
     * @param  packageName of the package associated to the specified prefix.
     */
    public void setNamespace(String prefix, String packageName) {
        // If default namespace, create a "default" prefix.
        if (prefix.length() == 0) {
            setNamespace("root", "");
        }

        // Removes any previous association of the specified package.
        for (int i = 0; i < _packages.size(); i++) {
            if (packageName.equals(_packages.get(i))) {
                _packages.remove(i);
                _prefixes.remove(i);
                break;
            }
        }

        // Overrides prefix association (if any)
        for (int i = 0; i < _prefixes.size(); i++) {
            if (prefix.equals(_prefixes.get(i))) {
                _packages.set(i, packageName);
                return; // Done.
            }
        }

        // New association.
        _prefixes.add(prefix);
        _packages.add(packageName);
    }

    /**
     * Sets the indentation <code>String</code> (default two-spaces).
     *
     * @param  indent the indent <code>String</code>, usually some number of
     *         spaces.
     */
    public void setIndent(String indent) {
        _indent = indent;
    }

    /**
     * Indicates if the XML prolog has to be written
     * (default <code>true</code>).
     *
     * @param  isProlog <code>true</code> if the XML prolog has to be written;
     *         <code>false</code> otherwise.
     */
    public void setProlog(boolean isProlog) {
        _isProlog = isProlog;
    }

    /**
     * Clears the internal object references maintained by this writer.
     * Objects previously written will not be refered to, they will be
     * send again.
     */
    public void reset() {
        _objectToId.clear();
        _idCount = 0;
    }

    /**
     * Writes the specified object to the given writer in XML format.
     * The writer is closed once serialization is complete.
     * 
     * <p> Note: For instances of <code>java.io.OutputStreamWriter</code>,
     *     the stream encoding is specified in the prolog (if any).</p> 
     *
     * @param  obj the object to format.
     * @param  writer the writer to write to.
     * @throws IOException if there's any problem writing.
     */
    public void write(Object obj, Writer writer) throws IOException {
    	write(obj, writer, false);
    }

    /**
     * Writes the specified object to the given writer in XML format.
     * 
     * <p> Note: For instances of <code>java.io.OutputStreamWriter</code>,
     *     the stream encoding is specified in the prolog (if any).</p> 
     *
     * @param  obj the object to format.
     * @param  writer the writer to write to.
     * @param  keepOpen indicates if the writer is kept open once serialization
     *         is complete.
     * @throws IOException if there's any problem writing.
     */
    public void write(Object obj, Writer writer, boolean keepOpen) throws IOException {
        if (_isProlog) {
            if ((writer instanceof OutputStreamWriter)
                    && (GET_ENCODING != null)) {
                String encoding = (String) GET_ENCODING.invoke(writer);
                writer.write("<?xml version=\"1.0\" encoding=\"" + encoding
                        + "\"?>\n");
            } else {
                writer.write("<?xml version=\"1.0\"?>\n");
            }
        }
        writeElement(obj, writer, 0);
        writer.flush();
        if (!keepOpen) writer.close();
    }

    private static final Reflection.Method GET_ENCODING = Reflection
            .getMethod("java.io.OutputStreamWriter.getEncoding()");

    /**
     * Writes the specified object to the given output stream in XML format. 
     * The characters are written using UTF-8 encoding. The stream is 
     * closed once serialization is complete.
     *
     * @param  obj the object to format.
     * @param  out the output stream to write to.
     * @throws IOException if there's any problem writing.
     */
    public void write(Object obj, OutputStream out) throws IOException {
        write(obj, out, false);
    }

    /**
     * Writes the specified object to the given output stream in XML format. 
     * The characters are written using UTF-8 encoding.
     *
     * @param  obj the object to format.
     * @param  out the output stream to write to.
     * @param  keepOpen indicates if the stream is kept open once serialization
     *         is complete.
     * @throws IOException if there's any problem writing.
     */
    public void write(Object obj, OutputStream out, boolean keepOpen) throws IOException {
        _utf8StreamWriter.setOutputStream(out);
        if (_isProlog) {
            _utf8StreamWriter
                    .write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        }
        writeElement(obj, _utf8StreamWriter, 0);
        _utf8StreamWriter.flush();
        if (!keepOpen) _utf8StreamWriter.close();
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
        _utf8ByteBufferWriter.setByteBuffer(byteBuffer);
        if (_isProlog) {
            _utf8ByteBufferWriter
                    .write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        }
        writeElement(obj, _utf8ByteBufferWriter, 0);
        _utf8ByteBufferWriter.close();
    }

    /*
     * Writes the specified element.
     *
     * @param obj the object to serialize.
     * @param out the writer to write to.
     * @param level the level of nesting (0 for root).
     */
    private void writeElement(Object obj, Writer writer, int level)
            throws IOException {
        // Checks for Character Data
        if (obj instanceof CharacterData) {
            CharacterData cd = (CharacterData) obj; 
            writer.write("<![CDATA[");
            for (int i=0; i < cd.length();) {
                writer.write(cd.charAt(i++));
            }
            writer.write("]]>");
            return;
        }
        // Indentation.
        if (level > 0) {
            writer.write('\n');
            for (int i = 0; i < level; i++) {
                writer.write(_indent);
            }
        }

        // Serializes.
        XmlElement xml = _stack[level];
        xml._objectClass = obj.getClass();
        xml._format = XmlFormat.getInstance(xml._objectClass);

        String idName = xml._format.identifier(false);
        if (idName != null) { // Identifier to be used.
            CharSequence idValue = (CharSequence)_objectToId.get(obj);
            if (idValue != null) { // Reference.
                String refName = xml._format.identifier(true);
                if (refName.equals(idValue)) {
                    throw new Error("Identifier for reference and non-reference should" +
                            " be distinct (XmlFormat for " + xml._objectClass +
                    " )");
                }
                xml.setAttribute(refName, idValue);
            } else { // New object to be identified.
                xml._format.format(obj, xml);
                idValue = xml.getAttribute(idName);
                if (idValue == null) { // Automatic assignment.
                    idValue = Text.valueOf(++_idCount);
                }
                _objectToId.put(obj, idValue);    
            }
        } else {
            xml._format.format(obj, xml);
        }        

        // Searches for associated prefix with longest package name.
        String prefix = null;
        String pkgName = "";
        String className = xml._objectClass.getName();

        for (int i = 0; i < _packages.size(); i++) {
            String pkg = (String) _packages.get(i);
            if ((pkg.length() >= pkgName.length())
                    && (className.startsWith(pkg))) {
                prefix = (String) _prefixes.get(i);
                pkgName = pkg;
            }
        }
        if (prefix == null) {
            throw new UnsupportedOperationException("Class " + className
                    + " does not belong to any namespace (not even default)");
        }

        // Construct element's tag.
        String tag = (pkgName.length() != 0) ? (prefix.length() != 0) ? prefix
                + ":" + className.substring(pkgName.length() + 1) : className
                .substring(pkgName.length() + 1)
                : (prefix.length() != 0) ? prefix + ":" + className : className;

        // Writes start tag.
        writer.write('<');
        writer.write(tag);
        if (level == 0) {
            // Writes namespace declaration (root).
            for (int i = 0; i < _prefixes.size(); i++) {
                String pfx = (String) _prefixes.get(i);
                String pkg = (String) _packages.get(i);
                if (pfx.length() == 0) { // Default namespace.
                    if (pkg.length() != 0) {
                        writer.write(" xmlns=\"java:");
                        writer.write(pkg);
                        writer.write('"');
                    }
                } else {
                    writer.write(" xmlns:");
                    writer.write(pfx);
                    writer.write("=\"java:");
                    writer.write(pkg);
                    writer.write('"');
                }
            }
        }

        // Writes attributes
        for (Iterator i = xml.getAttributes().entrySet().iterator(); i
                .hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            CharSequence value = (CharSequence) entry.getValue();

            writer.write(' ');
            writer.write(key);
            writer.write("=\"");
            ObjectWriter.write(writer, value);
            writer.write('"');
        }
        // Writes content
        if (xml.isEmpty()) {
            writer.write("/>"); // Empty element.
        } else {
            writer.write(">");
            for (Iterator i = xml.iterator(); i.hasNext();) {
                Object child = i.next();
                writeElement(child, writer, level + 1);
            }
            // Indentation.
            writer.write('\n');
            for (int i = 0; i < level; i++) {
                writer.write(_indent);
            }
            writer.write("</");
            writer.write(tag);
            writer.write('>');
        }

        // Clears XmlElement for reuse.
        xml.reset();
    }

    /**
     * Writes the specified character and replaces special characters with their
     * appropriate entity reference suitable for XML attributes.
     */
    private static void write(Writer writer, CharSequence chars)
            throws IOException {
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            switch (c) {
            case '<':
                writer.write("&lt;");
                break;
            case '>':
                writer.write("&gt;");
                break;
            case '\'':
                writer.write("&apos;");
                break;
            case '\"':
                writer.write("&quot;");
                break;
            case '&':
                writer.write("&amp;");
                break;
            default:
                if (c >= ' ') {
                    writer.write(c);
                } else {
                    writer.write("&#");
                    writer.write(Integer.toString(c));
                    writer.write(';');
                }
            }
        }
    }
}