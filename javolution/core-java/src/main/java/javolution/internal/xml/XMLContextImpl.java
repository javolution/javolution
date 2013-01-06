/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.xml;

import javolution.annotation.Format;
import javolution.util.FastMap;
import javolution.xml.XMLContext;
import javolution.xml.XMLFormat;

/**
 * Holds the default implementation of XMLContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
public final class XMLContextImpl extends XMLContext {

    private final FastMap<Class, XMLFormat> formats = new FastMap();

    @Override
    protected XMLContext inner() {
        XMLContextImpl ctx = new XMLContextImpl();
        ctx.formats.putAll(formats);
        return ctx;
    }

    @Override
    protected <T> XMLFormat<T> getFormatInContext(Class<T> type) {
        XMLFormat xml = formats.get(type);
        if (xml != null) return xml;
        Format format = type.getAnnotation(Format.class);
        if ((format == null) || (format.xml() == Format.UnsupportedXMLFormat.class))
            return null;
        Class<? extends XMLFormat> formatClass = format.xml();
        try {
            xml = formatClass.newInstance();
            synchronized (formats) { // Required since possible concurrent use 
                // (getFormatInContext is not a configuration method).
                formats.put(type, xml);
            }
            return xml;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T> void setFormat(Class<T> type, XMLFormat<T> format) {
        formats.put(type, format);
    }

}
