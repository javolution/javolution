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
 * <p> A context for plain text parsing/formatting. This context provides 
 *     the {@link javolution.text.TextFormat TextFormat} to parse/format objects
 *     of any class. If not superseded, the text format for a class is specified
 *     by the {@link javolution.text.DefaultTextFormat DefaultTextFormat} 
 *     annotation.</p>
 * 
 * <p> A text context always returns the most specialized format. If the class 
 *     has no default format annotation (inherited or not), then the default 
 *     {@link java.lang.Object} format (which calls {@link Object#toString})
 *     is returned. A predefined format exists for the following standard types:
 *     <code><ul>
 *       <li>java.lang.Object (parsing not supported, formatting calls toString())</li>
 *       <li>java.lang.Boolean</li>
 *       <li>java.lang.Character</li>
 *       <li>java.lang.Byte</li>
 *       <li>java.lang.Short</li>
 *       <li>java.lang.Integer</li>
 *       <li>java.lang.Long</li>
 *       <li>java.lang.Float</li>
 *       <li>java.lang.Double</li>
 *       <li>java.lang.Class</li>
 *       <li>java.lang.String</li>
 *       <li>java.util.Date (ISO 8601)</li> 
 *       <li>java.math.BigInteger</li>
 *       <li>java.math.BigDecimal</li>
 *       <li>java.awt.Color (hexadecimal RGB value, e.g. {@code 0x112233})</li>
 *       <li>java.awt.Font</li>
 *     </ul></code>
 *     </p>
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
     * Returns the text format for the specified type. It is the most 
     * specialized format able to parse/format instances of the specified 
     * class. If there is no default format for the specified class, 
     * the standard object format (toString based) is returned.
     */
    public static <T> TextFormat<T> getFormat(Class<? extends T> type) {
        return TextContext.currentTextContext().searchFormat(type);
    }

    /**
     * Sets the text format for the specified type (and its sub-types).
     */
    public abstract <T> void setFormat(Class<? extends T> type,
            TextFormat<T> newFormat);

    /**
     * Searches the most specialized format for the specified type.
     */
    protected abstract <T> TextFormat<T> searchFormat(Class<? extends T> type);

    /**
     * Returns the current text context.
     */
    private static TextContext currentTextContext() {
        TextContext ctx = AbstractContext.current(TextContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getTextContext();
    }
}