/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.context.PoolContext;
import javolution.lang.MathLib;
import javolution.text.Text;
import javolution.text.TextBuilder;
import javolution.text.TypeFormat;

/**
 * <p> This class holds {@link javolution.lang} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 20, 2005
 */
final class PerfText extends Javolution implements Runnable {

    static volatile int COUNT = 1000; // Volatile to avoid loop unrolling.

    private final String STRING = "Concatenates this line 1000 times (resulting in a text of about 80,000 characters)";

    private final Text TEXT = Text.valueOf(STRING);

    private final Text ONE_CHAR = Text.valueOf('X');
    
    /** 
     * Executes benchmark.
     */
    public void run() {
        println("//////////////////////////////");
        println("// Package: javolution.text //");
        println("//////////////////////////////");
        println("");

        println("-- Primitive types formatting --");
        println("");
        int n = 1000000;
        int ii = MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE);
        long ll = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);

        print("StringBuffer.append(int): ");
        {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(ii);
            }
        }
        println(endTime(n));
        
        print("TextBuilder.append(int): ");
        {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.reset();
                tmp.append(ii);
            }
        }
        println(endTime(n));
        
        print("StringBuffer.append(long): ");
        {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(ll);
            }
        }
        println(endTime(n));
        
        print("TextBuilder.append(long): ");
        {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.reset();
                tmp.append(ll);
            }
        }
        println(endTime(n));
        
        /*@JVM-1.1+@
        float ff = MathLib.random(-1.0f, 1.0f);
        double dd = MathLib.random(-1.0d, 1.0d);
        
        print("StringBuffer.append(float): ");
        {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(ff);
            }
        }
        println(endTime(n));
        
        print("TextBuilder.append(float): ");
        {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.reset();
                tmp.append(ff);
            }
        }
        println(endTime(n));
        
        print("StringBuffer.append(double): ");
        {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(dd);
            }
        }
        println(endTime(n));
        
        print("TextBuilder.append(double): ");
        {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.reset();
                tmp.append(dd);
            }
        }
        println(endTime(n));
        /**/
        println("");
        
        println("-- Primitive types parsing --");
        println("");
        String is = "" + ii;
        String ls = "" + ll;

        print("Integer.parseInt(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Integer.parseInt(is) != ii) throw new Error();
            }
        }
        println(endTime(n));
        
        print("TypeFormat.parseInt(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseInt(is) != ii) throw new Error();
            }
        }
        println(endTime(n));
        
        print("Long.parseLong(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Long.parseLong(ls) != ll) throw new Error();
            }
        }
        println(endTime(n));
        
        print("TypeFormat.parseLong(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseLong(ls) != ll) throw new Error();
            }
        }
        println(endTime(n));
        
        /*@JVM-1.1+@
        String fs = "" + ff;
        String ds = "" + dd;

        print("Float.parseFloat(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Float.parseFloat(fs) != ff) throw new Error();
            }
        }
        println(endTime(n));
        
        print("TypeFormat.parseFloat(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseFloat(fs) != ff) throw new Error();
            }
        }
        println(endTime(n));
        
        print("Double.parseDouble(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Double.parseDouble(ds) != dd) throw new Error();
            }
        }
        println(endTime(n));
        
        print("TypeFormat.parseDouble(String): ");
        {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseDouble(ds) != dd) throw new Error();
            }
        }
        println(endTime(n));
        /**/
        println("");
        
        println("-- String/StringBuffer versus Text --");
        println("");

        println("\"" + STRING + "\"");

        print("String \"+\" operator: ");
        startTime();
        String str = "";
        for (int j = COUNT; --j >= 0;) {
            str += STRING;
        }
        println(endTime(1));

        print("StringBuffer \"append\" : ");
        startTime();
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j = COUNT; --j >= 0;) {
                sb.append(STRING);
            }
        }
        println(endTime(100));

        print("Text \"concat\" (heap): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            Text txt = Text.EMPTY;
            for (int j = COUNT; --j >= 0;) {
                txt = txt.concat(TEXT);
            }
        }
        println(endTime(100));

        print("Text \"concat\" (stack): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.EMPTY;
            for (int j = COUNT; --j >= 0;) {
                txt = txt.concat(TEXT);
            }
            PoolContext.exit();
        }
        println(endTime(100));

        println("");
        println("Inserts one character at random locations 1,000 times to the 80,000 characters text.");

        print("StringBuffer insert: ");
        startTime();
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer(str);
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, sb.length());
                sb.insert(index, 'X');
            }
        }
        println(endTime(100));

        print("Text insert (heap): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            Text txt = Text.valueOf(str);
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length());
                txt = txt.insert(index, ONE_CHAR);
            }
        }
        println(endTime(100));

        print("Text insert (stack): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.valueOf(str);
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length());
                txt = txt.insert(index, ONE_CHAR);
            }
            PoolContext.exit();
        }
        println(endTime(100));

        println("");
        println("Delete 1,000 times one character at random location from the 80,000 characters text.");

        print("StringBuffer delete: ");
        startTime();
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer(str); 
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, sb.length() - 1);
                sb.deleteCharAt(index);
            }
        }
        println(endTime(100));

        print("Text delete (heap): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            Text txt = Text.valueOf(str);
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length() - 1);
                txt = txt.delete(index, index + 1);
            }
        }
        println(endTime(100));

        print("Text delete (stack): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.valueOf(str);
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length() - 1);
                txt = txt.delete(index, index + 1);
            }
            PoolContext.exit();
        }
        println(endTime(100));

        println("");
    }
}