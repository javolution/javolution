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
import static javolution.internal.osgi.JavolutionActivator.XML_CONTEXT_TRACKER;
import javolution.lang.Configurable;
import javolution.text.TypeFormat;

/**
 * <p> A context for plain xml serialization/deserialization. 
 *     The default xml format for any class is given by the 
 *     {@link javolution.annotation.Format Format} inheritable annotation.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0 December 12, 2012
 */
public abstract class XMLContext extends FormatContext<XMLContext> {

    /**
     * Indicates whether or not static methods will block for an OSGi published
     * implementation this class (default configuration <code>false</code>).
     */
    public static final Configurable<Boolean> WAIT_FOR_SERVICE = new Configurable(false) {

        @Override
        public void configure(CharSequence configuration) {
            setDefaultValue(TypeFormat.parseBoolean(configuration));
        }

    };

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
        XMLContext ctx = AbstractContext.current(XMLContext.class);
        if (ctx != null) return ctx.inner().enterScope();
        return XML_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefaultValue()).inner().enterScope();
    }

    /**
     * Returns the xml format for the specified type or <code>null</code> 
     * if none defined.
     */
    public static <T> XMLFormat<T> getFormat(Class<T> type) {
        XMLContext ctx = AbstractContext.current(XMLContext.class);
        if (ctx != null) {
            ctx = XML_CONTEXT_TRACKER.getService(WAIT_FOR_SERVICE.getDefaultValue());
        }
        return ctx.getFormatInContext(type);
    }

    /**
     * Sets the xml format for the specified type (and its sub-types).
     */
    public abstract <T> void setFormat(Class<T> type, XMLFormat<T> format);

    /**
     * Returns the xml format for the specified type.
     */
    protected abstract <T> XMLFormat<T> getFormatInContext(Class<T> type);

}