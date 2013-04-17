/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javolution.annotation.Format;
import javolution.util.FastMap;
import javolution.xml.XMLContext;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * Holds the default implementation of XMLContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@SuppressWarnings("rawtypes")
public final class XMLContextImpl extends XMLContext {

    private final FastMap<Class<?>, XMLFormat<?>> formats = new FastMap<Class<?>, XMLFormat<?>>();

    @Override
    protected XMLContext inner() {
        XMLContextImpl ctx = new XMLContextImpl();
        ctx.formats.putAll(formats);
        return ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> XMLFormat<T> getFormatInContext(Class<? extends T> type) {
        XMLFormat xml = formats.get(type);
        if (xml != null)
            return xml;
        Format format = type.getAnnotation(Format.class);
        if ((format != null) && (format.xml() != XMLFormat.Default.class)) {
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
        // Check predefined format as last resource.
        if (Map.class.isAssignableFrom(type))
            return MAP_XML;
        if (Collection.class.isAssignableFrom(type))
            return COLLECTION_XML;
        return OBJECT_XML;
    }

    @Override
    public <T> void setFormat(Class<? extends T> type, XMLFormat<T> format) {
        formats.put(type, format);
    }

    /////////////////////////
    // PREDEFINED FORMATS  //
    /////////////////////////
    /**
     * Holds the static XML format for <code>java.lang.Object.class</code> instances.
     * The XML representation consists of the text representation of the object
     * as a "value" attribute.
     */
    private static final XMLFormat OBJECT_XML = new XMLFormat.Default();

    /**
     * Holds the default XML representation for <code>java.util.Collection</code>
     * instances. This representation consists of nested XML elements one for
     * each element of the collection. The elements' order is defined by
     * the collection iterator order. Collections are deserialized using their
     * default constructor.
     */
    private static final XMLFormat COLLECTION_XML = new XMLFormat() {

        @SuppressWarnings("unchecked")
        public void read(XMLFormat.InputElement xml, Object obj)
                throws XMLStreamException {
            Collection collection = (Collection) obj;
            while (xml.hasNext()) {
                collection.add(xml.getNext());
            }
        }

        public void write(Object obj, XMLFormat.OutputElement xml)
                throws XMLStreamException {
            Collection collection = (Collection) obj;
            for (Iterator i = collection.iterator(); i.hasNext();) {
                xml.add(i.next());
            }
        }

    };

    /**
     * Holds the default XML representation for <code>java.util.Map</code>
     * instances. This representation consists of key/value pair as nested
     * XML elements. For example:[code]
     * <javolution.util.FastMap>
     *     <Key class="java.lang.String" value="ONE"/>
     *     <Value class="java.lang.Integer" value="1"/>
     *     <Key class="java.lang.String" value="TWO"/>
     *     <Value class="java.lang.Integer" value="2"/>
     *     <Key class="java.lang.String" value="THREE"/>
     *     <Value class="java.lang.Integer" value="3"/>
     * </javolution.util.FastMap>[/code]
     *
     * The elements' order is defined by the map's entries iterator order.
     * Maps are deserialized using their default constructor.
     */
    private static final XMLFormat MAP_XML = new XMLFormat() {

        @SuppressWarnings("unchecked")
        public void read(XMLFormat.InputElement xml, Object obj)
                throws XMLStreamException {
            final Map map = (Map) obj;
            while (xml.hasNext()) {
                Object key = xml.get("Key");
                Object value = xml.get("Value");
                map.put(key, value);
            }
        }

        public void write(Object obj, XMLFormat.OutputElement xml)
                throws XMLStreamException {
            final Map map = (Map) obj;
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.add(entry.getKey(), "Key");
                xml.add(entry.getValue(), "Value");
            }
        }

    };

}
