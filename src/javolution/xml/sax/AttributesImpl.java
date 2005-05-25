package javolution.xml.sax;

import j2me.lang.CharSequence;
import javolution.lang.Reusable;
import javolution.util.FastComparator;
import javolution.util.FastTable;
import javolution.xml.sax.Attributes;

/**
 * This class provides a default implementation of the {@link Attributes}
 * interface. 
 * It is a more generic version of <code>org.xml.sax.AttributesImpl</code> with
 * <code>String</code> replaced by <code>CharSequence</code>.
 * 
 * @author <a href="mailto:sax@megginson.com">David Megginson</a>
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @author <a href="mailto:javolution@arakelian.com">Gregory Arakelian </a>
 */
public final class AttributesImpl implements Attributes, Reusable {

    /**
     * Holds the entries increment between each attribute.
     * For each attribute, we store the namespace URI, the local name, the 
     * prefix, the qualified or q-name, the XML data type and the value.
     */
    private static final int INCR = 6;

    /**
     * Holds the current number of attributes.
     */
    private int _length;

    /**
     * Holds the entries.
     */
    private FastTable _entries = new FastTable();

    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public AttributesImpl() {
    }

    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Attributes.
    ////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of attributes in the list.
     * 
     * @return the number of attributes in the list.
     */
    public int getLength() {
        return _length;
    }

    /**
     * Return an attribute namespace URI.
     * 
     * @param index the attribute index (zero-based).
     * @return the namespace URI, the empty string if none is available,
     *         or null if the index is out of range.
     */
    public CharSequence getURI(int index) {
        if (index >= 0 && index < _length)
            return (CharSequence) _entries.get(index * INCR + URI_OFFSET);
        return null;
    }

    /**
     * Return an attribute local name.
     * 
     * @param index the attribute index (zero-based).
     * @return The attribute local name, the empty string if none is
     *         available, or null if the index if out of range.
     */
    public CharSequence getLocalName(int index) {
        if (index >= 0 && index < _length)
            return (CharSequence) _entries
                    .get(index * INCR + LOCAL_NAME_OFFSET);
        return null;
    }

    /**
     * Return an attribute prefix.
     * 
     * @param index the attribute index (zero-based).
     * @return the attribute prefix, the empty string if none is
     *         available, or null if the index is out of bounds.
     */
    public CharSequence getPrefix(int index) {
        if (index >= 0 && index < _length)
            return (CharSequence) _entries.get(index * INCR + PREFIX_OFFSET);
        return null;
    }

    /**
     * Return an attribute qualified (prefixed) name.
     * 
     * @param index the attribute index (zero-based).
     * @return the attribute qualified name, the empty string if none is
     *         available, or null if the index is out of bounds.
     */
    public CharSequence getQName(int index) {
        if (index >= 0 && index < _length)
            return (CharSequence) _entries.get(index * INCR + QNAME_OFFSET);
        return null;
    }

    /**
     * Return an attribute type by index.
     * 
     * @param index the attribute index (zero-based).
     * @return the attribute type, "CDATA" if the type is unknown, or null if
     *         the index is out of bounds.
     */
    public String getType(int index) {
        if (index >= 0 && index < _length)
            return (String) _entries.get(index * INCR + TYPE_OFFSET);
        return null;

    }

    /**
     * Return an attribute value by index.
     * 
     * @param index he attribute index (zero-based).
     * @return the attribute value or null if the index is out of bounds.
     */
    public CharSequence getValue(int index) {
        if (index >= 0 && index < _length)
            return (CharSequence) _entries.get(index * INCR + VALUE_OFFSET);
        return null;
    }

    /**
     * Look up an attribute index by namespace name.
     * In many cases, it will be more efficient to look up the name once and use
     * the index query methods rather than using the name query methods
     * repeatedly.
     * 
     * @param uri the attribute namespace URI, or the empty string if none is
     *            available.
     * @param localName the attribute local name.
     * @return the attribute index, or -1 if none matches.
     */
    public int getIndex(CharSequence uri, CharSequence localName) {
        for (int i = 0; i < _length; i++) {
            if (FastComparator.LEXICAL.areEqual(_entries.get(i * INCR
                    + URI_OFFSET), uri)
                    && FastComparator.LEXICAL.areEqual(_entries.get(i * INCR
                            + LOCAL_NAME_OFFSET), localName))
                return i;
        }
        return -1;
    }

    /**
     * Equivalent to {@link #getIndex(CharSequence, CharSequence)} 
     * (for J2ME compatibility).
     */
    public int getIndex(String uri, String localName) {
        for (int i = 0; i < _length; i++) {
            if (FastComparator.LEXICAL.areEqual(_entries.get(i * INCR
                    + URI_OFFSET), uri)
                    && FastComparator.LEXICAL.areEqual(_entries.get(i * INCR
                            + LOCAL_NAME_OFFSET), localName))
                return i;
        }
        return -1;
    }

    /**
     * Look up an attribute index by qualified (prefixed) name.
     * 
     * @param qName the qualified name.
     * @return the attribute index, or -1 if none matches.
     */
    public int getIndex(CharSequence qName) {
        for (int i = 0; i < _length; i++) {
            if (FastComparator.LEXICAL.areEqual(_entries.get(i * INCR
                    + QNAME_OFFSET), qName))
                return i;
        }
        return -1;
    }

    /**
     * Equivalent to {@link #getIndex(CharSequence)} (for J2ME compatibility).
     */
    public int getIndex(String qName) {
        for (int i = 0; i < _length; i++) {
            if (FastComparator.LEXICAL.areEqual(_entries.get(i * INCR
                    + QNAME_OFFSET), qName))
                return i;
        }
        return -1;
    }

    /**
     * Look up an attribute type by namespace-qualified name.
     * 
     * @param uri the namespace URI, or the empty string for a name with no
     *            explicit Namespace URI.
     * @param localName he local name.
     * @return the attribute type, or null if there is no matching attribute.
     */
    public String getType(CharSequence uri, CharSequence localName) {
        final int index = getIndex(uri, localName);
        if (index >= 0)
            return (String) _entries.get(index * INCR + TYPE_OFFSET);
        return null;
    }

    /**
     * Equivalent to {@link #getType(CharSequence, CharSequence)} 
     * (for J2ME compatibility).
     */
    public String getType(String uri, String localName) {
        final int index = getIndex(uri, localName);
        if (index >= 0)
            return (String) _entries.get(index * INCR + TYPE_OFFSET);
        return null;
    }

    /**
     * Look up an attribute type by qualified (prefixed) name.
     * 
     * @param qName the qualified name.
     * @return the attribute type, or null if there is no matching attribute.
     */
    public String getType(CharSequence qName) {
        final int index = getIndex(qName);
        if (index >= 0)
            return (String) _entries.get(index * INCR + TYPE_OFFSET);
        return null;
    }

    /**
     * Equivalent to {@link #getType(CharSequence)} (for J2ME compatibility).
     */
    public String getType(String qName) {
        final int index = getIndex(qName);
        if (index >= 0)
            return (String) _entries.get(index * INCR + TYPE_OFFSET);
        return null;
    }

    /**
     * Look up an attribute value by namespace-qualified name.
     * 
     * @param uri the namespace URI, or the empty string for a name with no
     *        explicit namespace URI.
     * @param localName the local name.
     * @return the attribute's value, or null if there is no matching attribute.
     */
    public CharSequence getValue(CharSequence uri, CharSequence localName) {
        final int index = getIndex(uri, localName);
        if (index >= 0)
            return (CharSequence) _entries.get(index * INCR + VALUE_OFFSET);
        return null;
    }

    /**
     * Equivalent to {@link #getValue(CharSequence, CharSequence)} 
     * (for J2ME compatibility).
     */
    public CharSequence getValue(String uri, String localName) {
        final int index = getIndex(uri, localName);
        if (index >= 0)
            return (CharSequence) _entries.get(index * INCR + VALUE_OFFSET);
        return null;
    }

    /**
     * Look up an attribute value by qualified (prefixed) name.
     * 
     * @param qName the qualified name.
     * @return the attribute value, or null if there is no matching attribute.
     */
    public CharSequence getValue(CharSequence qName) {
        final int index = getIndex(qName);
        if (index >= 0)
            return (CharSequence) _entries.get(index * INCR + VALUE_OFFSET);
        return null;
    }

    /**
     * Equivalent to {@link #getValue(CharSequence)} (for J2ME compatibility).
     */
    public CharSequence getValue(String qName) {
        final int index = getIndex(qName);
        if (index >= 0)
            return (CharSequence) _entries.get(index * INCR + VALUE_OFFSET);
        return null;
    }

    ////////////////////////////////////////////////////////////////////
    // Manipulators.
    ////////////////////////////////////////////////////////////////////

    /**
     * Clear the attribute list for reuse.
     */
    public void reset() {
        _entries.clear();
        _length = 0;
    }

    /**
     * Adds an attribute to the end of the attribute list.
     * 
     * @param uri the namespace URI or an empty sequence.
     * @param localName the local name or an empty sequence.
     * @param prefix the prefix or an empty sequence.
     * @param qName the qualified (prefixed) name or an empty sequence.
     * @param type the attribute type as a string.
     * @param value the attribute value.
     * @return the attribute index.
     */
    public int addAttribute(CharSequence uri, CharSequence localName,
            CharSequence prefix, CharSequence qName, String type,
            CharSequence value) {
        _entries.add(uri);
        _entries.add(localName);
        _entries.add(prefix);
        _entries.add(qName);
        _entries.add(type);
        _entries.add(value);
        return _length++;
    }

    private static final int URI_OFFSET = 0;

    private static final int LOCAL_NAME_OFFSET = 1;

    private static final int PREFIX_OFFSET = 2;

    private static final int QNAME_OFFSET = 3;

    private static final int TYPE_OFFSET = 4;

    private static final int VALUE_OFFSET = 5;

    /**
     * Removes an attribute from the list.
     * 
     * @param index the index of the attribute (zero-based).
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= length())</code>
     */
    public void removeAttribute(int index) {
        final int i = index * INCR;
        _entries.removeRange(i, i + INCR);
        _length--;
    }

    /**
     * Sets the namespace URI of a specific attribute.
     * 
     * @param index the index of the attribute (zero-based).
     * @param uri the attribute's namespace URI, or the empty string for none.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= length())</code>
     */
    public void setURI(int index, CharSequence uri) {
        _entries.set(index * INCR + URI_OFFSET, uri);
    }

    /**
     * Sets the local name of a specific attribute.
     * 
     * @param index the index of the attribute (zero-based).
     * @param localName the attribute local name, or the empty string for none.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= length())</code>
     */
    public void setLocalName(int index, CharSequence localName) {
        _entries.set(index * INCR + LOCAL_NAME_OFFSET, localName);
    }

    /**
     * Sets the prefix of a specific attribute.
     * 
     * @param index the index of the attribute (zero-based).
     * @param prefix the attribute prefix, or the empty string for none.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= length())</code>
     */
    public void setPrefix(int index, CharSequence prefix) {
        _entries.set(index * INCR + PREFIX_OFFSET, prefix);
    }

    /**
     * Sets the qualified name of a specific attribute.
     * 
     * @param index the index of the attribute (zero-based).
     * @param qName the attribute qualified name, or the empty string for 
     *        none.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= length())</code>
     */
    public void setQName(int index, CharSequence qName) {
        _entries.set(index * INCR + QNAME_OFFSET, qName);
    }

    /**
     * Sets the type of a specific attribute.
     * 
     * @param index the index of the attribute (zero-based).
     * @param type the attribute type.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= length())</code>
     */
    public void setType(int index, String type) {
        _entries.set(index * INCR + TYPE_OFFSET, type);
    }

    /**
     * Sets the value of a specific attribute.
     * 
     * @param index the index of the attribute (zero-based).
     * @param value the attribute value.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= length())</code>
     */
    public void setValue(int index, CharSequence value) {
        _entries.set(index * INCR + VALUE_OFFSET, value);
    }

}