/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.jmh.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.xml.bind.JAXBException;
import java.util.IdentityHashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class IdentityHashMapBenchmark {

    private IdentityHashMap<Class<?>,Integer> identityHashMap;
    private Random random;

    @Setup
    public void setup() throws JAXBException{
        identityHashMap = new IdentityHashMap<Class<?>, Integer>(15);
        identityHashMap.put(Integer.class, 0);
        identityHashMap.put(Boolean.class, 1);
        identityHashMap.put(Long.class, 2);
        identityHashMap.put(Short.class, 3);
        identityHashMap.put(Double.class, 4);
        identityHashMap.put(Float.class, 5);
        identityHashMap.put(String.class, 6);
        identityHashMap.put(Byte.class, 7);
        identityHashMap.put(int.class, 8);
        identityHashMap.put(boolean.class, 9);
        identityHashMap.put(long.class, 10);
        identityHashMap.put(short.class, 11);
        identityHashMap.put(double.class, 12);
        identityHashMap.put(float.class, 13);
        identityHashMap.put(byte.class, 14);

        random = new Random(0);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void measureGet(Blackhole bh) throws InterruptedException, JAXBException {
        int randomInt = random.nextInt(15);

        int result;

        switch (randomInt) {
            case 0:
                result = identityHashMap.get(Integer.class);
                break;
            case 1:
                result = identityHashMap.get(Boolean.class);
                break;
            case 2:
                result = identityHashMap.get(Long.class);
                break;
            case 3:
                result = identityHashMap.get(Short.class);
                break;
            case 4:
                result = identityHashMap.get(Double.class);
                break;
            case 5:
                result = identityHashMap.get(Float.class);
                break;
            case 6:
                result = identityHashMap.get(String.class);
                break;
            case 7:
                result = identityHashMap.get(Byte.class);
                break;
            case 8:
                result = identityHashMap.get(int.class);
                break;
            case 9:
                result = identityHashMap.get(boolean.class);
                break;
            case 10:
                result = identityHashMap.get(long.class);
                break;
            case 11:
                result = identityHashMap.get(short.class);
                break;
            case 12:
                result = identityHashMap.get(double.class);
                break;
            case 13:
                result = identityHashMap.get(float.class);
                break;
            case 14:
                result = identityHashMap.get(byte.class);
                break;
            default:
                throw new RuntimeException("Invalid Number");
        }

        bh.consume(result);
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
