/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.jmh.benchmark;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import javolution.util.FastMap;
import javolution.util.function.Equalities;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@State(Scope.Thread)
public class FastMapBenchmark {

    private FastMap<Class<?>,Integer> fastMap;
    private Random random;

    @Setup
    public void setup() throws JAXBException{
        fastMap = new FastMap<Class<?>, Integer>(Equalities.IDENTITY);
        fastMap.put(Integer.class, 0);
        fastMap.put(Boolean.class, 1);
        fastMap.put(Long.class, 2);
        fastMap.put(Short.class, 3);
        fastMap.put(Double.class, 4);
        fastMap.put(Float.class, 5);
        fastMap.put(String.class, 6);
        fastMap.put(Byte.class, 7);
        fastMap.put(int.class, 8);
        fastMap.put(boolean.class, 9);
        fastMap.put(long.class, 10);
        fastMap.put(short.class, 11);
        fastMap.put(double.class, 12);
        fastMap.put(float.class, 13);
        fastMap.put(byte.class, 14);

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
                result = fastMap.get(Integer.class);
                break;
            case 1:
                result = fastMap.get(Boolean.class);
                break;
            case 2:
                result = fastMap.get(Long.class);
                break;
            case 3:
                result = fastMap.get(Short.class);
                break;
            case 4:
                result = fastMap.get(Double.class);
                break;
            case 5:
                result = fastMap.get(Float.class);
                break;
            case 6:
                result = fastMap.get(String.class);
                break;
            case 7:
                result = fastMap.get(Byte.class);
                break;
            case 8:
                result = fastMap.get(int.class);
                break;
            case 9:
                result = fastMap.get(boolean.class);
                break;
            case 10:
                result = fastMap.get(long.class);
                break;
            case 11:
                result = fastMap.get(short.class);
                break;
            case 12:
                result = fastMap.get(double.class);
                break;
            case 13:
                result = fastMap.get(float.class);
                break;
            case 14:
                result = fastMap.get(byte.class);
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
