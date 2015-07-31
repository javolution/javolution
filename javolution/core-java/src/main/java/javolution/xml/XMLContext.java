/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import javolution.context.AbstractContext;
import javolution.context.FormatContext;
import javolution.osgi.internal.OSGiServices;
import javolution.text.TextFormat;

/**
 * <p> A context for XML parsing/formatting. This context provides 
 *     the {@link javolution.xml.XMLFormat XMLFormat} to parse/format objects
 *     of any class. If not superseded, the XML format for a class is specified
 *     by the {@link javolution.xml.DefaultXMLFormat DefaultXMLFormat} 
 *     annotation.</p>
 * <p> A XML context always returns the most specialized format. If a class 
 *     has no default format annotation (inherited or not), then the default 
 *     {@link java.lang.Object} format (with "value" attribute is 
 *     parsed/formatted using {@link javolution.text.TextContext current 
 *     text format}) is returned. </p>
 *     <p>A predefined format exists for the following standard types:</p>
 *     <ul>
 *       <li>java.lang.Object (attribute "value" parsed/formatted using {@link TextFormat})</li>
 *       <li>java.util.Collection</li>
 *       <li>java.util.Map</li>
 *     </ul>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class XMLContext extends FormatContext {

     /**
     * Default constructor.
     */
    protected XMLContext() {}

    /**
     * Enters and returns a new xml context instance.
     * @return Reference to the entered context
     */
    public static XMLContext enter() {
        return (XMLContext) XMLContext.currentXMLContext().enterInner();
    }

    /**
     * Returns the xml format for the specified type; if none defined 
     * the default object xml format (based on {@link TextFormat}) is returned.
     * @param <T> Type to get a format for
     * @param type Class of the type to get a format for
     * @return XML format
     */
    public static <T> XMLFormat<T> getFormat(Class<? extends T> type) {
        return XMLContext.currentXMLContext().searchFormat(type);
    }

    /**
     * Sets the xml format for the specified type (and its sub-types).
     * @param <T> Type to set a format for
     * @param type Class of the type to set a format for
     * @param format XML Format to set
     */
    public abstract <T> void setFormat(Class<? extends T> type,
            XMLFormat<T> format);

    /**
     * Searches the xml format for the specified type.
     * @param <T> Type to search for a format for
     * @param type Class of the type to search for a format for
     * @return XML Format for the specified class
     */
    protected abstract <T> XMLFormat<T> searchFormat(
            Class<? extends T> type);

    /**
     * Returns the current xml context.
     */
    private static XMLContext currentXMLContext() {
        XMLContext ctx = AbstractContext.current(XMLContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getXMLContext();
    }
}