/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.realtime.PoolContext;
import javolution.util.Reflection;

/**
 * <p> This class holds {@link javolution.lang} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 */
final class Perf_Lang extends Javolution implements Runnable {

    /** 
     * Executes benchmark.
     */
    public void run() {
        println("-- String/StringBuffer/StringBuilder versus Text/TextBuilder --");
        final String TXT_STRING = "Concatenates this line one hundred times "
                + "(resulting in a text of about 8000 characters)";
        final Text TXT_CHARS = Text.valueOf(TXT_STRING);
        println("\"" + TXT_STRING + "\"");
        print("String \"+\" operator: ");
        startTime();
        for (int i = 0; i < 100; i++) {
            String str = "";
            for (int j = 0; j < 100; j++) {
                str += TXT_STRING;
            }
        }
        endTime(100);
        print("StringBuffer \"append\": ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < 100; j++) {
                sb.append(TXT_STRING);
            }
        }
        endTime(1000);
        Reflection.Constructor newStringBuilder = Reflection
                .getConstructor("java.lang.StringBuilder()");
        Reflection.Method stringBuilderAppend = Reflection
                .getMethod("java.lang.StringBuilder.append(java.lang.CharSequence)");
        if (newStringBuilder != null) {
            print("StringBuilder \"append\": ");
            startTime();
            for (int i = 0; i < 1000; i++) {
                Object sb = newStringBuilder.newInstance();
                for (int j = 0; j < 100; j++) {
                    stringBuilderAppend.invoke(sb, TXT_CHARS);
                }
            }
            endTime(1000);
        }
        print("Text \"plus\" (heap): ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            Text chars = Text.EMPTY;
            for (int j = 0; j < 100; j++) {
                chars = chars.plus(TXT_CHARS);
            }
        }
        endTime(1000);
        print("Text \"plus\" (stack): ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            PoolContext.enter();
            Text chars = Text.EMPTY;
            for (int j = 0; j < 100; j++) {
                chars = chars.plus(TXT_CHARS);
            }
            PoolContext.exit();
        }
        endTime(1000);
        print("TextBuilder \"append\": ");
        startTime();
        for (int i = 0; i < 1000; i++) {
            PoolContext.enter();
            TextBuilder cb = TextBuilder.newInstance();
            for (int j = 0; j < 100; j++) {
                cb.append(TXT_CHARS);
            }
            cb.toText();
            PoolContext.exit();
        }
        endTime(1000);
        println("");
    }
}