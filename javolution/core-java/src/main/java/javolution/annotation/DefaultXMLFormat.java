/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javolution.xml.XMLFormat;

/**
 * <p> Indicates the default xml format of a class (for xml serialization/deserialization). 
 *     The default format is used by the {@link javolution.xml.XMLObjectReader}
 *     and  {@link javolution.xml.XMLObjectWriter} classes. It can be locally overridden 
 *     in the scope of a {@link javolution.xml.XMLContext XMLContext}.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, December 12, 2012
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultXMLFormat {
    
    /**
     * Returns the default xml format of the annotated class.
     */
    @SuppressWarnings("rawtypes")
    Class<? extends XMLFormat> value();    
       
}
