/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javolution.context.LogContext;
import javolution.text.CharArray;
import javolution.text.TextBuilder;
import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.FastMap;
import javolution.xml.DefaultXMLFormat;
import javolution.xml.XMLContext;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * Holds the default implementation of XMLContext.
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
@SuppressWarnings("rawtypes")
public final class XMLContextImpl extends XMLContext {

    // Holds class->format mapping. 
    private final FastMap<Class<?>, XMLFormat<?>> classToFormat = new FastMap<Class<?>, XMLFormat<?>>()
            .shared();

    // Holds parent (null if root).
    private final XMLContextImpl parent;

    /** Default constructor for root */
    public XMLContextImpl() {
        parent = null;
    }

    /** Inner constructor */
    public XMLContextImpl(XMLContextImpl parent) {
        this.parent = parent;
    }

    @Override
    protected XMLContext inner() {
        return new XMLContextImpl(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> XMLFormat<T> searchFormat(Class<? extends T> type) {
        XMLFormat<T> format = (XMLFormat<T>) classToFormat.get(type);
        if (format != null) return format;
        if (parent != null) { // Searches parent.
            format = parent.searchFormat(type);
            classToFormat.put(type, format);
            return format;
        }
        // Root context (search inheritable annotations).
        DefaultXMLFormat annotation = type
                .getAnnotation(DefaultXMLFormat.class);
        if (annotation != null) { // Found it.
            try {
                format = (XMLFormat<T>) annotation.value().newInstance();
                classToFormat.put(type, format);
                return format;
            } catch (Throwable error) {
                LogContext.warning(error);
            }
        }
        if (Collection.class.isAssignableFrom(type)) {
            classToFormat.put(type, COLLECTION_XML);
            return (XMLFormat<T>) COLLECTION_XML;
        } else if (Map.class.isAssignableFrom(type)) {
            classToFormat.put(type, MAP_XML);
            return (XMLFormat<T>) MAP_XML;
        } else {
            classToFormat.put(type, OBJECT_XML);
            return (XMLFormat<T>) OBJECT_XML;
        }
    }

    @Override
    public <T> void setFormat(Class<? extends T> type, XMLFormat<T> format) {
        classToFormat.put(type, format);
    }

    /////////////////////////
    // PREDEFINED FORMATS  //
    /////////////////////////

    /**
     * THe default Object XML representation consists of the text representation 
     * of the object as a "value" attribute.
     */
    private static final XMLFormat<Object> OBJECT_XML = new XMLFormat<Object>() {

        @Override
        public boolean isReferenceable() {
            return false; // Always by value.
        }

        @Override
        public Object newInstance(Class<?> cls, XMLFormat.InputElement xml)
                throws XMLStreamException {
            TextFormat<?> format = TextContext.getFormat(cls);
            CharArray value = xml.getAttribute("value");
            if (value == null) throw new XMLStreamException(
                    "Missing value attribute to parse " + cls + " instances.");
            return format.parse(value);
        }

        @Override
        public void read(XMLFormat.InputElement xml, Object obj)
                throws XMLStreamException {
            // Do nothing.
        }

        @Override
        public void write(Object obj, XMLFormat.OutputElement xml)
                throws XMLStreamException {
            TextBuilder tmp = new TextBuilder();
            TextFormat<Object> tf = TextContext.getFormat(obj.getClass());
            tf.format(obj, tmp);
            xml.setAttribute("value", tmp);
        }

    };

    /**
     * The default XML representation for {@link java.util.Collection}
     * consists of nested XML elements one for each element of the collection. 
     * The elements' order is defined by the collection iterator order. 
     * Collections are deserialized using their default constructor.
     */
    private static final XMLFormat<Collection> COLLECTION_XML = new XMLFormat<Collection>() {

        @SuppressWarnings("unchecked")
        @Override
        public void read(XMLFormat.InputElement xml, Collection collection)
                throws XMLStreamException {
            while (xml.hasNext()) {
                collection.add(xml.getNext());
            }
        }

        @Override
        public void write(Collection collection, XMLFormat.OutputElement xml)
                throws XMLStreamException {
            for (Iterator i = collection.iterator(); i.hasNext();) {
                xml.add(i.next());
            }
        }
    };

    /**
     * The default XML representation for {@link java.util.Map} consists of 
     * key/value pair as nested XML elements. For example:[code]
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
    private static final XMLFormat<Map> MAP_XML = new XMLFormat<Map>() {

        @SuppressWarnings("unchecked")
        @Override
        public void read(XMLFormat.InputElement xml, Map map)
                throws XMLStreamException {
            while (xml.hasNext()) {
                Object key = xml.get("Key");
                Object value = xml.get("Value");
                map.put(key, value);
            }
        }

        @Override
        public void write(Map map, XMLFormat.OutputElement xml)
                throws XMLStreamException {
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                xml.add(entry.getKey(), "Key");
                xml.add(entry.getValue(), "Value");
            }
        }
    };
}
