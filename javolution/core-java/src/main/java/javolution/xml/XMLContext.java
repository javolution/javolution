/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml;

import static javolution.internal.osgi.JavolutionActivator.XML_CONTEXT_TRACKER;
import javolution.context.AbstractContext;
import javolution.context.FormatContext;
import javolution.internal.xml.XMLContextImpl;
import javolution.lang.Configurable;
import javolution.text.TextFormat;

/**
 * <p> A context for xml serialization/deserialization. 
 *     The default xml format for any class is given by the 
 *     {@link javolution.annotation.Format Format} inheritable annotation.</p>
 * 
 * <p> A default xml format exists for the following predefined types:
 *     <code><ul>
 *       <li>java.lang.Object (value attribute parsed/formatted using {@link TextFormat})</li>
 *       <li>java.util.Collection</li>
 *       <li>java.util.Map</li>
 *    </ul></code></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class XMLContext extends FormatContext<XMLContext> {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE 
    = new Configurable<Boolean>(false);
    
    /**
     * Default constructor.
     */
    protected XMLContext() {
    }

    /**
     * Enters a new xml context instance.
     * 
     * @return the new xml context implementation entered.
     */
    public static XMLContext enter() {
        return XMLContext.current().inner().enterScope();
    }

    /**
     * Returns the xml format for the specified type; if none defined 
     * the default object xml format (based on {@link TextFormat}) is returned.
     */
    public static <T> XMLFormat<T> getFormat(Class<? extends T> type) {
        return XMLContext.current().getFormatInContext(type);
    }

    /**
     * Sets the xml format for the specified type (and its sub-types).
     */
    public abstract <T> void setFormat(Class<? extends T> type, XMLFormat<T> format);

    /**
     * Returns the xml format for the specified type.
     */
    protected abstract <T> XMLFormat<T> getFormatInContext(Class<? extends T> type);

    /**
     * Returns the current xml context.
     */
    protected static XMLContext current() {
        XMLContext ctx = AbstractContext.current(XMLContext.class);
        if (ctx != null) return ctx;
        return XML_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.get(), DEFAULT);
    }
    
    private static final XMLContextImpl DEFAULT = new XMLContextImpl();    
}