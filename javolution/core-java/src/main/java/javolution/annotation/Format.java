/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.annotation;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javolution.text.Cursor;
import javolution.text.TextFormat;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

/**
 * <p> Defines the annotation holding the default 
 *     serializer/deserializer for a class.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Format {
    
    /**
     * Returns the default text format implementation class.
     * @return the text format implementation.
     */
    Class<? extends TextFormat> text() default UnsupportedTextFormat.class;    

    /**
     * Returns the default text format implementation class.
     * @return the text format implementation.
     */
    Class<? extends XMLFormat> xml() default UnsupportedXMLFormat.class;  ;
    
   
    /**
     * Defines the class when text formatting is not supported.  
     */
    public static final class UnsupportedTextFormat extends TextFormat {
        @Override
        public Object parse(CharSequence csq, Cursor cursor) throws IllegalArgumentException {
            throw new UnsupportedOperationException("TextFormat Unknown.");
        }

        @Override
        public Appendable format(Object obj, Appendable dest) throws IOException {
            throw new UnsupportedOperationException("TextFormat Unknown.");
        }        
    }
    
    /**
     * Defines the class when text formatting is not supported.  
     */
    public static final class UnsupportedXMLFormat extends XMLFormat {

        @Override
        public void write(Object obj, OutputElement xml) throws XMLStreamException {
            throw new UnsupportedOperationException("XML Format Unkown.");
        }

        @Override
        public void read(InputElement xml, Object obj) throws XMLStreamException {
            throw new UnsupportedOperationException("XML Format Unknown.");
        }
        
    }
}
