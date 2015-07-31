/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.stream;

import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javolution.text.CharArray;
import javolution.xml.internal.stream.XMLStreamReaderImpl;
import javolution.xml.jaxb.test.schema.TestElement;
import javolution.xml.jaxb.test.schema.TestRoot;

import org.junit.Before;
import org.junit.Test;

public class XMLStreamReaderImplTest {

	private static final CharArray TEST_INT_ELEMENT = new CharArray("testIntElement");
	private static final CharArray TEST_LONG_ELEMENT = new CharArray("testLongElement");
	private static final CharArray TEST_ELEMENT = new CharArray("testElement");
	private static final CharArray TEST_ROOT = new CharArray("testRoot");

	private CharArray _tempValue;
	private List<TestRoot> _testRoots;
	private TestElement _testElement;
	private TestRoot _testRoot;
	
	private XMLStreamReaderImpl _xmlStreamReaderImpl;

	@Before
	public void init(){
		_testRoots = new ArrayList<TestRoot>();
		_xmlStreamReaderImpl = new XMLStreamReaderImpl();
	}

	@Test
	public void testReadXmlWithInputStreamAndLargeXML() throws XMLStreamException{
		_xmlStreamReaderImpl.setInput(this.getClass().getResourceAsStream("/test-stax.xml"));
		readStream();
		validate();
	}
	
	@Test
	public void testReadXmlWithReaderAndLargeXML() throws XMLStreamException{
		_xmlStreamReaderImpl.setInput(new InputStreamReader(this.getClass().getResourceAsStream("/test-stax.xml")));
		readStream();
		validate();
	}
	
	private void readStream() throws XMLStreamException{
		while(_xmlStreamReaderImpl.hasNext()){
			int event = _xmlStreamReaderImpl.next();
			
			CharArray localName;

			switch(event){
			
			case XMLStreamConstants.START_ELEMENT:
				localName = _xmlStreamReaderImpl.getLocalName();
				
				if(TEST_ELEMENT.equals(localName)) {
					_testElement = new TestElement();
				}
				else if(TEST_ROOT.equals(localName)) {
					_testRoot = new TestRoot();
					final CharArray type = _xmlStreamReaderImpl.getAttributeValue(null, "type");
					_testRoot.setType(type.toString());
				}
				
				break;

			case XMLStreamConstants.CHARACTERS:
				_tempValue = _xmlStreamReaderImpl.getText();
				break;

			case XMLStreamConstants.END_ELEMENT:
				localName = _xmlStreamReaderImpl.getLocalName();
				
				if(TEST_INT_ELEMENT.equals(localName)) {
					_testElement.setTestIntElement(_tempValue.toInt());
				}
				else if(TEST_LONG_ELEMENT.equals(localName)) {
					_testElement.setTestLongElement(_tempValue.toLong());
				}
				else if(TEST_ELEMENT.equals(localName)) {
					_testRoot.getTestElement().add(_testElement);
				}
				else if(TEST_ROOT.equals(localName)) {
					_testRoots.add(_testRoot);
				}
				
				break;
			}
		}
	}
	
	private void validate() {
		assertEquals("TestRoots Size == 1", 1, _testRoots.size());

		final TestRoot testRoot = _testRoots.get(0);
		final List<TestElement> elements = testRoot.getTestElement();
		assertEquals("Elements Size == 1000", 1000, elements.size());
		
		for(int i = 0; i < elements.size(); i++) {
			final TestElement element = elements.get(i);
			assertEquals("TestIntElement", Integer.valueOf(1), element.getTestIntElement());
			assertEquals("TestLongElement", Long.valueOf(2), element.getTestLongElement());
		}
	}
}