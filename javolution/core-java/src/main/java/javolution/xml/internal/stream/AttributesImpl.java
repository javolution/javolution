package javolution.xml.internal.stream;

import javolution.text.CharArray;
import javolution.util.FastTable;
import javolution.xml.sax.Attributes;

/**
 * This class provides the implementation of the {@link Attributes}
 * interface for the StAX parser. 
 */
public final class AttributesImpl implements Attributes {

    /**
     * Attribute implementation.
     */
    private static class AttributeImpl {
        CharArray localName;
        CharArray prefix; // null if no namespace URI.
        CharArray qName;
        CharArray value;
        public String toString() {
            return qName + "=" + value;
        }

    }

    private static final CharArray CDATA = new CharArray("CDATA");

    private static final CharArray EMPTY = new CharArray();

    /**
     * Holds the attributes.
     */
    private final FastTable<AttributeImpl> attributes = new FastTable<AttributeImpl>();

    /**
     * Holds the current number of attributes set.
     */
    private int length;

    /**
     * Holds the namespace stack.
     */
    private final NamespacesImpl namespaces;

    /**
     * Creates a list of attribute using the specified namespace stack.
     */
    public AttributesImpl(NamespacesImpl namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Adds an attribute to the end of the attribute list.
     * 
     * @param localName the local name.
     * @param prefix the prefix or <code>null</code> if none.
     * @param qName the qualified (prefixed) name.
     * @param value the attribute value.
     */
    public void addAttribute(CharArray localName, CharArray prefix,
            CharArray qName, CharArray value) {
        AttributeImpl attribute;
        if (length >= attributes.size()) {
            attribute = new AttributeImpl();
            attributes.add(attribute);
        } else {
            attribute = attributes.get(length);
        }
        attribute.localName = localName;
        attribute.prefix = prefix;
        attribute.qName = qName;
        attribute.value = value;
    }

    @Override
    public int getIndex(CharSequence qName) {
        for (int i = 0; i < length; i++) {
            if (qName.equals(attributes.get(i).qName))
                return i;
        }
        return -1;
    }

    @Override
    public int getIndex(CharSequence uri, CharSequence localName) {
        for (int i = 0; i < length; i++) {
            AttributeImpl attribute = attributes.get(i);
            if (localName.equals(attribute.localName)) {
                if (attribute.prefix == null) { // No namespace URI.
                    if (uri.length() == 0)
                        return i;
                } else { // Check if matching namespace URI.
                    if (uri.equals(namespaces.getNamespaceURI(attribute.prefix)))
                        return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public CharArray getLocalName(int index) {
        if ((index < 0) || (index >= length)) return null;
        return attributes.get(index).localName;
    }

    public CharArray getPrefix(int index) {
        if ((index < 0) || (index >= length)) return null;
        return attributes.get(index).prefix;
    }

    @Override
    public CharArray getQName(int index) {
        if ((index < 0) || (index >= length)) return null;
        return attributes.get(index).qName;
    }

    @Override
    public CharArray getType(CharSequence qName) {
        return (getIndex(qName) >= 0) ? CDATA : null;
    }

    @Override
    public CharArray getType(CharSequence uri, CharSequence localName) {
         return (getIndex(uri, localName) >= 0) ? CDATA : null;
    }

    @Override
    public CharArray getType(int index) {
        if ((index < 0) || (index >= length)) return null;
        return CDATA;
    }
    @Override
    public CharArray getURI(int index) {
        if ((index < 0) || (index >= length)) return null;
        CharArray prefix = attributes.get(index).prefix;
        return (prefix == null) ? EMPTY : namespaces.getNamespaceURI(prefix);
    }

    @Override
    public CharArray getValue(CharSequence qName) {
        final int index = getIndex(qName);
        return (index >= 0) ? attributes.get(index).value : null;
    }

    @Override
    public CharArray getValue(CharSequence uri, CharSequence localName) {
        final int index = getIndex(uri, localName);
        return (index >= 0) ? attributes.get(index).value : null;
    }

    @Override
    public CharArray getValue(int index) {
        if ((index < 0) || (index >= length)) return null;
        return attributes.get(index).value;
    }

    /**
     * Clear the attribute list for reuse.
     */
    public void reset() {
        length = 0;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}