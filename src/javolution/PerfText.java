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
        int n = 100000;
        int ii = MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE);
        long ll = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);

        print("StringBuffer.append(int): ");
        for (int j=0; j < 10; j++) {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(ii);
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TextBuilder.append(int): ");
        for (int j=0; j < 10; j++) {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.clear().append(ii);
            }
            keepBestTime(n);
            if (!tmp.contentEquals(ii + "")) throw new Error(); 
        }
        println(endTime());
        
        print("StringBuffer.append(long): ");
        for (int j=0; j < 10; j++) {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(ll);
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TextBuilder.append(long): ");
        for (int j=0; j < 10; j++) {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.clear().append(ll);
            }
            keepBestTime(n);
            if (!tmp.contentEquals(ll + "")) throw new Error(); 
        }
        println(endTime());
        
        /*@JVM-1.1+@
        float ff = MathLib.random(-1.0f, 1.0f);
        double dd = MathLib.random(-1.0d, 1.0d);
        
        print("StringBuffer.append(float): ");
        for (int j=0; j < 10; j++) {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(ff);
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TextBuilder.append(float): ");
        for (int j=0; j < 10; j++) {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.clear().append(ff);
            }
            keepBestTime(n);
            if (ff != Float.parseFloat(tmp.toString())) throw new Error(); 
        }
        println(endTime());
        
        print("StringBuffer.append(double): ");
        for (int j=0; j < 10; j++) {
            StringBuffer tmp = new StringBuffer();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.setLength(0);
                tmp.append(dd);
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TextBuilder.append(double): ");
        for (int j=0; j < 10; j++) {
            TextBuilder tmp = new TextBuilder();
            startTime();
            for (int i = 0; i < n; i++) {
                tmp.clear().append(dd);
            }
            keepBestTime(n);
            if (dd != Double.parseDouble(tmp.toString())) throw new Error(); 
        }
        println(endTime());
        /**/
        println("");
        
        println("-- Primitive types parsing --");
        println("");
        String is = "" + ii;
        String ls = "" + ll;

        print("Integer.parseInt(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Integer.parseInt(is) != ii) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TypeFormat.parseInt(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseInt(is) != ii) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("Long.parseLong(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Long.parseLong(ls) != ll) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TypeFormat.parseLong(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseLong(ls) != ll) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        
        /*@JVM-1.1+@
        String fs = "" + ff;
        String ds = "" + dd;

        print("Float.parseFloat(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Float.parseFloat(fs) != ff) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TypeFormat.parseFloat(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseFloat(fs) != ff) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("Double.parseDouble(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (Double.parseDouble(ds) != dd) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        
        print("TypeFormat.parseDouble(String): ");
        for (int j=0; j < 10; j++) {
            startTime();
            for (int i = 0; i < n; i++) {
                if (TypeFormat.parseDouble(ds) != dd) throw new Error();
            }
            keepBestTime(n);
        }
        println(endTime());
        /**/
        println("");
        
        println("-- String/StringBuffer versus Text --");
        println("");

        println("\"" + STRING + "\"");

        print("String \"+\" operator: ");
        String str = "";
        for (int i = 0; i < 2; i++) {
            str = "";
            startTime();
            for (int j = COUNT; --j >= 0;) {
                str += STRING;
            }
            keepBestTime(COUNT);
        }
        println(endTime());

        print("StringBuffer \"append\" : ");
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer();
            startTime();
            for (int j = COUNT; --j >= 0;) {
                sb.append(STRING);
            }
            keepBestTime(COUNT);
        }
        println(endTime());

        print("Text \"concat\" (heap): ");
        for (int i = 0; i < 100; i++) {
            Text txt = Text.EMPTY;
            startTime();
            for (int j = COUNT; --j >= 0;) {
                txt = txt.concat(TEXT);
            }
            keepBestTime(COUNT);
        }
        println(endTime());

        print("Text \"concat\" (stack): ");
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.EMPTY;
            startTime();
            for (int j = COUNT; --j >= 0;) {
                txt = txt.concat(TEXT);
            }
            keepBestTime(COUNT);
            PoolContext.exit();
        }
        println(endTime());

        println("");
        println("Inserts one character at random locations 1,000 times to the 80,000 characters text.");

        print("StringBuffer insert: ");
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer(str);
            startTime();
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, sb.length());
                sb.insert(index, 'X');
            }
            keepBestTime(COUNT);
        }
        println(endTime());

        print("Text insert (heap): ");
        for (int i = 0; i < 100; i++) {
            Text txt = Text.valueOf(str);
            startTime();
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length());
                txt = txt.insert(index, ONE_CHAR);
            }
            keepBestTime(COUNT);
        }
        println(endTime());

        print("Text insert (stack): ");
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.valueOf(str);
            startTime();
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length());
                txt = txt.insert(index, ONE_CHAR);
            }
            keepBestTime(COUNT);
            PoolContext.exit();
        }
        println(endTime());

        println("");
        println("Delete 1,000 times one character at random location from the 80,000 characters text.");

        print("StringBuffer delete: ");
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer(str); 
            startTime();
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, sb.length() - 1);
                sb.deleteCharAt(index);
            }
            keepBestTime(COUNT);
        }
        println(endTime());

        print("Text delete (heap): ");
        for (int i = 0; i < 100; i++) {
            Text txt = Text.valueOf(str);
            startTime();
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length() - 1);
                txt = txt.delete(index, index + 1);
            }
            keepBestTime(COUNT);
        }
        println(endTime());

        print("Text delete (stack): ");
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.valueOf(str);
            startTime();
            for (int j = COUNT; --j >= 0;) {
                int index = MathLib.random(0, txt.length() - 1);
                txt = txt.delete(index, index + 1);
            }
            keepBestTime(COUNT);
            PoolContext.exit();
        }
        println(endTime());

        println("");
    }
}