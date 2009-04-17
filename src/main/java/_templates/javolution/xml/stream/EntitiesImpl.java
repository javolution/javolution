/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.javolution.xml.stream;

import _templates.java.lang.CharSequence;
import _templates.java.util.Collection;
import _templates.java.util.Iterator;
import _templates.java.util.Map;
import _templates.javolution.lang.Reusable;
import _templates.javolution.text.CharArray;
import _templates.javolution.util.FastCollection;
import _templates.javolution.util.FastCollection.Record;

/**
 * This class holds defined entities while parsing.
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, June 16, 2006
 */
final class EntitiesImpl implements Reusable {

    /**
     * Holds maximum length.
     */
    private int _maxLength = 1;

    /**
     * Holds the user defined entities mapping.
     */
    private Map _entitiesMapping;

    /**
     * Default constructor.
     */
    EntitiesImpl() {
    }

    /**
     * Returns the length of the largest entity.
     * 
     * @return the length largest entity.
     */
    public int getMaxLength() {
        return _maxLength;
    }

    /**
     * Replaces the entity at the specified position. 
     * The five predefined XML entities "&#38;lt;", "&#38;gt;", "&#38;apos;",
     * "&#38;quot;", "&#38;amp;" as well as character refererences 
     * (decimal or hexadecimal) are always recognized.
     * 
     * @param buffer the data buffer.
     * @param start the index of entity first character (index of '&')
     * @return the length of the replacement entity (including ';') 
     * @throws XMLStreamException if the entity is not recognized.
     */
    public int replaceEntity(char[] buffer, int start, int length)
            throws XMLStreamException {

        // Checks for character references.
        if (buffer[start + 1] == '#') {
            char c = buffer[start + 2];
            int base = (c == 'x') ? 16 : 10;
            int i = (c == 'x') ? 3 : 2;
            int charValue = 0;
            for (; i < length - 1; i++) {
                c = buffer[start + i];
                charValue *= base;
                charValue += (c <= '9') ? (c - '0') : (c <= 'Z') ? c - 'A'
                        : c - 'a';
            }
            buffer[start] = (char) charValue;
            return 1;
        }

        if ((buffer[start + 1] == 'l') && (buffer[start + 2] == 't')
                && (buffer[start + 3] == ';')) {
            buffer[start] = '<';
            return 1;
        }

        if ((buffer[start + 1] == 'g') && (buffer[start + 2] == 't')
                && (buffer[start + 3] == ';')) {
            buffer[start] = '>';
            return 1;
        }

        if ((buffer[start + 1] == 'a') && (buffer[start + 2] == 'p')
                && (buffer[start + 3] == 'o') && (buffer[start + 4] == 's')
                && (buffer[start + 5] == ';')) {
            buffer[start] = '\'';
            return 1;
        }

        if ((buffer[start + 1] == 'q') && (buffer[start + 2] == 'u')
                && (buffer[start + 3] == 'o') && (buffer[start + 4] == 't')
                && (buffer[start + 5] == ';')) {
            buffer[start] = '"';
            return 1;
        }

        if ((buffer[start + 1] == 'a') && (buffer[start + 2] == 'm')
                && (buffer[start + 3] == 'p') && (buffer[start + 4] == ';')) {
            buffer[start] = '&';
            return 1;
        }

        // Searches user defined entities.
        _tmp.setArray(buffer, start + 1, length - 2);
        CharSequence replacementText = (_entitiesMapping != null) ?
                (CharSequence) _entitiesMapping.get(_tmp) : null;
        if (replacementText == null)
            throw new XMLStreamException("Entity " + _tmp + " not recognized");
        int replacementTextLength = replacementText.length();
        for (int i = 0; i < replacementTextLength; i++) {
            buffer[start + i] = replacementText.charAt(i);
        }
        return replacementTextLength;
    }

    private CharArray _tmp = new CharArray();

    /**
     * Defines a custom entity mapping. 
     * 
     * @param entityToReplacementText the entity (e.g. "copy") to replacement 
     *        text (e.g. "©") mapping (both CharSequence).
     */
    public void setEntitiesMapping(Map entityToReplacementText) {
        // Sets the maximum length for replacement text.
        Collection values = entityToReplacementText.values();
        if (values instanceof FastCollection) { // Avoids allocating iterators.
             FastCollection fc = (FastCollection) values;
             for (Record r=fc.head(), t=fc.tail(); (r = r.getNext())!= t;) {
                 CharSequence value = (CharSequence) fc.valueOf(r);
                 if (_maxLength < value.length()) {
                     _maxLength = value.length();
                 }
             }
        } else {
            for (Iterator i=values.iterator(); i.hasNext();) {
                CharSequence value = (CharSequence) i.next();
                if (_maxLength < value.length()) {
                    _maxLength = value.length();
                }
            }
        }
        _entitiesMapping = entityToReplacementText;
    }

    /**
     * Returns the custom entity mapping. 
     * 
     * @return the entity (e.g. "copy") to replacement text (e.g. "©") mapping 
     *         (both CharSequence).
     */
    public Map getEntitiesMapping() {
        return _entitiesMapping;
    }
    
    // Implements Reusable.
    public void reset() {
        _maxLength = 1;
        _entitiesMapping = null;
    }

}
