package _templates.javolution.xml.stream;

import _templates.java.lang.CharSequence;
import _templates.javax.realtime.MemoryArea;
import _templates.javolution.lang.Reusable;
import _templates.javolution.text.CharArray;
import _templates.javolution.text.Text;
import _templates.javolution.xml.sax.Attributes;

/**
 * This class provides the implementation of the {@link Attributes}
 * interface for the StAX parser. 
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 */
final class AttributesImpl implements Attributes, Reusable {

    /**
     * Holds the local names.
     */
    private CharArray[] _localNames = new CharArray[16];

    /**
     * Holds the prefixes.
     */
    private CharArray[] _prefixes = new CharArray[16];

    /**
     * Holds the qualified names.
     */
    private CharArray[] _qNames = new CharArray[16];

    /**
     * Holds the values.
     */
    private CharArray[] _values = new CharArray[16];

    /**
     * Holds the namespace stack.
     */
    private final NamespacesImpl _namespaces;

    /**
     * Holds the current number of attributes.
     */
    private int _length;

    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////

    /**
     * Creates a list of attribute using the specified namespace stack.
     */
    public AttributesImpl(NamespacesImpl namespaces) {
        _namespaces = namespaces;
    }

    // Implements Attributes.
    public int getLength() {
        return _length;
    }

    // Implements Attributes.
    public CharArray getURI(int index) {
        return (index >= 0 && index < _length) ? _namespaces
                .getNamespaceURINullAllowed(_prefixes[index]) : null;
    }

    // Implements Attributes.
    public CharArray getLocalName(int index) {
        return (index >= 0 && index < _length) ? _localNames[index] : null;
    }

    // Implements Attributes.
    public CharArray getPrefix(int index) {
        return (index >= 0 && index < _length) ? _prefixes[index] : null;
    }

    // Implements Attributes.
    public CharArray getQName(int index) {
        return (index >= 0 && index < _length) ? _qNames[index] : null;
    }

    // Implements Attributes.
    public CharArray getType(int index) {
        return (index >= 0 && index < _length) ? CDATA : null;
    }

    private static final CharArray CDATA = new CharArray("CDATA");

    // Implements Attributes.
    public CharArray getValue(int index) {
        return (index >= 0 && index < _length) ? _values[index] : null;
    }

    // Implements Attributes.
    public int getIndex(CharSequence uri, CharSequence localName) {
        if (uri == null) 
            throw new IllegalArgumentException(
                    "null namespace URI is not allowed");
        for (int i = 0; i < _length; i++) {
            if (_localNames[i].equals(localName)) {
                CharArray ns = _namespaces.getNamespaceURINullAllowed(_prefixes[i]);
                if ((ns != null) && ns.equals(uri))
                    return i;
            }
        }
        return -1;
    }

    // Implements Attributes.
    public int getIndex(CharSequence qName) {
        for (int i = 0; i < _length; i++) {
            if (_qNames[i].equals(qName))
                return i;
        }
        return -1;
    }

    // Implements Attributes.
    public CharArray getType(CharSequence uri, CharSequence localName) {
        final int index = getIndex(uri, localName);
        return (index >= 0) ? CDATA : null;
    }

    // Implements Attributes.
    public CharArray getType(CharSequence qName) {
        final int index = getIndex(qName);
        return (index >= 0) ? CDATA : null;
    }

    // Implements Attributes.
    public CharArray getValue(CharSequence uri, CharSequence localName) {
        final int index = getIndex(uri, localName);
        return (index >= 0) ? _values[index] : null;
    }

    // Implements Attributes.
    public CharArray getValue(CharSequence qName) {
        final int index = getIndex(qName);
        return (index >= 0) ? _values[index] : null;
    }

    /**
     * Clear the attribute list for reuse.
     */
    public void reset() {
        _length = 0;
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
        if (_length >= _localNames.length) {
            increaseCapacity();
        }
        _localNames[_length] = localName;
        _prefixes[_length] = prefix;
        _qNames[_length] = qName;
        _values[_length++] = value;
    }

    /**
     * Returns the string representation of these attributes.
     * 
     * @return this attributes textual representation.
     */
    public String toString() {
        Text text = Text.valueOf('[');
        final Text equ = Text.valueOf('=');
        final Text sep = Text.valueOf(", ");
        for (int i = 0; i < _length;) {
            text = text.concat(Text.valueOf(_qNames[i]).concat(equ).concat(
                    Text.valueOf(_values[i])));
            if (++i != _length) {
                text = text.concat(sep);
            }
        }
        return text.concat(Text.valueOf(']')).toString();
    }

    private void increaseCapacity() {
        MemoryArea.getMemoryArea(this).executeInArea(new Runnable() {
            public void run() {
                final int newCapacity = _length * 2;

                CharArray[] tmp = new CharArray[newCapacity];
                System.arraycopy(_localNames, 0, tmp, 0, _length);
                _localNames = tmp;

                tmp = new CharArray[newCapacity];
                System.arraycopy(_prefixes, 0, tmp, 0, _length);
                _prefixes = tmp;

                tmp = new CharArray[newCapacity];
                System.arraycopy(_qNames, 0, tmp, 0, _length);
                _qNames = tmp;

                tmp = new CharArray[newCapacity];
                System.arraycopy(_values, 0, tmp, 0, _length);
                _values = tmp;
            }
        });
    }

}