/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.lang.MathLib;
import javolution.lang.Text;
import javolution.realtime.PoolContext;

/**
 * <p> This class holds {@link javolution.lang} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 20, 2005
 */
final class Perf_Lang extends Javolution implements Runnable {

    private static final int COUNT = 1000;

    private final String STRING = "Concatenates this line 1000 times (resulting in a text of about 80,000 characters)";

    private final Text TEXT = Text.valueOf(STRING);

    private final Text ONE_CHAR = Text.valueOf('X');
    
    /** 
     * Executes benchmark.
     */
    public void run() {
        println("//////////////////////////////");
        println("// Package: javolution.lang //");
        println("//////////////////////////////");
        println("");

        println("-- String/StringBuffer versus Text --");
        println("");

        println("\"" + STRING + "\"");

        print("String \"+\" operator: ");
        startTime();
        String str = "";
        for (int j = 0; j < COUNT; j++) {
            str += STRING;
        }
        println(endTime(1));

        print("StringBuffer \"append\" : ");
        startTime();
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < COUNT; j++) {
                sb.append(STRING);
            }
        }
        println(endTime(100));

        print("Text \"concat\" (heap): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            Text txt = Text.EMPTY;
            for (int j = 0; j < COUNT; j++) {
                txt = txt.concat(TEXT);
            }
        }
        println(endTime(100));

        print("Text \"concat\" (stack): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.EMPTY;
            for (int j = 0; j < COUNT; j++) {
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
            for (int j = 1; j < COUNT; j++) {
                int index = MathLib.randomInt(0, sb.length());
                sb.insert(index, 'X');
            }
        }
        println(endTime(100));

        print("Text insert (heap): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            Text txt = Text.valueOf(str);
            for (int j = 1; j < COUNT; j++) {
                int index = MathLib.randomInt(0, txt.length());
                txt = txt.insert(index, ONE_CHAR);
            }
        }
        println(endTime(100));

        print("Text insert (stack): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.valueOf(str);
            for (int j = 1; j < COUNT; j++) {
                int index = MathLib.randomInt(0, txt.length());
                txt = txt.insert(index, ONE_CHAR);
            }
            PoolContext.exit();
        }
        println(endTime(100));

        println("");
        println("Delete 1,000 times one character at random location from the 80,000 characters text.");

        StringBuffer tmp = new StringBuffer();
        print("StringBuffer delete: ");
        startTime();
        for (int i = 0; i < 100; i++) {
            StringBuffer sb = new StringBuffer(str); 
            for (int j = 0; j < COUNT; j++) {
                int index = MathLib.randomInt(0, sb.length() - 1);
                sb.deleteCharAt(index);
            }
        }
        println(endTime(100));

        print("Text delete (heap): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            Text txt = Text.valueOf(str);
            for (int j = 0; j < COUNT; j++) {
                int index = MathLib.randomInt(0, txt.length() - 1);
                txt = txt.delete(index, index + 1);
            }
        }
        println(endTime(100));

        print("Text delete (stack): ");
        startTime();
        for (int i = 0; i < 100; i++) {
            PoolContext.enter();
            Text txt = Text.valueOf(str);
            for (int j = 0; j < COUNT; j++) {
                int index = MathLib.randomInt(0, txt.length() - 1);
                txt = txt.delete(index, index + 1);
            }
            PoolContext.exit();
        }
        println(endTime(100));

        println("");
    }
}