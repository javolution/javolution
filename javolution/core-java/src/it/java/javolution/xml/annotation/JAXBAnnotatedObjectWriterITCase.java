/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.annotation;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import javolution.context.LogContext;
import javolution.tools.Perfometer;
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

/**
 * Class for Benchmarking the JAXB Annotated Object Writer
 *
 * Follow instructions in the comments and toggle as appropriate
 *
 * Set up a test case in the static block. The default case is based
 * on test-large-nested-mixed-object.xml
 *
 * Pick Number of Iterations (toggle comment or specify)
 * Pick JDK or Javolution (toggle comment)
 *
 * Note: If you do both results will be close but can introduce a small
 * amount of bias. For best results do one at a time.
 *
 */
public class JAXBAnnotatedObjectWriterITCase {

	private static final DatatypeFactory DATA_TYPE_FACTORY;
	private static final TestRoot TEST_ROOT;

	private Perfometer<Marshaller> jdkPerf;
	private Perfometer<JAXBAnnotatedObjectWriter> javolutionPerf;

	static {
		try {
			DATA_TYPE_FACTORY = DatatypeFactory.newInstance();
		}
		catch (final DatatypeConfigurationException e) {
			throw new RuntimeException("Error Initializing!");
		}

		TEST_ROOT = new TestRoot();
		TEST_ROOT.setType("Test6");
		TEST_ROOT.setType2("Test9");

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
		testElement7.setTestDateElement(DATA_TYPE_FACTORY.newXMLGregorianCalendar("1988-01-01"));

		final TestElement testElement8 = new TestElement();
		testElement8.setTestDateElement(DATA_TYPE_FACTORY.newXMLGregorianCalendar("1979-01-01T01:23:45"));

		TEST_ROOT.getTestElement().add(testElement);
		TEST_ROOT.getTestElement().add(testElement2);
		TEST_ROOT.getTestElement().add(testElement3);
		TEST_ROOT.getTestElement().add(testElement4);
		TEST_ROOT.getTestElement().add(testElement5);
		TEST_ROOT.getTestElement().add(testElement6);
		TEST_ROOT.getTestElement().add(testElement7);
		TEST_ROOT.getTestElement().add(testElement8);

		final TestValidationElement testValidationElement = new TestValidationElement();
		testValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_ONE);
		testValidationElement.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_THREE);

		final TestValidationElement testValidationElement2 = new TestValidationElement();
		testValidationElement2.getTestUnboundedEnumElement().add(TestEnumElement.TEST_ENUM_TWO);

		TEST_ROOT.getTestValidationElement().add(testValidationElement);
		TEST_ROOT.getTestValidationElement().add(testValidationElement2);

		final TestAttributeElement testAttributeElement = new TestAttributeElement();
		testAttributeElement.setStringAttribute("string");
		testAttributeElement.setIntAttribute(1);
		testAttributeElement.setLongAttribute(1L);
		testAttributeElement.setBooleanAttribute(true);
		testAttributeElement.setFloatAttribute(1.0f);
		testAttributeElement.setDoubleAttribute(1.0);
		testAttributeElement.setByteAttribute((byte)1);
		testAttributeElement.setShortAttribute((short)1);
		testAttributeElement.setDateAttribute(DATA_TYPE_FACTORY.newXMLGregorianCalendar("1960-01-01"));

		final TestAttributeElement testAttributeElement2 = new TestAttributeElement();
		testAttributeElement2.setStringAttribute("string2");
		testAttributeElement2.setIntAttribute(2);
		testAttributeElement2.setLongAttribute(2L);
		testAttributeElement2.setBooleanAttribute(false);
		testAttributeElement2.setFloatAttribute(2.0f);
		testAttributeElement2.setDoubleAttribute(2.0);
		testAttributeElement2.setByteAttribute((byte)2);
		testAttributeElement2.setShortAttribute((short)2);
		testAttributeElement2.setDateAttribute(DATA_TYPE_FACTORY.newXMLGregorianCalendar("1963-01-01"));

		final TestAttributeElement testAttributeElement3 = new TestAttributeElement();
		testAttributeElement3.setStringAttribute("string3");

		final TestAttributeElement testAttributeElement4 = new TestAttributeElement();
		testAttributeElement4.setIntAttribute(4);
		testAttributeElement4.setLongAttribute(4L);

		TEST_ROOT.getTestAttributeElement().add(testAttributeElement);
		TEST_ROOT.getTestAttributeElement().add(testAttributeElement2);
		TEST_ROOT.getTestAttributeElement().add(testAttributeElement3);
		TEST_ROOT.getTestAttributeElement().add(testAttributeElement4);

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
		testBoundedWrapperElementTestAttributeElement.setDateAttribute(DATA_TYPE_FACTORY.newXMLGregorianCalendar("1999-01-01"));

		testBoundedWrapperElement.setTestAttributeElement(testBoundedWrapperElementTestAttributeElement);
		testBoundedWrapperElement.setTestValidationElement(testBoundedWrapperElementTestValidationElement);
		testBoundedWrapperElement.setTestAttributeElement(testBoundedWrapperElementTestAttributeElement);

		TEST_ROOT.setTestBoundedWrapperElement(testBoundedWrapperElement);

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
		testUnboundedWrapperElementTestAttributeElement.setDateAttribute(DATA_TYPE_FACTORY.newXMLGregorianCalendar("2002-01-01"));

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
		testUnboundedWrapperElementTestAttributeElement2.setDateAttribute(DATA_TYPE_FACTORY.newXMLGregorianCalendar("1934-01-01"));

		testUnboundedWrapperElement2.getTestElement().add(testUnboundedWrapperElementTestElement2);
		testUnboundedWrapperElement2.getTestValidationElement().add(testUnboundedWrapperElementTestValidationElement2);
		testUnboundedWrapperElement2.getTestAttributeElement().add(testUnboundedWrapperElementTestAttributeElement2);

		TEST_ROOT.getTestUnboundedWrapperElement().add(testUnboundedWrapperElement);
		TEST_ROOT.getTestUnboundedWrapperElement().add(testUnboundedWrapperElement2);
	}

	@Before
	public void init(){
		jdkPerf = new Perfometer<Marshaller>("JDK JAXB Serialize") {
			Marshaller marshaller;

			@Override
			protected void initialize() throws Exception {
				marshaller = getInput();
			}

			@Override
			protected void run(final boolean measure) throws Exception {
				if (measure) {
					final StringWriter writer = new StringWriter();
					marshaller.marshal(TEST_ROOT, writer);
				}
			}

		};

		javolutionPerf = new Perfometer<JAXBAnnotatedObjectWriter>("Javolution JAXB Serialize") {

			@Override
			protected void initialize() throws Exception {
			}

			@Override
			protected void run(final boolean measure) throws Exception {
				if (measure) {
					final StringWriter writer = new StringWriter();
					getInput().write(TEST_ROOT, writer);
				}
			}
		};
	}

	@Test
	public void testJaxbPerformance() throws JAXBException{
		//benchmark(1);
		//benchmark(10);
		//benchmark(100);
		benchmark(1000);
		//benchmark(10000);
		//benchmark(100000);
	}

	private void benchmark(final int iterations) throws JAXBException{
		LogContext.info("Benchmarking... JAXB Annotation Serialize");

		final JAXBContext context = JAXBContext.newInstance(TestRoot.class);
		final Marshaller marshaller = context.createMarshaller();

		final JAXBAnnotatedObjectWriter jaxbWriter = new JAXBAnnotatedObjectWriterImpl(TestRoot.class);

		jdkPerf.measure(marshaller, iterations).print();
		javolutionPerf.measure(jaxbWriter, iterations).print();
	}
}
