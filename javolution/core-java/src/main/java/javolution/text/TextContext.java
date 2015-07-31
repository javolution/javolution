/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.text;

import java.io.IOException;

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
 * <p> A text context always returns the most specialized format. If a class 
 *     has no default format annotation (inherited or not), then the default 
 *     {@link java.lang.Object} format (which calls {@link Object#toString})
 *     is returned. A predefined format exists for the following standard types:</p>
 *     <ul>
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
 *     </ul>
 *     
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
     * @return Reference to the entered TextContext
     */
    public static TextContext enter() {
        return (TextContext) TextContext.currentTextContext().enterInner();
    }

    /**
     * Returns the text format for the specified type. It is the most 
     * specialized format able to parse/format instances of the specified 
     * class. If there is no default format for the specified class, 
     * the standard object format (toString based) is returned.
     * @param <T> Type of the TextFormat
     * @param type Class to get a TextFormat for 
     * @return TextFormat for the given type
     */
    public static <T> TextFormat<T> getFormat(Class<? extends T> type) {
        return TextContext.currentTextContext().searchFormat(type);
    }

    /**
     * Sets the text format for the specified type (and its sub-types).
     * @param <T> Type of the TextFormat
     * @param type Class to get a TextFormat for
     * @param newFormat Format to set for the specified class
     */
    public abstract <T> void setFormat(Class<? extends T> type,
            TextFormat<T> newFormat);

    /**
     * Formats the specified object using its current {@link #getFormat(Class) 
     * format} (convenience method).
     * @param obj Object to format
     * @param dest destination of the formatted object
     * @throws java.io.IOException if an error occurs while formatting or writing the result
     * @return Appendable with formatted object written to it
     */
    public static Appendable format(Object obj, Appendable dest) throws IOException {
    	if (obj == null) return dest.append("null");
    	return TextContext.getFormat(obj.getClass()).format(obj, dest);    	
    }

    /**
     * Searches the most specialized format for the specified type.
     * @param <T> Type of the TextFormat to search for
     * @param type Type to search for a format for
     * @return TextFormat for the specified type
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