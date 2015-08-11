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

import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import javolution.xml.internal.annotation.JAXBAnnotatedObjectWriterImpl;
import javolution.xml.jaxb.test.schema.TestAttributeElement;
import javolution.xml.jaxb.test.schema.TestBoundedWrapperElement;
import javolution.xml.jaxb.test.schema.TestElement;
import javolution.xml.jaxb.test.schema.TestEnumElement;
import javolution.xml.jaxb.test.schema.TestRoot;
import javolution.xml.jaxb.test.schema.TestUnboundedWrapperElement;
import javolution.xml.jaxb.test.schema.TestValidationElement;

import org.junit.Before;
import org.junit.Test;

public class JAXBAnnotatedObjectWriterTest {

	private static final String EXPECTED_LARGE_NESTED_MIXED = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><testRoot type=\"Test6\" type2=\"Test9\" xmlns=\"http://javolution.org/xml/schema/javolution\"><testElement><testIntElement>1</testIntElement><testBooleanElement>true</testBooleanElement></testElement><testElement><testLongElement>999999999999999</testLongElement><testStringElement>ABC</testStringElement></testElement><testElement><testIntElement>131</testIntElement><testLongElement>333333333333333</testLongElement><testBooleanElement>true</testBooleanElement><testStringElement>TEST</testStringElement><testFloatElement>5.0</testFloatElement><testDoubleElement>11152.725</testDoubleElement></testElement><testElement><testFloatElement>1.0</testFloatElement><testDoubleElement>83952.1525</testDoubleElement></testElement><testElement><testBooleanElement>false</testBooleanElement><testStringElement>DEF</testStringElement></testElement><testElement><testIntElement>153</testIntElement><testFloatElement>9.0</testFloatElement></testElement><testElement><testDateElement>1988-01-01</testDateElement></testElement><testElement><testDateElement>1979-01-01T01:23:45</testDateElement></testElement><testValidationElement><testUnboundedEnumElement>TEST_ENUM_ONE</testUnboundedEnumElement><testUnboundedEnumElement>TEST_ENUM_THREE</testUnboundedEnumElement><testRequiredLongElement>0</testRequiredLongElement></testValidationElement><testValidationElement><testUnboundedEnumElement>TEST_ENUM_TWO</testUnboundedEnumElement><testRequiredLongElement>0</testRequiredLongElement></testValidationElement><testAttributeElement stringAttribute=\"string\" intAttribute=\"1\" longAttribute=\"1\" booleanAttribute=\"true\" floatAttribute=\"1.0\" doubleAttribute=\"1.0\" dateAttribute=\"1960-01-01\" shortAttribute=\"1\" byteAttribute=\"1\"></testAttributeElement><testAttributeElement stringAttribute=\"string2\" intAttribute=\"2\" longAttribute=\"2\" booleanAttribute=\"false\" floatAttribute=\"2.0\" doubleAttribute=\"2.0\" dateAttribute=\"1963-01-01\" shortAttribute=\"2\" byteAttribute=\"2\"></testAttributeElement><testAttributeElement stringAttribute=\"string3\"></testAttributeElement><testAttributeElement intAttribute=\"4\" longAttribute=\"4\"></testAttributeElement><testUnboundedWrapperElement><testElement><testIntElement>231</testIntElement><testLongElement>4444444444444</testLongElement><testBooleanElement>true</testBooleanElement><testStringElement>TEST2</testStringElement><testFloatElement>50.0</testFloatElement><testDoubleElement>21152.725</testDoubleElement></testElement><testValidationElement><testUnboundedEnumElement>TEST_ENUM_THREE</testUnboundedEnumElement><testUnboundedEnumElement>TEST_ENUM_TWO</testUnboundedEnumElement><testUnboundedEnumElement>TEST_ENUM_ONE</testUnboundedEnumElement><testRequiredLongElement>0</testRequiredLongElement></testValidationElement><testAttributeElement stringAttribute=\"string4\" intAttribute=\"4\" longAttribute=\"4\" booleanAttribute=\"true\" floatAttribute=\"4.0\" doubleAttribute=\"4.0\" dateAttribute=\"2002-01-01\" shortAttribute=\"4\" byteAttribute=\"4\"></testAttributeElement></testUnboundedWrapperElement><testUnboundedWrapperElement><testElement><testIntElement>331</testIntElement><testLongElement>5555555555555</testLongElement><testBooleanElement>true</testBooleanElement><testStringElement>TEST3</testStringElement><testFloatElement>60.0</testFloatElement><testDoubleElement>61152.725</testDoubleElement></testElement><testValidationElement><testUnboundedEnumElement>TEST_ENUM_TWO</testUnboundedEnumElement><testUnboundedEnumElement>TEST_ENUM_THREE</testUnboundedEnumElement><testUnboundedEnumElement>TEST_ENUM_ONE</testUnboundedEnumElement><testRequiredLongElement>0</testRequiredLongElement></testValidationElement><testAttributeElement stringAttribute=\"string5\" intAttribute=\"5\" longAttribute=\"5\" booleanAttribute=\"true\" floatAttribute=\"5.0\" doubleAttribute=\"5.0\" dateAttribute=\"1934-01-01\" shortAttribute=\"5\" byteAttribute=\"5\"></testAttributeElement></testUnboundedWrapperElement><testBoundedWrapperElement><testValidationElement><testUnboundedEnumElement>TEST_ENUM_ONE</testUnboundedEnumElement><testUnboundedEnumElement>TEST_ENUM_TWO</testUnboundedEnumElement><testUnboundedEnumElement>TEST_ENUM_THREE</testUnboundedEnumElement><testRequiredLongElement>0</testRequiredLongElement></testValidationElement><testAttributeElement stringAttribute=\"string3\" intAttribute=\"3\" longAttribute=\"3\" booleanAttribute=\"true\" floatAttribute=\"3.0\" doubleAttribute=\"3.0\" dateAttribute=\"1999-01-01\" shortAttribute=\"3\" byteAttribute=\"3\"></testAttributeElement></testBoundedWrapperElement></testRoot>";
	private static final String EXPECTED_SMALL = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><testRoot type=\"Test6\" xmlns=\"http://javolution.org/xml/schema/javolution\"><testElement><testIntElement>1</testIntElement><testBooleanElement>true</testBooleanElement></testElement><testElement><testFloatElement>1.0</testFloatElement><testDoubleElement>1.5</testDoubleElement></testElement></testRoot>";

	private DatatypeFactory _dataTypeFactory;

	@Before
	public void init() throws DatatypeConfigurationException{
		_dataTypeFactory = DatatypeFactory.newInstance();
	}

	@Test
	public void testWithLargeNestedMixedObject() throws JAXBException{
		final JAXBAnnotatedObjectWriter jaxbWriter = new JAXBAnnotatedObjectWriterImpl(TestRoot.class);
		//jaxbWriter.setUseCDATA(true);
		//jaxbWriter.setValidating(true);

		//final JAXBContext contextA = JAXBContext.newInstance(TestRoot.class);
		//final Marshaller marshaller = contextA.createMarshaller();

		final TestRoot testRoot = createLargeNestedMixedObject();
		final StringWriter writer = new StringWriter();
		jaxbWriter.write(testRoot, writer);
		//marshaller.marshal(testRoot, writer);

		//System.out.println(writer.toString());

		assertEquals("Case 1 XML", EXPECTED_LARGE_NESTED_MIXED, writer.toString());
	}

	@Test
	public void testWithLargeNestedMixedObjectWithJAXBElementWrapping() throws JAXBException{
		final JAXBAnnotatedObjectWriter jaxbWriter = new JAXBAnnotatedObjectWriterImpl(TestRoot.class);
		//jaxbWriter.setUseCDATA(true);
		//jaxbWriter.setValidating(true);

		//final JAXBContext contextA = JAXBContext.newInstance(TestRoot.class);
		//final Marshaller marshaller = contextA.createMarshaller();

		final TestRoot testRoot = createLargeNestedMixedObject();
		final StringWriter writer = new StringWriter();
		final JAXBElement<TestRoot> jaxbElement = new JAXBElement<TestRoot>(
				new QName("http://javolution.org/xml/schema/javolution","TestRoot"),
				TestRoot.class, testRoot);
		jaxbWriter.write(jaxbElement, writer);
		//marshaller.marshal(testRoot, writer);

		//System.out.println(writer.toString());

		assertEquals("Case 1 XML", EXPECTED_LARGE_NESTED_MIXED, writer.toString());
	}

	private TestRoot createLargeNestedMixedObject() {
		final TestRoot testRoot = new TestRoot();
		testRoot.setType("Test6");
		testRoot.setType2("Test9");

		final TestElement testElement = new TestElement();
		testElement.setTestIntElement(1);
		testElement.setTestBooleanElement(true);

		final TestElement testElement2 = new TestElement();
		testElement2.setTestLongElement(999999999999999L);
		testElement2.setTestStringElement("ABC");

		final TestElement testElement3 = new TestElement();
		testElement3.setTestIntElement(131);
		testElement3.setTestLongElement(333333333333333L);
		testElement3.setTestFloatElement(5.0f);
		testElement3.setTestDoubleElement(11152.725);
		testElement3.setTestBooleanElement(true);
		testElement3.setTestStringElement("TEST");

		final TestElement testElement4 = new TestElement();
		testElement4.setTestFloatElement(1.0f);
		testElement4.setTestDoubleElement(83952.1525);

		final TestElement testElement5 = new TestElement();
		testElement5.setTestBooleanElement(false);
		testElement5.setTestStringElement("DEF");

		final TestElement testElement6 = new TestElement();
		testElement6.setTestIntElement(153);
		testElement6.setTestFloatElement(9.0f);

		final TestElement testElement7 = new TestElement();
		testElement7.setTestDateElement(_dataTypeFactory.newXMLGregorianCalendar("1988-01-01"));

		final TestElement testElement8 = new TestElement();
		testElement8.setTestDateElement(_dataTypeFactory.newXMLGregorianCalendar("1979-01-01T01:23:45"));

		testRoot.getTestElement().add(testElement);
		testRoot.getTestElement().add(testElement2);
		testRoot.getTestElement().add(testElement3);
		testRoot.getTestElement().add(testElement4);
		testRoot.getTestElement().add(testElement5);
		testRoot.getTestElement().add(testElement6);
		testRoot.getTestElement().add(testElement7);
		testRoot.getTestElement().add(testElement8);

		final TestValidationElement testValidationElement = new TestValidationElement();
		testValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_ONE);
		testValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_THREE);

		final TestValidationElement testValidationElement2 = new TestValidationElement();
		testValidationElement2.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_TWO);

		testRoot.getTestValidationElement().add(testValidationElement);
		testRoot.getTestValidationElement().add(testValidationElement2);


		final TestAttributeElement testAttributeElement = new TestAttributeElement();
		testAttributeElement.setStringAttribute("string");
		testAttributeElement.setIntAttribute(1);
		testAttributeElement.setLongAttribute(1L);
		testAttributeElement.setBooleanAttribute(true);
		testAttributeElement.setFloatAttribute(1.0f);
		testAttributeElement.setDoubleAttribute(1.0);
		testAttributeElement.setByteAttribute((byte)1);
		testAttributeElement.setShortAttribute((short)1);
		testAttributeElement.setDateAttribute(_dataTypeFactory.newXMLGregorianCalendar("1960-01-01"));

		final TestAttributeElement testAttributeElement2 = new TestAttributeElement();
		testAttributeElement2.setStringAttribute("string2");
		testAttributeElement2.setIntAttribute(2);
		testAttributeElement2.setLongAttribute(2L);
		testAttributeElement2.setBooleanAttribute(false);
		testAttributeElement2.setFloatAttribute(2.0f);
		testAttributeElement2.setDoubleAttribute(2.0);
		testAttributeElement2.setByteAttribute((byte)2);
		testAttributeElement2.setShortAttribute((short)2);
		testAttributeElement2.setDateAttribute(_dataTypeFactory.newXMLGregorianCalendar("1963-01-01"));

		final TestAttributeElement testAttributeElement3 = new TestAttributeElement();
		testAttributeElement3.setStringAttribute("string3");

		final TestAttributeElement testAttributeElement4 = new TestAttributeElement();
		testAttributeElement4.setIntAttribute(4);
		testAttributeElement4.setLongAttribute(4L);

		testRoot.getTestAttributeElement().add(testAttributeElement);
		testRoot.getTestAttributeElement().add(testAttributeElement2);
		testRoot.getTestAttributeElement().add(testAttributeElement3);
		testRoot.getTestAttributeElement().add(testAttributeElement4);

		final TestBoundedWrapperElement testBoundedWrapperElement = new TestBoundedWrapperElement();;

		final TestElement testBoundedWrapperElementTestElement = new TestElement();
		testBoundedWrapperElementTestElement.setTestIntElement(131);
		testBoundedWrapperElementTestElement.setTestLongElement(333333333333333L);
		testBoundedWrapperElementTestElement.setTestFloatElement(5.0f);
		testBoundedWrapperElementTestElement.setTestDoubleElement(11152.725);
		testBoundedWrapperElementTestElement.setTestBooleanElement(true);
		testBoundedWrapperElementTestElement.setTestStringElement("TEST");

		final TestValidationElement testBoundedWrapperElementTestValidationElement = new TestValidationElement();
		testBoundedWrapperElementTestValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_ONE);
		testBoundedWrapperElementTestValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_TWO);
		testBoundedWrapperElementTestValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_THREE);

		final TestAttributeElement testBoundedWrapperElementTestAttributeElement = new TestAttributeElement();;
		testBoundedWrapperElementTestAttributeElement.setStringAttribute("string3");
		testBoundedWrapperElementTestAttributeElement.setIntAttribute(3);
		testBoundedWrapperElementTestAttributeElement.setLongAttribute(3L);
		testBoundedWrapperElementTestAttributeElement.setBooleanAttribute(true);
		testBoundedWrapperElementTestAttributeElement.setFloatAttribute(3.0f);
		testBoundedWrapperElementTestAttributeElement.setDoubleAttribute(3.0);
		testBoundedWrapperElementTestAttributeElement.setByteAttribute((byte)3);
		testBoundedWrapperElementTestAttributeElement.setShortAttribute((short)3);
		testBoundedWrapperElementTestAttributeElement.setDateAttribute(_dataTypeFactory.newXMLGregorianCalendar("1999-01-01"));

		testBoundedWrapperElement.setTestAttributeElement(testBoundedWrapperElementTestAttributeElement);
		testBoundedWrapperElement.setTestValidationElement(testBoundedWrapperElementTestValidationElement);
		testBoundedWrapperElement.setTestAttributeElement(testBoundedWrapperElementTestAttributeElement);

		testRoot.setTestBoundedWrapperElement(testBoundedWrapperElement);

		final TestUnboundedWrapperElement testUnboundedWrapperElement = new TestUnboundedWrapperElement();

		final TestElement testUnboundedWrapperElementTestElement = new TestElement();
		testUnboundedWrapperElementTestElement.setTestIntElement(231);
		testUnboundedWrapperElementTestElement.setTestLongElement(4444444444444L);
		testUnboundedWrapperElementTestElement.setTestFloatElement(50.0f);
		testUnboundedWrapperElementTestElement.setTestDoubleElement(21152.725);
		testUnboundedWrapperElementTestElement.setTestBooleanElement(true);
		testUnboundedWrapperElementTestElement.setTestStringElement("TEST2");

		final TestValidationElement testUnboundedWrapperElementTestValidationElement = new TestValidationElement();
		testUnboundedWrapperElementTestValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_THREE);
		testUnboundedWrapperElementTestValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_TWO);
		testUnboundedWrapperElementTestValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_ONE);

		final TestAttributeElement testUnboundedWrapperElementTestAttributeElement = new TestAttributeElement();
		testUnboundedWrapperElementTestAttributeElement.setStringAttribute("string4");
		testUnboundedWrapperElementTestAttributeElement.setIntAttribute(4);
		testUnboundedWrapperElementTestAttributeElement.setLongAttribute(4L);
		testUnboundedWrapperElementTestAttributeElement.setBooleanAttribute(true);
		testUnboundedWrapperElementTestAttributeElement.setFloatAttribute(4.0f);
		testUnboundedWrapperElementTestAttributeElement.setDoubleAttribute(4.0);
		testUnboundedWrapperElementTestAttributeElement.setByteAttribute((byte)4);
		testUnboundedWrapperElementTestAttributeElement.setShortAttribute((short)4);
		testUnboundedWrapperElementTestAttributeElement.setDateAttribute(_dataTypeFactory.newXMLGregorianCalendar("2002-01-01"));

		testUnboundedWrapperElement.getTestElement().add(testUnboundedWrapperElementTestElement);
		testUnboundedWrapperElement.getTestValidationElement().add(testUnboundedWrapperElementTestValidationElement);
		testUnboundedWrapperElement.getTestAttributeElement().add(testUnboundedWrapperElementTestAttributeElement);

		final TestUnboundedWrapperElement testUnboundedWrapperElement2 = new TestUnboundedWrapperElement();

		final TestElement testUnboundedWrapperElementTestElement2 = new TestElement();
		testUnboundedWrapperElementTestElement2.setTestIntElement(331);
		testUnboundedWrapperElementTestElement2.setTestLongElement(5555555555555L);
		testUnboundedWrapperElementTestElement2.setTestFloatElement(60.0f);
		testUnboundedWrapperElementTestElement2.setTestDoubleElement(61152.725);
		testUnboundedWrapperElementTestElement2.setTestBooleanElement(true);
		testUnboundedWrapperElementTestElement2.setTestStringElement("TEST3");

		final TestValidationElement testUnboundedWrapperElementTestValidationElement2 = new TestValidationElement();
		testUnboundedWrapperElementTestValidationElement2.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_TWO);
		testUnboundedWrapperElementTestValidationElement2.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_THREE);
		testUnboundedWrapperElementTestValidationElement2.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_ONE);

		final TestAttributeElement testUnboundedWrapperElementTestAttributeElement2 = new TestAttributeElement();
		testUnboundedWrapperElementTestAttributeElement2.setStringAttribute("string5");
		testUnboundedWrapperElementTestAttributeElement2.setIntAttribute(5);
		testUnboundedWrapperElementTestAttributeElement2.setLongAttribute(5L);
		testUnboundedWrapperElementTestAttributeElement2.setBooleanAttribute(true);
		testUnboundedWrapperElementTestAttributeElement2.setFloatAttribute(5.0f);
		testUnboundedWrapperElementTestAttributeElement2.setDoubleAttribute(5.0);
		testUnboundedWrapperElementTestAttributeElement2.setByteAttribute((byte)5);
		testUnboundedWrapperElementTestAttributeElement2.setShortAttribute((short)5);
		testUnboundedWrapperElementTestAttributeElement2.setDateAttribute(_dataTypeFactory.newXMLGregorianCalendar("1934-01-01"));

		testUnboundedWrapperElement2.getTestElement().add(testUnboundedWrapperElementTestElement2);
		testUnboundedWrapperElement2.getTestValidationElement().add(testUnboundedWrapperElementTestValidationElement2);
		testUnboundedWrapperElement2.getTestAttributeElement().add(testUnboundedWrapperElementTestAttributeElement2);

		testRoot.getTestUnboundedWrapperElement().add(testUnboundedWrapperElement);
		testRoot.getTestUnboundedWrapperElement().add(testUnboundedWrapperElement2);

		return testRoot;
	}

}
