/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import java.io.IOException;

import javolution.lang.Appendable;
import javolution.lang.Text;
import javolution.lang.TextBuilder;
import javolution.realtime.AllocationProfile;
import javolution.util.MathLib;
import javolution.util.Reflection;

/**
 * <p> This class holds {@link javolution.lang} benchmark.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 20, 2005
 */
final class Perf_Lang extends Javolution implements Runnable {

    private static final int COUNT = 1000;
    
    private final String PLUS_STRING = "Concatenates this line one thousand times "
        + "(resulting in a text of about 80,000 characters).";
    
    private final String INSERT_STRING = "Inserts this line into itself one thousand times."
        + " Then remove 80 characters at random location one thousand times";

    private final Text PLUS_TEXT = Text.valueOf(PLUS_STRING);
    
    private final Text INSERT_TEXT = Text.valueOf(INSERT_STRING);
    
    private static final Reflection.Constructor STRING_BUILDER_CONSTRUCTOR
    = Reflection.getConstructor("j2me.lang.StringBuilder()");
    
    /** 
     * Executes benchmark.
     */
    public void run() {
        println("//////////////////////////////");
        println("// Package: javolution.lang //");
        println("//////////////////////////////");
        println("");

        println("-- String/StringBuffer/StringBuilder versus Text/TextBuilder --");
        println("\"" + PLUS_STRING + "\"");
        print("String \"+\" operator: ");
        startTime();
        String str = "";
        for (int j = 0; j < COUNT; j++) {
            str += PLUS_STRING;
        }
        endTime(1);

        print("Text \"plus\": ");
        startTime();
        Text txt = Text.EMPTY;
        for (int j = 0; j < COUNT; j++) {
            txt = txt.plus(PLUS_TEXT);
        }
        endTime(1);

        // StringBuffer
        benchmarkAppendable(new StringBuffer());
        
        // StringBuilder (if supported)
        if (STRING_BUILDER_CONSTRUCTOR != null) {
            benchmarkAppendable(STRING_BUILDER_CONSTRUCTOR.newInstance());
        }
        
        // TextBuilder
        benchmarkAppendable(new TextBuilder());

        println("");
        
        println("-- StringBuffer versus Text (insertion/deletion) --");
        println("\"" + INSERT_STRING + "\"");

        print("StringBuffer insert: ");
        startTime();
        StringBuffer sb = new StringBuffer(INSERT_STRING);
        for (int j = 0; j < COUNT; j++) {
            int index = MathLib.abs(MathLib.randomInt()) % sb.length();
            sb.insert(index, INSERT_STRING);
        }
        endTime(1);
        
        print("StringBuffer delete: ");
        startTime();
        for (int j = 0; j < COUNT; j++) {
            int index = MathLib.abs(MathLib.randomInt()) % sb.length();
            sb.delete(index, Math.min(index + 80, sb.length()));
        }
        endTime(1);

        print("Text insert: ");
        startTime();
        txt = INSERT_TEXT;
        for (int j = 0; j < COUNT; j++) {
            int index = MathLib.abs(MathLib.randomInt()) % txt.length();
            txt = txt.insert(index, INSERT_TEXT);
        }
        endTime(1);
        
        print("Text delete: ");
        startTime();
        for (int j = 0; j < COUNT; j++) {
            int index = MathLib.abs(MathLib.randomInt()) % txt.length();
            txt = txt.delete(index, Math.min(index + 80, txt.length()));
        }
        endTime(1);
        
        println("-- Using Preallocation Facility --");
        AllocationProfile.preallocate();

        print("Text \"plus\": ");
        startTime();
        txt = Text.EMPTY;
        for (int j = 0; j < COUNT; j++) {
            txt = txt.plus(PLUS_TEXT);
        }
        endTime(1);
        
        print("Text insert: ");
        startTime();
        txt = INSERT_TEXT;
        for (int j = 0; j < COUNT; j++) {
            int index = MathLib.abs(MathLib.randomInt()) % txt.length();
            txt = txt.insert(index, INSERT_TEXT);
        }
        endTime(1);
        
        print("Text delete: ");
        startTime();
        for (int j = 0; j < COUNT; j++) {
            int index = MathLib.abs(MathLib.randomInt()) % txt.length();
            txt = txt.delete(index, Math.min(index + 80, txt.length()));
        }
        endTime(1);
        
        println("");
    }
    
    private void benchmarkAppendable(Object obj) {
        try {
            if (!(obj instanceof Appendable)) return;
            print(obj.getClass() + " \"append\": ");
            Appendable a = (Appendable) obj;
            startTime();
            for (int i = 0; i < COUNT; i++) {
                 a.append(PLUS_TEXT);
            }
            endTime(1);
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }
    
}