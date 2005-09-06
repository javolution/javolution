package javolution.xml;

import j2me.lang.CharSequence;
import javolution.lang.PersistentReference;
import javolution.lang.Reusable;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.xml.sax.Attributes;

/**
 * This class provides the implementation of the {@link Attributes}
 * interface during formatting.
 *  
 * Note: Only qualified name are being used during formatting.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 */
final class FormatAttributes implements Attributes, Reusable {

    /**
     * Holds the configurable nominal capacity (number of entries).
     */
    private static final PersistentReference CAPACITY = new PersistentReference(
            "javolution.xml.FormatAttributes#CAPACITY", new Integer(16));

    /**
     * Holds the qualified names.
     */
    private String[] _qNames = new String[((Integer) CAPACITY
            .get()).intValue()];

    /**
     * Holds the values.
     */
    private CharSequence[] _values = new CharSequence[((Integer) CAPACITY
            .get()).intValue()];

    /**
     * Holds the text builders (to avoid object creation).
     */
    private TextBuilder[] _textBuilders = new TextBuilder[((Integer) CAPACITY
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
    public FormatAttributes() {
        for (int i=0; i < _textBuilders.length;) {
            _textBuilders[i++] = new TextBuilder();
        }
    }

    // Implements Attributes.
    public int getLength() {
        return _length;
    }

    // Implements Attributes.
    public CharSequence getURI(int index) {
        return (index >= 0 && index < _length) ? Text.EMPTY : null;
    }

    // Implements Attributes.
    public CharSequence getLocalName(int index) {
        return (index >= 0 && index < _length) ? toCharSeq(_qNames[index]) : null;
    }

    // Implements Attributes.
    public CharSequence getPrefix(int index) {
        return (index >= 0 && index < _length) ? Text.EMPTY : null;
    }

    // Implements Attributes.
    public CharSequence getQName(int index) {
        return (index >= 0 && index < _length) ? toCharSeq(_qNames[index]) : null;
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
            if (_qNames[i].equals(localName))
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
     * @param qName the qualified (prefixed) name.
     * @param value the attribute value.
     */
    public void addAttribute(String qName, CharSequence value) {
        if (_length >= _qNames.length) { // Resizes.
            final int newCapacity = _length * 2;
            CAPACITY.setMinimum(new Integer(newCapacity));

            String[] tmp1 = new String[newCapacity];
            System.arraycopy(_qNames, 0, tmp1, 0, _length);
            _qNames = tmp1;

            CharSequence[] tmp2 = new CharSequence[newCapacity];
            System.arraycopy(_values, 0, tmp2, 0, _length);
            _values = tmp2;
            
            TextBuilder[] tmp3 = new TextBuilder[newCapacity];
            System.arraycopy(_textBuilders, 0, tmp3, 0, _length);
            _textBuilders = tmp3;
            for (int i=_length; i < _textBuilders.length;) { // Populates.
                _textBuilders[i++] = new TextBuilder();
            }
        }
        _qNames[_length] = qName;
        _values[_length++] = value;
    }

    /**
     * Adds a new attribute whose values is to be hold by the specified 
     * {@link TextBuilder}.
     * 
     * @param qName the qualified (prefixed) name.
     * @return an empty text builder to hold the attribute value.
     */
    public TextBuilder newAttribute(String qName) {
        addAttribute(qName, null);
        TextBuilder tb = _textBuilders[_length-1];
        _values[_length-1] = tb;
        tb.reset();
        return tb;
    }

    /**
     * Removes the attribute at the specified index.
     * 
     * @param index 
     * @param value the attribute value.
     */
    public void remove(int index) {
        _qNames[index] = _qNames[--_length];
        _values[index] = _values[_length];
        TextBuilder tmp = _textBuilders[_length];
        _textBuilders[_length] = _textBuilders[index];
        _textBuilders[index] = tmp;
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