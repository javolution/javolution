/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.jmh.benchmark;

import org.javolution.util.FastMap;
import org.javolution.util.function.Order;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.xml.bind.JAXBException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class FastIdentityMapBenchmark {

    private FastMap<Class<?>,Integer> fastIdentityMap;
    private Random random;

    @Setup
    public void setup() throws JAXBException{
        fastIdentityMap = FastMap.<Class<?>, Integer> newMap(Order.IDENTITY);
        fastIdentityMap.put(Integer.class, 0);
        fastIdentityMap.put(Boolean.class, 1);
        fastIdentityMap.put(Long.class, 2);
        fastIdentityMap.put(Short.class, 3);
        fastIdentityMap.put(Double.class, 4);
        fastIdentityMap.put(Float.class, 5);
        fastIdentityMap.put(String.class, 6);
        fastIdentityMap.put(Byte.class, 7);
        fastIdentityMap.put(int.class, 8);
        fastIdentityMap.put(boolean.class, 9);
        fastIdentityMap.put(long.class, 10);
        fastIdentityMap.put(short.class, 11);
        fastIdentityMap.put(double.class, 12);
        fastIdentityMap.put(float.class, 13);
        fastIdentityMap.put(byte.class, 14);

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
                result = fastIdentityMap.get(Integer.class);
                break;
            case 1:
                result = fastIdentityMap.get(Boolean.class);
                break;
            case 2:
                result = fastIdentityMap.get(Long.class);
                break;
            case 3:
                result = fastIdentityMap.get(Short.class);
                break;
            case 4:
                result = fastIdentityMap.get(Double.class);
                break;
            case 5:
                result = fastIdentityMap.get(Float.class);
                break;
            case 6:
                result = fastIdentityMap.get(String.class);
                break;
            case 7:
                result = fastIdentityMap.get(Byte.class);
                break;
            case 8:
                result = fastIdentityMap.get(int.class);
                break;
            case 9:
                result = fastIdentityMap.get(boolean.class);
                break;
            case 10:
                result = fastIdentityMap.get(long.class);
                break;
            case 11:
                result = fastIdentityMap.get(short.class);
                break;
            case 12:
                result = fastIdentityMap.get(double.class);
                break;
            case 13:
                result = fastIdentityMap.get(float.class);
                break;
            case 14:
                result = fastIdentityMap.get(byte.class);
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
