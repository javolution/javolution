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
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class HashMapBenchmark {

    private HashMap<Class<?>,Integer> hashMap;
    private Random random;

    @Setup
    public void setup() throws JAXBException{
        hashMap = new HashMap<Class<?>, Integer>(15);
        hashMap.put(Integer.class, 0);
        hashMap.put(Boolean.class, 1);
        hashMap.put(Long.class, 2);
        hashMap.put(Short.class, 3);
        hashMap.put(Double.class, 4);
        hashMap.put(Float.class, 5);
        hashMap.put(String.class, 6);
        hashMap.put(Byte.class, 7);
        hashMap.put(int.class, 8);
        hashMap.put(boolean.class, 9);
        hashMap.put(long.class, 10);
        hashMap.put(short.class, 11);
        hashMap.put(double.class, 12);
        hashMap.put(float.class, 13);
        hashMap.put(byte.class, 14);

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
                result = hashMap.get(Integer.class);
                break;
            case 1:
                result = hashMap.get(Boolean.class);
                break;
            case 2:
                result = hashMap.get(Long.class);
                break;
            case 3:
                result = hashMap.get(Short.class);
                break;
            case 4:
                result = hashMap.get(Double.class);
                break;
            case 5:
                result = hashMap.get(Float.class);
                break;
            case 6:
                result = hashMap.get(String.class);
                break;
            case 7:
                result = hashMap.get(Byte.class);
                break;
            case 8:
                result = hashMap.get(int.class);
                break;
            case 9:
                result = hashMap.get(boolean.class);
                break;
            case 10:
                result = hashMap.get(long.class);
                break;
            case 11:
                result = hashMap.get(short.class);
                break;
            case 12:
                result = hashMap.get(double.class);
                break;
            case 13:
                result = hashMap.get(float.class);
                break;
            case 14:
                result = hashMap.get(byte.class);
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
