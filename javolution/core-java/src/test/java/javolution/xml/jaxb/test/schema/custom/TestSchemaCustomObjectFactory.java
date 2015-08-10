/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.jaxb.test.schema.custom;

import javax.xml.bind.annotation.XmlRegistry;

import javolution.xml.jaxb.test.schema.ObjectFactory;
import javolution.xml.jaxb.test.schema.TestElement;

@XmlRegistry
public class TestSchemaCustomObjectFactory extends ObjectFactory {

	@Override
	public TestElement createTestElement() {
		return new TestEnhancedElement();
	}

}
