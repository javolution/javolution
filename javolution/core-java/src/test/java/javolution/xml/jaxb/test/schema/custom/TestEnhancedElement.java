/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.jaxb.test.schema.custom;

import javolution.xml.jaxb.test.schema.TestElement;

public class TestEnhancedElement extends TestElement {

	private Integer testEnhancedIntElement;

	@Override
	public void setTestIntElement(final Integer value) {
		this.testIntElement = value;
		this.testEnhancedIntElement = value;
	}

	public Integer getTestEnhancedIntElement() {
		return testEnhancedIntElement;
	}

	public void setTestEnhancedIntElement(final Integer testEnhancedIntElement) {
		this.testEnhancedIntElement = testEnhancedIntElement;
	}
}
