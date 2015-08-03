/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.annotation;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import javolution.context.LogContext;
import javolution.tools.Perfometer;
import javolution.xml.internal.annotation.JAXBAnnotatedObjectReaderImpl;
import javolution.xml.jaxb.common.test.schema.TestCommonRoot;
import javolution.xml.jaxb.test.schema.TestRoot;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JAXBAnnotatedObjectReaderITCase {

	private static final boolean USE_COMMON_SCHEMA;
	private static final String XML_STRING;
	
	private Perfometer<Unmarshaller> jdkPerf;
	private Perfometer<JAXBAnnotatedObjectReader> javolutionPerf;

	static {
		USE_COMMON_SCHEMA = false;
		
		// USE_COMMON_SCHEMA = false
		//final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-small.xml");
		final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-more-than-one-of-same-element-with-mixed-data.xml");
		//final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-with-enum-and-date.xml");
		//final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-bounded-unbounded-nesting.xml");
		//final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-bounded-element-with-nested-attribute-only.xml");
		//final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-large-nested-mixed-object.xml");
		//final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-large-valid-nested-mixed-object.xml");
		//final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-with-unmapped-element.xml");
		
		// USE_COMMON_SCHEMA = true
		// final URL xmlUrl = JAXBAnnotatedObjectReaderTest.class.getResource("/test-with-choice-element.xml");
		
		File xmlFile;

		try {
			xmlFile = new File(xmlUrl.toURI());
			XML_STRING = new String(Files.readAllBytes(xmlFile.toPath()), StandardCharsets.UTF_8);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Before
	public void init(){
		jdkPerf = new Perfometer<Unmarshaller>("JDK JAXB Deserialize") {
			Unmarshaller unmarshaller;

			@Override
			protected void initialize() throws Exception {
				unmarshaller = getInput();

				for(int i = 0; i < getNbrOfIterations(); i++) {
					unmarshaller.unmarshal(new StringReader(XML_STRING));
				}
				System.gc();
			}

			@Override
			protected void run(final boolean measure) throws Exception {
				if (measure) {
					for(int i = 0; i < getNbrOfIterations(); i++) {
						if(USE_COMMON_SCHEMA){
							final TestCommonRoot testCommonRoot = (TestCommonRoot) unmarshaller.unmarshal(new StringReader(XML_STRING));
							assert testCommonRoot != null;	
						}
						else {
							final TestRoot testRoot = (TestRoot) unmarshaller.unmarshal(new StringReader(XML_STRING));
							assert testRoot != null;
						}
					}
				}
			}

		};

		javolutionPerf = new Perfometer<JAXBAnnotatedObjectReader>("Javolution JAXB Deserialize") {


			@Override
			protected void initialize() throws Exception {
				for(int i = 0; i < getNbrOfIterations(); i++) {
					getInput().read(new StringReader(XML_STRING));
				}
				System.gc();
			}

			@Override
			protected void run(final boolean measure) throws Exception {
				if (measure) {
					for(int i = 0; i < getNbrOfIterations(); i++) {
						if(USE_COMMON_SCHEMA){
							final TestCommonRoot testCommonRoot = (TestCommonRoot)getInput().read(new StringReader(XML_STRING));
							assert testCommonRoot != null;
						}
						else {
							final TestRoot testRoot = (TestRoot)getInput().read(new StringReader(XML_STRING));
							assert testRoot != null;
						}
					}
				}
			}
		};
	}

	@Test
	public void testJaxbPerformance() throws JAXBException, SAXException{
		// Don't run more than 1 at the same time or you'll skew the results 
		// of subsequent runs because of caching. Pick a iterations set and do a cold JVM run
		//benchmark(1);
		//benchmark(10);
		//benchmark(25);
		benchmark(50);
		//benchmark(100);
		//benchmark(250);
		//benchmark(1000);
	}

	private void benchmark(final int iterations) throws JAXBException, SAXException{
		LogContext.info("Benchmarking... JAXB Annotation Deserialize");

		final JAXBContext context; 
		final JAXBAnnotatedObjectReader jaxbReader;
		
		if(USE_COMMON_SCHEMA){
			context = JAXBContext.newInstance(TestCommonRoot.class);
			jaxbReader = new JAXBAnnotatedObjectReaderImpl(TestCommonRoot.class);
		}
		else {
			context = JAXBContext.newInstance(TestRoot.class);
			jaxbReader = new JAXBAnnotatedObjectReaderImpl(TestRoot.class);
		}
		
		final Unmarshaller unmarshaller = context.createUnmarshaller();

		// To bench with validation...
		// jaxbReader.setValidating(true);
		// SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		// 
		// Source[] schemas = new StreamSource[]{ 
		// new StreamSource(this.getClass().getResourceAsStream("/test-jaxb-common-schema.xsd")),
		// new StreamSource(this.getClass().getResourceAsStream("/test-jaxb-schema.xsd"))
		// };
		// 
		// Schema schema = schemaFactory.newSchema(schemas);
		// unmarshaller.setSchema(schema);
		// 
		// End Validation Snippet
		
		jdkPerf.measure(unmarshaller, iterations).print();
		System.gc();
		javolutionPerf.measure(jaxbReader, iterations).print();
		System.gc();
	}
}
