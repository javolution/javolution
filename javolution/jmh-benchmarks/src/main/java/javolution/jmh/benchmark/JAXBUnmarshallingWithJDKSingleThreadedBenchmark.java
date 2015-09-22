/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.jmh.benchmark;

import javolution.xml.jaxb.test.schema.TestRoot;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class JAXBUnmarshallingWithJDKSingleThreadedBenchmark {

	private InputStream xmlUrl;
	private String xmlString;
	private JAXBContext jaxbContext;
	private Unmarshaller unmarshaller;

	@Setup
	public void setup() throws JAXBException{
		jaxbContext = JAXBContext.newInstance(TestRoot.class);
		unmarshaller = jaxbContext.createUnmarshaller();

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
	public void measureJDK(Blackhole bh) throws InterruptedException, JAXBException {
		//NOTE: Use this one with only 1 thread. If you're single threaded you can
		// reuse the unmarshaller
		bh.consume(unmarshaller.unmarshal(new StringReader(xmlString)));
	}

	/*
	 * ============================== HOW TO RUN THIS TEST: ====================================
	 *
	 * You are expected to see the different run modes for the same benchmark.
	 * Note the units are different, scores are consistent with each other.
	 *
	 * You can run this test:
	 *
	 * a) Via the command line:
	 *    $ mvn clean install
	 *    $ java -jar target/benchmarks.jar JMHSample_02 -wi 5 -i 5 -f 1
	 *    (we requested 5 warmup/measurement iterations, single fork)
	 *
	 * b) Via the Java API:
	 *    (see the JMH homepage for possible caveats when running from IDE:
	 *      http://openjdk.java.net/projects/code-tools/jmh/)
	 */
	public static void main(final String[] args) throws RunnerException {

		final Options opt = new OptionsBuilder()
		.include(JAXBUnmarshallingWithJDKSingleThreadedBenchmark.class.getSimpleName())
		.warmupIterations(5)
		.measurementIterations(5)
		.forks(1)
		.build();

		new Runner(opt).run();
	}

}
