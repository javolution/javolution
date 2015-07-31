/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.sax;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javolution.xml.jaxb.test.schema.TestElement;
import javolution.xml.jaxb.test.schema.TestRoot;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SAX2ReaderImplTest extends org.xml.sax.helpers.DefaultHandler {

	private static final String TEST_INT_ELEMENT = "testIntElement";
	private static final String TEST_LONG_ELEMENT = "testLongElement";
	private static final String TEST_ELEMENT = "testElement";
	private static final String TEST_ROOT = "testRoot";

	private String tempValue;
	private List<TestRoot> testRoots;
	private TestElement testElement;
	private TestRoot testRoot;
	private SAX2ReaderImpl _xmlReaderImpl;

	@Before
	public void init() {
		testRoots = new ArrayList<TestRoot>();

		_xmlReaderImpl = new SAX2ReaderImpl();
		_xmlReaderImpl.setContentHandler(this);
		_xmlReaderImpl.setErrorHandler(this);
	}

	@Test
	public void testParseFromInputSourceWith() throws IOException, SAXException{
		_xmlReaderImpl.parse(new InputSource(this.getClass().getResourceAsStream("/test-stax.xml")));
		validate();
	}

	@Override
	public void characters(final char ch[], final int start, final int length)
			throws SAXException {
		tempValue = new String(ch, start, length);
	}

	@Override
	public void endElement(final String namespaceURI, final String localName,
			final String qName) throws SAXException {
		if(TEST_INT_ELEMENT.equals(qName)) {
			testElement.setTestIntElement(Integer.valueOf(tempValue));
		}
		else if(TEST_LONG_ELEMENT.equals(qName)) {
			testElement.setTestLongElement(Long.valueOf(tempValue));
		}
		else if(TEST_ELEMENT.equals(qName)) {
			testRoot.getTestElement().add(testElement);
		}
		else if(TEST_ROOT.equals(qName)) {
			testRoots.add(testRoot);
		}
	}

	public List<TestRoot> getTestRoots() {
		return testRoots;
	}

	@Override
	public void startElement(final String namespaceURI, final String localName,
			final String qName, final org.xml.sax.Attributes atts) throws SAXException {
		if(TEST_ELEMENT.equals(qName)) {
			testElement = new TestElement();
		}
		else if(TEST_ROOT.equals(qName)) {
			testRoot = new TestRoot();
			testRoot.setType(atts.getValue("type"));
		}
	}

	private void validate() {
		assertEquals("TestRoots Size == 1", 1, testRoots.size());

		final TestRoot testRoot = testRoots.get(0);
		final List<TestElement> elements = testRoot.getTestElement();
		assertEquals("Elements Size == 1000", 1000, elements.size());
		
		for(int j = 0; j < elements.size(); j++) {
			final TestElement element = elements.get(j);
			assertEquals("TestIntElement", Integer.valueOf(1), element.getTestIntElement());
			assertEquals("TestLongElement", Long.valueOf(2), element.getTestLongElement());
		}
	}

}
