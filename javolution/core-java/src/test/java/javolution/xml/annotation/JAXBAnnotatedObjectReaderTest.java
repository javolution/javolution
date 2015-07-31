/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import javolution.xml.internal.annotation.JAXBAnnotatedObjectReaderImpl;
import javolution.xml.jaxb.test.schema.TestAttributeElement;
import javolution.xml.jaxb.test.schema.TestBoundedWrapperElement;
import javolution.xml.jaxb.test.schema.TestCommonElement;
import javolution.xml.jaxb.test.schema.TestElement;
import javolution.xml.jaxb.test.schema.TestEnumElement;
import javolution.xml.jaxb.test.schema.TestRoot;
import javolution.xml.jaxb.test.schema.TestUnboundedWrapperElement;
import javolution.xml.jaxb.test.schema.TestValidationElement;
import javolution.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

public class JAXBAnnotatedObjectReaderTest {

	private JAXBAnnotatedObjectReader _jaxbObjectReader;

	@Before
	public void init() throws JAXBException{
		_jaxbObjectReader = new JAXBAnnotatedObjectReaderImpl(TestRoot.class);
	}

	@Test
	public void testReadJaxbObjectBasic() throws JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-small.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test1", "Test1", testRoot.getType());
		assertEquals("TestRoot - 1 Element", 1, testRoot.getTestElement().size());
		final TestElement element = testRoot.getTestElement().get(0);
		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 2", Long.valueOf(2L), element.getTestLongElement());
	}

	@Test
	public void testReadJaxbObjectWithSkippedElements() throws JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-skipped-elements.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test1", "Test1", testRoot.getType());
		assertEquals("TestRoot - 1 Element", 1, testRoot.getTestElement().size());
		final TestElement element = testRoot.getTestElement().get(0);
		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertEquals("TestElement - TestStringElement = ABC", "ABC", element.getTestStringElement());
	}

	@Test
	public void testReadJaxbObjectWithAllBasicDatatypes() throws JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-basic-datatypes.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test1", "Test1", testRoot.getType());
		assertEquals("TestRoot - 1 Element", 1, testRoot.getTestElement().size());
		final TestElement element = testRoot.getTestElement().get(0);
		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 999999999999999L", Long.valueOf(999999999999999L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 1.0", Float.valueOf(1.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 83952.1525", Double.valueOf(83952.1525), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = ABC", "ABC", element.getTestStringElement());
	}

	@Test
	public void testReadJaxbObjectWithMoreThanOneOfSameElementWithMixedData() throws JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-more-than-one-of-same-element-with-mixed-data.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test6", "Test6", testRoot.getType());
		assertEquals("TestRoot - 6 Elements", 6, testRoot.getTestElement().size());

		TestElement element = testRoot.getTestElement().get(0);

		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());

		element = testRoot.getTestElement().get(1);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 999999999999999L", Long.valueOf(999999999999999L), element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = ABC", "ABC", element.getTestStringElement());

		element = testRoot.getTestElement().get(2);

		assertEquals("TestElement - TestIntElement = 131", Integer.valueOf(131), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 333333333333333", Long.valueOf(333333333333333L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 5.0", Float.valueOf(5.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 11152.725", Double.valueOf(11152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST", "TEST", element.getTestStringElement());

		element = testRoot.getTestElement().get(3);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 1.0", Float.valueOf(1.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 83952.1525", Double.valueOf(83952.1525), element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());

		element = testRoot.getTestElement().get(4);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = false", Boolean.FALSE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = DEF", "DEF", element.getTestStringElement());

		element = testRoot.getTestElement().get(5);

		assertEquals("TestElement - TestIntElement = 153", Integer.valueOf(153), element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 9.0", Float.valueOf(9.0f), element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());
	}

	@Test
	public void testReadJaxbObjectWithBoundedAndUnboundedNesting() throws XMLStreamException, JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-bounded-unbounded-nesting.xml"));
		
		TestBoundedWrapperElement boundedWrapperElement = testRoot.getTestBoundedWrapperElement();
		assertNotNull("TestBoundedWrapperElement - Not Null", boundedWrapperElement);
		
		TestValidationElement validationElement = boundedWrapperElement.getTestValidationElement();
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(2));
	}
	
	@Test
	public void testReadJaxbObjectWithBoundedElementAndMixedBoundedNesting() throws XMLStreamException, JAXBException {
		//final JAXBContext context = JAXBContext.newInstance(TestRoot.class);
		//final Unmarshaller unmarshaller = context.createUnmarshaller();
		//final TestRoot testRoot = (TestRoot) unmarshaller.unmarshal(this.getClass().getResourceAsStream("/test-bounded-element-with-mixed-bounded-nesting.xml"));
		
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-bounded-element-with-mixed-bounded-nesting.xml"));
		
		TestBoundedWrapperElement boundedWrapperElement = testRoot.getTestBoundedWrapperElement();
		assertNotNull("TestBoundedWrapperElement - Not Null", boundedWrapperElement);
		
		TestValidationElement validationElement = boundedWrapperElement.getTestValidationElement();
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(2));
		
		TestAttributeElement attributeElement = boundedWrapperElement.getTestAttributeElement();
		
		assertEquals("TestAttributeElement - StringAttribute = string3", "string3", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 3", Integer.valueOf(3), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 3", Long.valueOf(3), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 3", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 3.0", Float.valueOf(3.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 3.0", Double.valueOf(3.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 3", Byte.valueOf((byte)3), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 3", Short.valueOf((short)3), attributeElement.getShortAttribute());
		
		Calendar calendar = new GregorianCalendar(); 
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.YEAR, 1999);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		assertEquals("TestAttributeElement - DateAttribute = 1999-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
	}

	@Test
	public void testReadJaxbObjectWithBoundedElementWithNestedAttributeOnly() throws XMLStreamException, JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-bounded-element-with-nested-attribute-only.xml"));
		
		TestBoundedWrapperElement boundedWrapperElement = testRoot.getTestBoundedWrapperElement();
		assertNotNull("TestBoundedWrapperElement - Not Null", boundedWrapperElement);
				
		TestAttributeElement attributeElement = boundedWrapperElement.getTestAttributeElement();
		
		assertEquals("TestAttributeElement - StringAttribute = string3", "string3", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 3", Integer.valueOf(3), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 3", Long.valueOf(3), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 3", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 3.0", Float.valueOf(3.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 3.0", Double.valueOf(3.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 3", Byte.valueOf((byte)3), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 3", Short.valueOf((short)3), attributeElement.getShortAttribute());
		
		Calendar calendar = new GregorianCalendar(); 
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.YEAR, 1999);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		assertEquals("TestAttributeElement - DateAttribute = 1999-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
	}
	
	@Test
	public void testReadJaxbObjectWithLargeNestedMixedObject() throws XMLStreamException, JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-large-nested-mixed-object.xml"));

		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test6", "Test6", testRoot.getType());
		assertEquals("TestRoot - Type Is Test6", "Test9", testRoot.getType2());
		assertEquals("TestRoot - 8 Elements", 8, testRoot.getTestElement().size());

		TestElement element = testRoot.getTestElement().get(0);

		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());

		element = testRoot.getTestElement().get(1);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 999999999999999L", Long.valueOf(999999999999999L), element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = ABC", "ABC", element.getTestStringElement());

		element = testRoot.getTestElement().get(2);

		assertEquals("TestElement - TestIntElement = 131", Integer.valueOf(131), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 333333333333333", Long.valueOf(333333333333333L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 5.0", Float.valueOf(5.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 11152.725", Double.valueOf(11152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST", "TEST", element.getTestStringElement());

		element = testRoot.getTestElement().get(3);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 1.0", Float.valueOf(1.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 83952.1525", Double.valueOf(83952.1525), element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());

		element = testRoot.getTestElement().get(4);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = false", Boolean.FALSE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = DEF", "DEF", element.getTestStringElement());

		element = testRoot.getTestElement().get(5);

		assertEquals("TestElement - TestIntElement = 153", Integer.valueOf(153), element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 9.0", Float.valueOf(9.0f), element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());
		
		element = testRoot.getTestElement().get(6);
		
		Calendar calendar = new GregorianCalendar(); 
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.YEAR, 1988);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		assertEquals("TestElement - TestDateElement = 1988-01-01", calendar.getTime().toString(), element.getTestDateElement().toGregorianCalendar().getTime().toString());
		
		element = testRoot.getTestElement().get(7);
				
		calendar.set(Calendar.YEAR, 1979);
		calendar.set(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 23);
		calendar.set(Calendar.SECOND, 45);
		
		assertEquals("TestElement - TestDateElement = 1979-01-01T01:23:45", calendar.getTime().toString(), element.getTestDateElement().toGregorianCalendar().getTime().toString());
		
		assertEquals("TestRoot - 2 Validation Elements", 2, testRoot.getTestValidationElement().size());
		
		TestValidationElement validationElement = testRoot.getTestValidationElement().get(0);
		assertEquals("TestValidationElement: 2 TestEnumElements", 2, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(1));
		
		validationElement = testRoot.getTestValidationElement().get(1);
		assertEquals("TestValidationElement: 1 TestEnumElements", 1, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(0));

		assertEquals("TestRoot - 4 Attribute Elements", 4, testRoot.getTestAttributeElement().size());
		
		TestAttributeElement attributeElement = testRoot.getTestAttributeElement().get(0);
		
		assertEquals("TestAttributeElement - StringAttribute = string", "string", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 1", Integer.valueOf(1), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 1", Long.valueOf(1), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 1", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 1.0", Float.valueOf(1.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 1.0", Double.valueOf(1.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 1", Byte.valueOf((byte)1), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 1", Short.valueOf((short)1), attributeElement.getShortAttribute());
		
		calendar = new GregorianCalendar(); 
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.YEAR, 1960);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		assertEquals("TestAttributeElement - DateAttribute = 1960-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
		
		attributeElement = testRoot.getTestAttributeElement().get(1);
		
		assertEquals("TestAttributeElement - StringAttribute = string", "string2", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 2", Integer.valueOf(2), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 2", Long.valueOf(2), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 2", Boolean.FALSE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 2.0", Float.valueOf(2.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 2.0", Double.valueOf(2.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 2", Byte.valueOf((byte)2), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 2", Short.valueOf((short)2), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 1963);
		
		assertEquals("TestAttributeElement - DateAttribute = 1963-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());

		attributeElement = testRoot.getTestAttributeElement().get(2);
		
		assertEquals("TestAttributeElement - StringAttribute = string3", "string3", attributeElement.getStringAttribute());
		assertNull("TestAttributeElement - IntAttribute = null", attributeElement.getIntAttribute());
		assertNull("TestAttributeElement - LongAttribute = null", attributeElement.getLongAttribute());
		assertNull("TestAttributeElement - BooleanAttribute = null", attributeElement.isBooleanAttribute());
		assertNull("TestAttributeElement - FloatAttribute = null", attributeElement.getFloatAttribute());
		assertNull("TestAttributeElement - DoubleAttribute = null", attributeElement.getDoubleAttribute());
		assertNull("TestAttributeElement - ByteAttribute = null", attributeElement.getByteAttribute());
		assertNull("TestAttributeElement - ShortAttribute = null", attributeElement.getShortAttribute());
		assertNull("TestAttributeElement - DateAttribute = null", attributeElement.getDateAttribute());
		
		attributeElement = testRoot.getTestAttributeElement().get(3);
		
		assertNull("TestAttributeElement - StringAttribute = null", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 4", Integer.valueOf(4), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 4", Long.valueOf(4), attributeElement.getLongAttribute());
		assertNull("TestAttributeElement - BooleanAttribute = null", attributeElement.isBooleanAttribute());
		assertNull("TestAttributeElement - FloatAttribute = null", attributeElement.getFloatAttribute());
		assertNull("TestAttributeElement - DoubleAttribute = null", attributeElement.getDoubleAttribute());
		assertNull("TestAttributeElement - ByteAttribute = null", attributeElement.getByteAttribute());
		assertNull("TestAttributeElement - ShortAttribute = null", attributeElement.getShortAttribute());
		assertNull("TestAttributeElement - DateAttribute = null", attributeElement.getDateAttribute());
		
		TestBoundedWrapperElement boundedWrapperElement = testRoot.getTestBoundedWrapperElement();
		assertNotNull("TestBoundedWrapperElement - Not Null", boundedWrapperElement);
		
		element = boundedWrapperElement.getTestElement();
		
		assertEquals("TestElement - TestIntElement = 131", Integer.valueOf(131), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 333333333333333", Long.valueOf(333333333333333L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 5.0", Float.valueOf(5.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 11152.725", Double.valueOf(11152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST", "TEST", element.getTestStringElement());
		
		validationElement = boundedWrapperElement.getTestValidationElement();
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(2));
		
		attributeElement = boundedWrapperElement.getTestAttributeElement();
		
		assertEquals("TestAttributeElement - StringAttribute = string3", "string3", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 3", Integer.valueOf(3), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 3", Long.valueOf(3), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 3", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 3.0", Float.valueOf(3.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 3.0", Double.valueOf(3.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 3", Byte.valueOf((byte)3), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 3", Short.valueOf((short)3), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 1999);
		
		assertEquals("TestAttributeElement - DateAttribute = 1999-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
		
		assertEquals("TestRoot - 2 Unbounded Wrapper Elements", 2, testRoot.getTestUnboundedWrapperElement().size());
		
		TestUnboundedWrapperElement unboundedWrapperElement = testRoot.getTestUnboundedWrapperElement().get(0);
		
		assertEquals("TestUnboundedWrapperElement - 1 Test Element", 1, unboundedWrapperElement.getTestElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Validation Element", 1, unboundedWrapperElement.getTestValidationElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Attribute Element", 1, unboundedWrapperElement.getTestAttributeElement().size());
		
		element = unboundedWrapperElement.getTestElement().get(0);
		
		assertEquals("TestElement - TestIntElement = 231", Integer.valueOf(231), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 4444444444444", Long.valueOf(4444444444444L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 50.0", Float.valueOf(50.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 21152.725", Double.valueOf(21152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST2", "TEST2", element.getTestStringElement());
		
		validationElement = unboundedWrapperElement.getTestValidationElement().get(0);
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(2));

		attributeElement = unboundedWrapperElement.getTestAttributeElement().get(0);
		
		assertEquals("TestAttributeElement - StringAttribute = string4", "string4", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 4", Integer.valueOf(4), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 4", Long.valueOf(4), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 4", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 4.0", Float.valueOf(4.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 4.0", Double.valueOf(4.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 4", Byte.valueOf((byte)4), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 4", Short.valueOf((short)4), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 2002);
		
		assertEquals("TestAttributeElement - DateAttribute = 2002-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
		
		unboundedWrapperElement = testRoot.getTestUnboundedWrapperElement().get(1);
		
		assertEquals("TestUnboundedWrapperElement - 1 Test Element", 1, unboundedWrapperElement.getTestElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Validation Element", 1, unboundedWrapperElement.getTestValidationElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Attribute Element", 1, unboundedWrapperElement.getTestAttributeElement().size());
		
		element = unboundedWrapperElement.getTestElement().get(0);
		
		assertEquals("TestElement - TestIntElement = 331", Integer.valueOf(331), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 5555555555555", Long.valueOf(5555555555555L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 60.0", Float.valueOf(60.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 61152.725", Double.valueOf(61152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST3", "TEST3", element.getTestStringElement());
		
		validationElement = unboundedWrapperElement.getTestValidationElement().get(0);
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(2));

		attributeElement = unboundedWrapperElement.getTestAttributeElement().get(0);
		
		assertEquals("TestAttributeElement - StringAttribute = string5", "string5", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 5", Integer.valueOf(5), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 5", Long.valueOf(5), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 5", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 5.0", Float.valueOf(5.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 5.0", Double.valueOf(5.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 5", Byte.valueOf((byte)5), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 5", Short.valueOf((short)5), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 1934);
		
		assertEquals("TestAttributeElement - DateAttribute = 1934-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
	}
	
	@Test
	public void testReadJaxbObjectWithLargeValidNestedMixedObject() throws XMLStreamException, JAXBException {
		_jaxbObjectReader.setValidating(true);
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-large-valid-nested-mixed-object.xml"));

		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test6", "Test6", testRoot.getType());
		assertEquals("TestRoot - Type Is Test6", "Test9", testRoot.getType2());
		assertEquals("TestRoot - 8 Elements", 8, testRoot.getTestElement().size());

		TestElement element = testRoot.getTestElement().get(0);

		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());

		element = testRoot.getTestElement().get(1);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 999999999999999L", Long.valueOf(999999999999999L), element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = ABC", "ABC", element.getTestStringElement());

		element = testRoot.getTestElement().get(2);

		assertEquals("TestElement - TestIntElement = 131", Integer.valueOf(131), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 333333333333333", Long.valueOf(333333333333333L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 5.0", Float.valueOf(5.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 11152.725", Double.valueOf(11152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST", "TEST", element.getTestStringElement());

		element = testRoot.getTestElement().get(3);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 1.0", Float.valueOf(1.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 83952.1525", Double.valueOf(83952.1525), element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());

		element = testRoot.getTestElement().get(4);

		assertNull("TestElement - TestIntElement = Null", element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertNull("TestElement - TestFloatElement = Null", element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = false", Boolean.FALSE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = DEF", "DEF", element.getTestStringElement());

		element = testRoot.getTestElement().get(5);

		assertEquals("TestElement - TestIntElement = 153", Integer.valueOf(153), element.getTestIntElement());
		assertNull("TestElement - TestLongElement = Null", element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 9.0", Float.valueOf(9.0f), element.getTestFloatElement());
		assertNull("TestElement - TestDoubleElement = Null", element.getTestDoubleElement());
		assertNull("TestElement - TestBooleanElement = Null", element.isTestBooleanElement());
		assertNull("TestElement - TestStringElement = Null", element.getTestStringElement());
		
		element = testRoot.getTestElement().get(6);
		
		Calendar calendar = new GregorianCalendar(); 
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.YEAR, 1988);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		assertEquals("TestElement - TestDateElement = 1988-01-01", calendar.getTime().toString(), element.getTestDateElement().toGregorianCalendar().getTime().toString());
		
		element = testRoot.getTestElement().get(7);
				
		calendar.set(Calendar.YEAR, 1979);
		
		assertEquals("TestElement - TestDateElement = 1979-01-01", calendar.getTime().toString(), element.getTestDateElement().toGregorianCalendar().getTime().toString());
		
		assertEquals("TestRoot - 2 Validation Elements", 2, testRoot.getTestValidationElement().size());
		
		TestValidationElement validationElement = testRoot.getTestValidationElement().get(0);
		assertEquals("TestValidationElement: 2 TestEnumElements", 2, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestRequiredAttribute - 1", "1", validationElement.getTestRequiredAttribute());
		assertEquals("TestValidationElement: TestRequiredLongElement - 1", 1L, validationElement.getTestRequiredLongElement());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(1));
		
		validationElement = testRoot.getTestValidationElement().get(1);
		assertEquals("TestValidationElement: 1 TestEnumElements", 1, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestRequiredAttribute - 2", "2", validationElement.getTestRequiredAttribute());
		assertEquals("TestValidationElement: TestRequiredLongElement - 2", 2L, validationElement.getTestRequiredLongElement());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(0));

		assertEquals("TestRoot - 4 Attribute Elements", 4, testRoot.getTestAttributeElement().size());
		
		TestAttributeElement attributeElement = testRoot.getTestAttributeElement().get(0);
		
		assertEquals("TestAttributeElement - StringAttribute = string", "string", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 1", Integer.valueOf(1), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 1", Long.valueOf(1), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 1", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 1.0", Float.valueOf(1.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 1.0", Double.valueOf(1.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 1", Byte.valueOf((byte)1), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 1", Short.valueOf((short)1), attributeElement.getShortAttribute());
		
		calendar = new GregorianCalendar(); 
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.YEAR, 1960);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		assertEquals("TestAttributeElement - DateAttribute = 1960-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
		
		attributeElement = testRoot.getTestAttributeElement().get(1);
		
		assertEquals("TestAttributeElement - StringAttribute = string", "string2", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 2", Integer.valueOf(2), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 2", Long.valueOf(2), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 2", Boolean.FALSE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 2.0", Float.valueOf(2.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 2.0", Double.valueOf(2.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 2", Byte.valueOf((byte)2), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 2", Short.valueOf((short)2), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 1963);
		
		assertEquals("TestAttributeElement - DateAttribute = 1963-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());

		attributeElement = testRoot.getTestAttributeElement().get(2);
		
		assertEquals("TestAttributeElement - StringAttribute = string3", "string3", attributeElement.getStringAttribute());
		assertNull("TestAttributeElement - IntAttribute = null", attributeElement.getIntAttribute());
		assertNull("TestAttributeElement - LongAttribute = null", attributeElement.getLongAttribute());
		assertNull("TestAttributeElement - BooleanAttribute = null", attributeElement.isBooleanAttribute());
		assertNull("TestAttributeElement - FloatAttribute = null", attributeElement.getFloatAttribute());
		assertNull("TestAttributeElement - DoubleAttribute = null", attributeElement.getDoubleAttribute());
		assertNull("TestAttributeElement - ByteAttribute = null", attributeElement.getByteAttribute());
		assertNull("TestAttributeElement - ShortAttribute = null", attributeElement.getShortAttribute());
		assertNull("TestAttributeElement - DateAttribute = null", attributeElement.getDateAttribute());
		
		attributeElement = testRoot.getTestAttributeElement().get(3);
		
		assertEquals("TestAttributeElement - StringAttribute = string4", "string4", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 4", Integer.valueOf(4), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 4", Long.valueOf(4), attributeElement.getLongAttribute());
		assertNull("TestAttributeElement - BooleanAttribute = null", attributeElement.isBooleanAttribute());
		assertNull("TestAttributeElement - FloatAttribute = null", attributeElement.getFloatAttribute());
		assertNull("TestAttributeElement - DoubleAttribute = null", attributeElement.getDoubleAttribute());
		assertNull("TestAttributeElement - ByteAttribute = null", attributeElement.getByteAttribute());
		assertNull("TestAttributeElement - ShortAttribute = null", attributeElement.getShortAttribute());
		assertNull("TestAttributeElement - DateAttribute = null", attributeElement.getDateAttribute());
		
		TestBoundedWrapperElement boundedWrapperElement = testRoot.getTestBoundedWrapperElement();
		assertNotNull("TestBoundedWrapperElement - Not Null", boundedWrapperElement);
		
		element = boundedWrapperElement.getTestElement();
		
		assertEquals("TestElement - TestIntElement = 131", Integer.valueOf(131), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 333333333333333", Long.valueOf(333333333333333L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 5.0", Float.valueOf(5.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 11152.725", Double.valueOf(11152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST", "TEST", element.getTestStringElement());
		
		validationElement = boundedWrapperElement.getTestValidationElement();
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestRequiredAttribute - 3", "3", validationElement.getTestRequiredAttribute());
		assertEquals("TestValidationElement: TestRequiredLongElement - 3", 3L, validationElement.getTestRequiredLongElement());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(2));
		
		attributeElement = boundedWrapperElement.getTestAttributeElement();
		
		assertEquals("TestAttributeElement - StringAttribute = string3", "string3", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 3", Integer.valueOf(3), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 3", Long.valueOf(3), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 3", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 3.0", Float.valueOf(3.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 3.0", Double.valueOf(3.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 3", Byte.valueOf((byte)3), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 3", Short.valueOf((short)3), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 1999);
		
		assertEquals("TestAttributeElement - DateAttribute = 1999-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
		
		assertEquals("TestRoot - 2 Unbounded Wrapper Elements", 2, testRoot.getTestUnboundedWrapperElement().size());
		
		TestUnboundedWrapperElement unboundedWrapperElement = testRoot.getTestUnboundedWrapperElement().get(0);
		
		assertEquals("TestUnboundedWrapperElement - 1 Test Element", 1, unboundedWrapperElement.getTestElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Validation Element", 1, unboundedWrapperElement.getTestValidationElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Attribute Element", 1, unboundedWrapperElement.getTestAttributeElement().size());
		
		element = unboundedWrapperElement.getTestElement().get(0);
		
		assertEquals("TestElement - TestIntElement = 231", Integer.valueOf(231), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 4444444444444", Long.valueOf(4444444444444L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 50.0", Float.valueOf(50.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 21152.725", Double.valueOf(21152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST2", "TEST2", element.getTestStringElement());
		
		validationElement = unboundedWrapperElement.getTestValidationElement().get(0);
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestRequiredAttribute - 4", "4", validationElement.getTestRequiredAttribute());
		assertEquals("TestValidationElement: TestRequiredLongElement - 4", 4L, validationElement.getTestRequiredLongElement());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(2));

		attributeElement = unboundedWrapperElement.getTestAttributeElement().get(0);
		
		assertEquals("TestAttributeElement - StringAttribute = string4", "string4", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 4", Integer.valueOf(4), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 4", Long.valueOf(4), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 4", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 4.0", Float.valueOf(4.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 4.0", Double.valueOf(4.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 4", Byte.valueOf((byte)4), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 4", Short.valueOf((short)4), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 2002);
		
		assertEquals("TestAttributeElement - DateAttribute = 2002-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
		
		unboundedWrapperElement = testRoot.getTestUnboundedWrapperElement().get(1);
		
		assertEquals("TestUnboundedWrapperElement - 1 Test Element", 1, unboundedWrapperElement.getTestElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Validation Element", 1, unboundedWrapperElement.getTestValidationElement().size());
		assertEquals("TestUnboundedWrapperElement - 1 Test Attribute Element", 1, unboundedWrapperElement.getTestAttributeElement().size());
		
		element = unboundedWrapperElement.getTestElement().get(0);
		
		assertEquals("TestElement - TestIntElement = 331", Integer.valueOf(331), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 5555555555555", Long.valueOf(5555555555555L), element.getTestLongElement());
		assertEquals("TestElement - TestFloatElement = 60.0", Float.valueOf(60.0f), element.getTestFloatElement());
		assertEquals("TestElement - TestDoubleElement = 61152.725", Double.valueOf(61152.725), element.getTestDoubleElement());
		assertEquals("TestElement - TestBooleanElement = true", Boolean.TRUE, element.isTestBooleanElement());
		assertEquals("TestElement - TestStringElement = TEST3", "TEST3", element.getTestStringElement());
		
		validationElement = unboundedWrapperElement.getTestValidationElement().get(0);
		
		assertEquals("TestValidationElement: 3 TestEnumElements", 3, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestRequiredAttribute - 5", "5", validationElement.getTestRequiredAttribute());
		assertEquals("TestValidationElement: TestRequiredLongElement - 5", 5L, validationElement.getTestRequiredLongElement());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(1));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(2));

		attributeElement = unboundedWrapperElement.getTestAttributeElement().get(0);
		
		assertEquals("TestAttributeElement - StringAttribute = string5", "string5", attributeElement.getStringAttribute());
		assertEquals("TestAttributeElement - IntAttribute = 5", Integer.valueOf(5), attributeElement.getIntAttribute());
		assertEquals("TestAttributeElement - LongAttribute = 5", Long.valueOf(5), attributeElement.getLongAttribute());
		assertEquals("TestAttributeElement - BooleanAttribute = 5", Boolean.TRUE, attributeElement.isBooleanAttribute());
		assertEquals("TestAttributeElement - FloatAttribute = 5.0", Float.valueOf(5.0f), attributeElement.getFloatAttribute());
		assertEquals("TestAttributeElement - DoubleAttribute = 5.0", Double.valueOf(5.0), attributeElement.getDoubleAttribute());
		assertEquals("TestAttributeElement - ByteAttribute = 5", Byte.valueOf((byte)5), attributeElement.getByteAttribute());
		assertEquals("TestAttributeElement - ShortAttribute = 5", Short.valueOf((short)5), attributeElement.getShortAttribute());
		
		calendar.set(Calendar.YEAR, 1934);
		
		assertEquals("TestAttributeElement - DateAttribute = 1934-01-01", calendar.getTime().toString(), attributeElement.getDateAttribute().toGregorianCalendar().getTime().toString());
	}

	
	@Test
	public void testReadJaxbObjectWithEnumAndDate() throws JAXBException {
		//final JAXBContext context = JAXBContext.newInstance(TestRoot.class);
		//final Unmarshaller unmarshaller = context.createUnmarshaller();
		//final TestRoot testRoot = (TestRoot) unmarshaller.unmarshal(this.getClass().getResourceAsStream("/test-with-enum-and-date.xml"));
		
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-enum-and-date.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertNull("Type Is Null!", testRoot.getType());
		
		assertEquals("TestRoot - 2 Elements", 2, testRoot.getTestElement().size());
		TestElement element = testRoot.getTestElement().get(0);
		
		Calendar calendar = new GregorianCalendar(); 
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.YEAR, 1988);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		assertEquals("TestElement - TestDateElement = 1988-01-01", calendar.getTime().toString(), element.getTestDateElement().toGregorianCalendar().getTime().toString());
		
		element = testRoot.getTestElement().get(1);
				
		calendar.set(Calendar.YEAR, 1979);
		calendar.set(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 23);
		calendar.set(Calendar.SECOND, 45);
		
		assertEquals("TestElement - TestDateElement = 1979-01-01T01:23:45", calendar.getTime().toString(), element.getTestDateElement().toGregorianCalendar().getTime().toString());
		
		assertEquals("TestRoot - 2 Validation Elements", 2, testRoot.getTestValidationElement().size());
		
		TestValidationElement validationElement = testRoot.getTestValidationElement().get(0);
		assertEquals("TestValidationElement: 2 TestEnumElements", 2, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_ONE", TestEnumElement.TEST_ENUM_ONE, 
				validationElement.getTestUnboundedEnumElement().get(0));
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_THREE", TestEnumElement.TEST_ENUM_THREE, 
				validationElement.getTestUnboundedEnumElement().get(1));
		
		validationElement = testRoot.getTestValidationElement().get(1);
		assertEquals("TestValidationElement: 1 TestEnumElements", 1, validationElement.getTestUnboundedEnumElement().size());
		assertEquals("TestValidationElement: TestEnumElement - TEST_ENUM_TWO", TestEnumElement.TEST_ENUM_TWO, 
				validationElement.getTestUnboundedEnumElement().get(0));
		
	}
	
	@Test
	public void testReadJaxbObjectWithUnmappedAttributeAndValidationOff() throws JAXBException{
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-unmapped-attribute.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test1", "Test1", testRoot.getType());
		assertEquals("TestRoot - 1 Element", 1, testRoot.getTestElement().size());
		final TestElement element = testRoot.getTestElement().get(0);
		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 2", Long.valueOf(2L), element.getTestLongElement());
	}
	
	@Test(expected=ValidationException.class)
	public void testReadJaxbObjectWithUnmappedAttributeAndValidationOn() throws JAXBException{
		_jaxbObjectReader.setValidating(true);
		_jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-unmapped-attribute.xml"));
	}
	
	@Test(expected=ValidationException.class)
	public void testReadJaxbObjectWithUnmappedElementAndValidationOn() throws JAXBException{
		_jaxbObjectReader.setValidating(true);
		_jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-unmapped-element.xml"));
	}
	
	@Test
	public void testReadJaxbObjectWithUnmappedElementAndValidationOff() throws JAXBException{
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-unmapped-element.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test1", "Test1", testRoot.getType());
		assertEquals("TestRoot - 1 Element", 1, testRoot.getTestElement().size());
		final TestElement element = testRoot.getTestElement().get(0);
		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 2", Long.valueOf(2L), element.getTestLongElement());
	}
	
	@Test
	public void testReadJaxbObjectWithMissingRequiredElementAndValidationOff() throws JAXBException{
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-missing-required-element.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - 1 Validation Element", 1, testRoot.getTestValidationElement().size());
		TestValidationElement testValidationElement = testRoot.getTestValidationElement().get(0);
		assertEquals("TestRoot - 1 Test Enum Element", 1, testValidationElement.getTestUnboundedEnumElement().size());
		assertTrue("TestValidationElement Contains TEST_ENUM_ONE", testValidationElement.getTestUnboundedEnumElement().contains(TestEnumElement.TEST_ENUM_ONE));
	}
	
	@Test(expected=ValidationException.class)
	public void testReadJaxbObjectWithMissingRequiredElementAndValidationOn() throws JAXBException{
		_jaxbObjectReader.setValidating(true);
		_jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-missing-required-element.xml"));
	}
	
	@Test
	public void testReadJaxbObjectWithMissingRequiredAttributeAndValidationOff() throws JAXBException{
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-missing-required-attribute.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - 1 Validation Element", 1, testRoot.getTestValidationElement().size());
		TestValidationElement testValidationElement = testRoot.getTestValidationElement().get(0);
		assertEquals("TestRequiredLongElement Is 5", 5L, testValidationElement.getTestRequiredLongElement());
	}

	@Test(expected=ValidationException.class)
	public void testReadJaxbObjectWithMissingRequiredAttributeAndValidationOn() throws JAXBException{
		_jaxbObjectReader.setValidating(true);
		_jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-missing-required-attribute.xml"));
	}
	
	@Test
	public void testReadJaxbObjectWithOutOfOrderElementAndValidationOff() throws JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-out-of-order-element.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test1", "Test1", testRoot.getType());
		assertEquals("TestRoot - 1 Element", 1, testRoot.getTestElement().size());
		final TestElement element = testRoot.getTestElement().get(0);
		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 2", Long.valueOf(2L), element.getTestLongElement());
	}
	
	@Test(expected=ValidationException.class)
	public void testReadJaxbObjectWithOutOfOrderElementAndValidationOn() throws JAXBException{
		_jaxbObjectReader.setValidating(true);
		_jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-with-out-of-order-element.xml"));
	}
	
	@Test
	public void testReadJaxbObjectBasicWithValidationOn() throws JAXBException {
		_jaxbObjectReader.setValidating(true);
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-small.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - Type Is Test1", "Test1", testRoot.getType());
		assertEquals("TestRoot - 1 Element", 1, testRoot.getTestElement().size());
		final TestElement element = testRoot.getTestElement().get(0);
		assertEquals("TestElement - TestIntElement = 1", Integer.valueOf(1), element.getTestIntElement());
		assertEquals("TestElement - TestLongElement = 2", Long.valueOf(2L), element.getTestLongElement());
	}
	
	@Test
	public void testReadJaxbObjectWithCommonElement() throws JAXBException {
		final TestRoot testRoot = _jaxbObjectReader.read(this.getClass().getResourceAsStream("/test-common-element.xml"));
		assertNotNull("TestRoot Is Not Null!", testRoot);
		assertEquals("TestRoot - 1 Common Element", 1, testRoot.getTestCommonElement().size());
		final TestCommonElement element = testRoot.getTestCommonElement().get(0);
		assertEquals("TestCommonElement - TestCommonStringElement = common", "common", element.getTestCommonStringElement());
	}
}
