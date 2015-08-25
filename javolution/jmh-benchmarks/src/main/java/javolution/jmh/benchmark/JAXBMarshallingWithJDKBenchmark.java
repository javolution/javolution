/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.jmh.benchmark;

import javolution.osgi.internal.OSGiServices;
import javolution.xml.annotation.JAXBAnnotatedObjectReader;
import javolution.xml.annotation.JAXBAnnotationFactory;
import javolution.xml.jaxb.test.schema.TestRoot;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@State(Scope.Thread)
public class JAXBMarshallingWithJDKBenchmark {

	private JAXBAnnotatedObjectReader reader;
	private JAXBContext jaxbContext;
	private StringWriter stringWriter;
	private TestRoot largeNestedMixedObject;

	@Setup
	public void setup() throws JAXBException{
		final JAXBAnnotationFactory jaxbFactory = OSGiServices.getJAXBAnnotationFactory();
		reader = jaxbFactory.createJAXBAnnotatedObjectReader(TestRoot.class);

		jaxbContext = JAXBContext.newInstance(TestRoot.class);

		final String largeNestedMixedObjectString = readResourceToString(JAXBMarshallingWithJDKBenchmark.class.getResourceAsStream("/test-large-nested-mixed-object.xml"));
		largeNestedMixedObject = reader.read(new StringReader(largeNestedMixedObjectString));
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
	@BenchmarkMode({Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime})
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void measureJDK(Blackhole bh) throws InterruptedException, JAXBException {
		stringWriter = new StringWriter();
		//NOTE: Marshallers are not thread-safe, only the JAXBContext is. So a new
		// Marshaller must be gotten each time.
		final Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(largeNestedMixedObject, stringWriter);
		bh.consume(marshaller);
		bh.consume(stringWriter);
	}

	public static void main(final String[] args) throws RunnerException {

		final Options opt = new OptionsBuilder()
		.include(JAXBMarshallingWithJDKBenchmark.class.getSimpleName())
		.warmupIterations(5)
		.measurementIterations(5)
		.forks(1)
		.build();

		new Runner(opt).run();
	}

}
