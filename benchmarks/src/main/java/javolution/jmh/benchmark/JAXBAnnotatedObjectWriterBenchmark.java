/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.jmh.benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import javolution.osgi.internal.OSGiServices;
import javolution.xml.annotation.JAXBAnnotatedObjectWriter;
import javolution.xml.annotation.JAXBAnnotationFactory;
import javolution.xml.jaxb.test.schema.TestRoot;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@State(Scope.Thread)
public class JAXBAnnotatedObjectWriterBenchmark {

	private JAXBAnnotatedObjectWriter writer;
	private Unmarshaller unmarshaller;
	private TestRoot largeNestedMixedObject;

	@Setup
	public void setup() throws JAXBException{
		final JAXBAnnotationFactory jaxbFactory = OSGiServices.getJAXBAnnotationFactory();
		writer = jaxbFactory.createJAXBAnnotatedObjectWriter(TestRoot.class);

		final JAXBContext jaxbContext = JAXBContext.newInstance(TestRoot.class);
		unmarshaller = jaxbContext.createUnmarshaller();

		String fileName = System.getProperty("test.file");

		if(fileName == null){
			fileName = "/test-large-nested-mixed-object.xml";
		}

		final String largeNestedMixedObjectString = readResourceToString(JAXBAnnotatedObjectWriterBenchmark.class.getResourceAsStream(fileName));
		largeNestedMixedObject = (TestRoot) unmarshaller.unmarshal(new StringReader(largeNestedMixedObjectString));
	}

	private String readResourceToString(final InputStream xmlUrl){
		final StringBuilder build = new StringBuilder();
		final byte[] buf = new byte[1024];
		int length;

		try {
			while ((length = xmlUrl.read(buf)) != -1) {
				build.append(new String(buf, 0, length));
			}
		} catch (final IOException e) {
			throw new RuntimeException("Error Reading Resource!", e);
		}

		return build.toString();
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void measureJavolution(Blackhole bh) throws InterruptedException, JAXBException {
		final StringWriter stringWriter = new StringWriter();
		// Unlike its JAXB counterpart, the JAXBAnnotatedObjectWriter is stateless and
		// thus thread safe, so the same one is reusuable for all threads
		writer.write(largeNestedMixedObject, stringWriter);
		bh.consume(stringWriter);
	}

	public static void main(final String[] args) throws RunnerException {

		final Options opt = new OptionsBuilder()
		.include(JAXBAnnotatedObjectWriterBenchmark.class.getSimpleName())
		.warmupIterations(5)
		.measurementIterations(5)
		.forks(1)
		.build();

		new Runner(opt).run();
	}

}
