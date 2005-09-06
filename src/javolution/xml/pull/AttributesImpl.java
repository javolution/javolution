package javolution.xml.pull;

import j2me.lang.CharSequence;
import javolution.lang.PersistentReference;
import javolution.lang.Reusable;
import javolution.xml.sax.Attributes;

/**
 * This class provides the implementation of the {@link Attributes}
 * interface for the pull parser. 
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 */
final class AttributesImpl implements Attributes, Reusable {

    /**
     * Holds the configurable nominal capacity (number of entries).
     */
    private static final PersistentReference CAPACITY = new PersistentReference(
            "javolution.xml.sax.AttributesImpl#CAPACITY", new Integer(16));

    /**
     * Holds the uri namespaces.
     */
    private CharSequenceImpl[] _uris = new CharSequenceImpl[((Integer) CAPACITY
            .get()).intValue()];

    /**
     * Holds the local names.
     */
    private CharSequenceImpl[] _localNames = new CharSequenceImpl[((Integer) CAPACITY
            .get()).intValue()];

    /**
     * Holds the prefixes.
     */
    private CharSequenceImpl[] _prefixes = new CharSequenceImpl[((Integer) CAPACITY
            .get()).intValue()];

    /**
     * Holds the qualified names.
     */
    private CharSequenceImpl[] _qNames = new CharSequenceImpl[((Integer) CAPACITY
            .get()).intValue()];

    /**
     * Holds the values.
     */
    private CharSequenceImpl[] _values = new CharSequenceImpl[((Integer) CAPACITY
            .get()).intValue()];

    /**
     * Holds the current number of attributes.
     */
    private int _length;

    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public AttributesImpl() {
    }

    // Implements Attributes.
    public int getLength() {
        return _length;
    }

    // Implements Attributes.
    public CharSequence getURI(int index) {
        return (index >= 0 && index < _length) ? _uris[index] : null;
    }

    // Implements Attributes.
    public CharSequence getLocalName(int index) {
        return (index >= 0 && index < _length) ? _localNames[index] : null;
    }

    // Implements Attributes.
    public CharSequence getPrefix(int index) {
        return (index >= 0 && index < _length) ? _prefixes[index] : null;
    }

    // Implements Attributes.
    public CharSequence getQName(int index) {
        return (index >= 0 && index < _length) ? _qNames[index] : null;
    }

    // Implements Attributes.
    public String getType(int index) {
        return (index >= 0 && index < _length) ? "CDATA" : null;
    }

    // Implements Attributes.
    public CharSequence getValue(int index) {
        return (index >= 0 && index < _length) ? _values[index] : null;
    }

    // Implements Attributes.
    public int getIndex(String uri, String localName) {
        for (int i = 0; i < _length; i++) {
            if (_localNames[i].equals(localName) && 
                    (_uris[i].equals(uri)))
                return i;
        }
        return -1;
    }

    // Implements Attributes.
    public int getIndex(String qName) {
        for (int i = 0; i < _length; i++) {
            if (_qNames[i].equals(qName))
                return i;
        }
        return -1;
    }

    // Implements Attributes.
    public String getType(String uri, String localName) {
        final int index = getIndex(uri, localName);
        return (index >= 0) ? "CDATA" : null;
    }

    // Implements Attributes.
    public String getType(String qName) {
        final int index = getIndex(qName);
        return (index >= 0) ? "CDATA" : null;
    }

    // Implements Attributes.
    public CharSequence getValue(String uri, String localName) {
        final int index = getIndex(uri, localName);
        return (index >= 0) ? _values[index] : null;
    }

    // Implements Attributes.
    public CharSequence getValue(String qName) {
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
     * @param uri the namespace URI or an empty sequence.
     * @param localName the local name or an empty sequence.
     * @param prefix the prefix or an empty sequence.
     * @param qName the qualified (prefixed) name or an empty sequence.
     * @param value the attribute value.
     */
    public void addAttribute(CharSequenceImpl uri, CharSequenceImpl localName,
            CharSequenceImpl prefix, CharSequenceImpl qName,
            CharSequenceImpl value) {
        if (_length >= _uris.length) { // Resizes.
            final int newCapacity = _length * 2;
            CAPACITY.setMinimum(new Integer(newCapacity));

            CharSequenceImpl[] tmp = new CharSequenceImpl[newCapacity];
            System.arraycopy(_uris, 0, tmp, 0, _length);
            _uris = tmp;

            tmp = new CharSequenceImpl[newCapacity];
            System.arraycopy(_localNames, 0, tmp, 0, _length);
            _localNames = tmp;

            tmp = new CharSequenceImpl[newCapacity];
            System.arraycopy(_prefixes, 0, tmp, 0, _length);
            _prefixes = tmp;

            tmp = new CharSequenceImpl[newCapacity];
            System.arraycopy(_qNames, 0, tmp, 0, _length);
            _qNames = tmp;

            tmp = new CharSequenceImpl[newCapacity];
            System.arraycopy(_values, 0, tmp, 0, _length);
            _values = tmp;
        }
        _uris[_length] = uri;
        _localNames[_length] = localName;
        _prefixes[_length] = prefix;
        _qNames[_length] = qName;
        _values[_length++] = value;
    }
}