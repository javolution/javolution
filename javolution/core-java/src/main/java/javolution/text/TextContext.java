/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import javolution.context.AbstractContext;
import javolution.context.FormatContext;
import javolution.osgi.internal.OSGiServices;

/**
 * <p> A context for plain text parsing/formatting. The default text 
 *     format for any class is given by the 
 *     {@link javolution.text.DefaultTextFormat DefaultTextFormat} 
 *     inheritable annotation.</p>
 * 
 * <p> A default format exists for the following predefined types:
 *     <code><ul>
 *       <li>java.lang.String</li>
 *       <li>java.lang.Boolean</li>
 *       <li>java.lang.Character</li>
 *       <li>java.lang.Byte</li>
 *       <li>java.lang.Short</li>
 *       <li>java.lang.Integer</li>
 *       <li>java.lang.Long</li>
 *       <li>java.lang.Float</li>
 *       <li>java.lang.Double</li>
 *       <li>java.lang.Class</li>
 *     </ul></code></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class TextContext extends FormatContext {

    /**
     * Default constructor.
     */
    protected TextContext() {}

    /**
     * Enters and returns a new text context instance.
     */
    public static TextContext enter() {
        return (TextContext) TextContext.currentTextContext().enterInner();
    }

    /**
     * Returns the string representation of the specified object using 
     * its current format (convenience method).
     * If there is no format associated with this object; then the object 
     * default representation is returned (e.g. {@code Object#12345}).
     */
    public static String toString(Object obj) {
        TextFormat<Object> textFormat = getFormat(obj.getClass());
        return (textFormat != null) ? textFormat.format(obj) : 
            "Object#" + System.identityHashCode(obj);
    }

    /**
     * Returns the object of the specified class corresponding to the 
     * specified textual representation (convenience method).
     * 
     * @throws UnsupportedOperationException if the specified type has no 
     *         format associated.
     */
    public static <T> T parse(CharSequence csq, Class<T> type) {
        TextFormat<T> textFormat = getFormat(type);
        if (textFormat == null) throw new UnsupportedOperationException(
                "No text format defined for " + type);
        return textFormat.parse(csq);
    }

    /**
     * Returns the text format for the specified type or {@code null}
     * if none.
     */
    public static <T> TextFormat<T> getFormat(Class<? extends T> type) {
        return TextContext.currentTextContext().getFormatInContext(type);
    }

    /**
     * Sets the new text format for the specified type (and its sub-types).
     */
    public abstract <T> void setFormat(Class<? extends T> type,
            TextFormat<T> newFormat);

    /**
     * Returns the plain text format for the specified type.
     */
    protected abstract <T> TextFormat<T> getFormatInContext(
            Class<? extends T> type);

    /**
     * Returns the current text context.
     */
    protected static TextContext currentTextContext() {
        TextContext ctx = AbstractContext.current(TextContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getTextContext();
    }
}