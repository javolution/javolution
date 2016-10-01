/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.jmh.benchmark;

import org.javolution.osgi.internal.OSGiServices;
import org.javolution.xml.jaxb.JAXBAnnotatedObjectReader;
import org.javolution.xml.jaxb.JAXBAnnotationFactory;
import org.javolution.xml.jaxb.test.schema.TestRoot;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class JAXBAnnotatedObjectReaderBenchmark {

	private InputStream xmlUrl;
	private String xmlString;
	private JAXBAnnotatedObjectReader reader;

	@Setup
	public void setup() throws JAXBException{
		final JAXBAnnotationFactory jaxbFactory = OSGiServices.getJAXBAnnotationFactory();
		reader = jaxbFactory.createJAXBAnnotatedObjectReader(TestRoot.class);

		String fileName = System.getProperty("test.file");

		if(fileName == null){
			fileName = "/test-large-nested-mixed-object.xml";
		}

		xmlUrl = JAXBAnnotatedObjectReaderBenchmark.class.getResourceAsStream(fileName);

		try {
			final StringBuilder build = new StringBuilder();
			final byte[] buf = new byte[1024];
			int length;

			while ((length = xmlUrl.read(buf)) != -1) {
				build.append(new String(buf, 0, length));
			}

			xmlString = build.toString();
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public void measureJavolution(Blackhole bh) throws InterruptedException, JAXBException {
		// Unlike its JAXB counterpart, the JAXBAnnotatedObjectReader is stateless and
		// thus thread safe, so the same one is reusuable for all threads
		bh.consume(reader.read(new StringReader(xmlString)));
	}

	public static void main(final String[] args) throws RunnerException {

		final Options opt = new OptionsBuilder()
		.include(JAXBAnnotatedObjectReaderBenchmark.class.getSimpleName())
		.warmupIterations(5)
		.measurementIterations(5)
		.forks(1)
		.build();

		new Runner(opt).run();
	}

}
