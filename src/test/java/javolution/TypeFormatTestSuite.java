/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import javolution.lang.MathLib;
import javolution.testing.TestCase;
import javolution.testing.TestContext;
import javolution.testing.TestSuite;
import javolution.text.TextBuilder;
import javolution.text.TypeFormat;

/**
 * <p> This class holds the test cases for the {@link TypeFormat}
 *     class.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.3, March 19, 2007
 */
public final class TypeFormatTestSuite extends TestSuite {

    // Holds the number of random samples.
    static final int N = 1000;

    public TypeFormatTestSuite() {

        // Parsing.
        addTest(new ParseBoolean());
        addTest(new BooleanParse());
        addTest(new ParseInt());
        addTest(new IntegerParseInt());
        addTest(new ParseLong());
        addTest(new LongParseLong());
        addTest(new ParseLongHexa());
        addTest(new LongParseLongHexa());
        addTest(new ParseDouble());
        addTest(new DoubleParseDouble());

        // Formatting.
        addTest(new FormatBoolean());
        addTest(new StringBufferAppendBoolean());
        addTest(new FormatInt());
        addTest(new StringBufferAppendInt());
        addTest(new FormatLong());
        addTest(new StringBufferAppendLong());
        addTest(new FormatLongHexa());
        addTest(new StringBufferAppendLongHexa());
        addTest(new FormatDouble());
        addTest(new StringBufferAppendDouble());
    }

    class ParseBoolean extends TestCase {

        boolean[] _expected = new boolean[N];

        boolean[] _actual = new boolean[N];

        String[] _strings = new String[N];

        public String getName() {
            return "TypeFormat.parseBoolean(CharSequence)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(0, 1) == 0 ? false : true;
                _strings[i] = _expected[i] ? new String("true") : new String("false");
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseBoolean(_strings[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertException(IllegalArgumentException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseBoolean("TRUE?"); // Extraneous character.
                }
            });
        }
    }

    class BooleanParse extends TestCase {

        boolean[] _expected = new boolean[N];

        boolean[] _actual = new boolean[N];

        String[] _strings = new String[N];

        public String getName() {
            return "String.equalsIgnoreCase(\"true\")";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(0, 1) == 0 ? false : true;
                _strings[i] = _expected[i] ? new String("true") : new String("false");
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = parseBoolean(_strings[i]);
            }
        }

        private boolean parseBoolean(String str) {
            return str.equalsIgnoreCase("true");
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
        }
    }

    class ParseInt extends TestCase {

        int[] _expected = new int[N];

        int[] _actual = new int[N];

        String[] _strings = new String[N];

        public String getName() {
            return "TypeFormat.parseInt(CharSequence)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE);
                _strings[i] = String.valueOf(_expected[i]);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseInt(_strings[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals(Integer.MIN_VALUE, TypeFormat.parseInt(Integer.MIN_VALUE + ""));
            TestContext.assertEquals(0, TypeFormat.parseInt("0"));
            TestContext.assertEquals(Integer.MAX_VALUE, TypeFormat.parseInt(Integer.MAX_VALUE + ""));
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseInt("2147483648"); // Overflow.
                }
            });
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseInt("123E4"); // Invalid Character.
                }
            });
        }
    }

    class IntegerParseInt extends TestCase {

        int[] _expected = new int[N];

        int[] _actual = new int[N];

        String[] _strings = new String[N];

        public String getName() {
            return "Integer.parseInt(String)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE);
                _strings[i] = String.valueOf(_expected[i]);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = Integer.parseInt(_strings[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals(Integer.MIN_VALUE, Integer.parseInt("-2147483648"));
            TestContext.assertEquals(0, Integer.parseInt("0"));
            TestContext.assertEquals(Integer.MAX_VALUE, Integer.parseInt("2147483647"));
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    Integer.parseInt("2147483648"); // Overflow.
                }
            });
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    Integer.parseInt("123E4"); // Invalid Character.
                }
            });
        }
    }

    class ParseLong extends TestCase {

        long[] _expected = new long[N];

        long[] _actual = new long[N];

        String[] _strings = new String[N];

        public String getName() {
            return "TypeFormat.parseLong(CharSequence)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);
                _strings[i] = String.valueOf(_expected[i]);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseLong(_strings[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals(Long.MIN_VALUE, TypeFormat.parseLong(Long.MIN_VALUE + ""));
            TestContext.assertEquals(0, TypeFormat.parseLong("0"));
            TestContext.assertEquals(Long.MAX_VALUE, TypeFormat.parseLong(Long.MAX_VALUE + ""));
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseLong("9223372036854775808"); // Overflow.
                }
            });
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseLong("123E4"); // Invalid Character.
                }
            });
        }
    }

    class LongParseLong extends TestCase {

        long[] _expected = new long[N];

        long[] _actual = new long[N];

        String[] _strings = new String[N];

        public String getName() {
            return "Long.parseLong(String)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);
                _strings[i] = String.valueOf(_expected[i]);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = Long.parseLong(_strings[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals(Long.MIN_VALUE, TypeFormat.parseLong(Long.MIN_VALUE + ""));
            TestContext.assertEquals(0, TypeFormat.parseLong("0"));
            TestContext.assertEquals(Long.MAX_VALUE, TypeFormat.parseLong(Long.MAX_VALUE + ""));
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    Long.parseLong("9223372036854775808"); // Overflow.
                }
            });
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    Long.parseLong("123E4"); // Invalid Character.
                }
            });
        }
    }

    class ParseLongHexa extends ParseLong {

        public String getName() {
            return "TypeFormat.parseLong(CharSequence, 16)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);
                _strings[i] = Long.toString(_expected[i], 16);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseLong(_strings[i], 16);
            }
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals(Long.MIN_VALUE, TypeFormat.parseLong("-8000000000000000", 16));
            TestContext.assertEquals(0, TypeFormat.parseLong("0", 16));
            TestContext.assertEquals(Long.MAX_VALUE, TypeFormat.parseLong("7fffffffffffffff", 16));
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseLong("8000000000000000", 16); // Overflow.
                }
            });
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseLong("1234x"); // Invalid Character.
                }
            });
        }
    }

    class LongParseLongHexa extends LongParseLong {

        public String getName() {
            return "Long.parseLong(String, 16)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);
                _strings[i] = Long.toString(_expected[i], 16);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = Long.parseLong(_strings[i], 16);
            }
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals(Long.MIN_VALUE, Long.parseLong("-8000000000000000", 16));
            TestContext.assertEquals(0, Long.parseLong("0", 16));
            TestContext.assertEquals(Long.MAX_VALUE, Long.parseLong("7fffffffffffffff", 16));
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    Long.parseLong("8000000000000000", 16); // Overflow.
                }
            });
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    Long.parseLong("1234x"); // Invalid Character.
                }
            });
        }
    }

    class ParseDouble extends TestCase {

        double[] _expected = new double[N];

        double[] _actual = new double[N];

        String[] _strings = new String[N];

        public String getName() {
            return "TypeFormat.parseDouble(CharSequence)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = Double.longBitsToDouble(MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE));
                _strings[i] = String.valueOf(_expected[i]);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseDouble(_strings[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual, 0);
            TestContext.assertEquals(0, TypeFormat.parseDouble("0"), 0);
            TestContext.assertEquals(Double.NaN, TypeFormat.parseDouble("NaN"), 0);
            TestContext.assertEquals(Double.NEGATIVE_INFINITY, TypeFormat.parseDouble("-Infinity"), 0);
            TestContext.assertEquals(Double.POSITIVE_INFINITY, TypeFormat.parseDouble("Infinity"), 0);
            TestContext.assertEquals(Double.NEGATIVE_INFINITY, TypeFormat.parseDouble("-1E500"), 0);
            TestContext.assertEquals(Double.POSITIVE_INFINITY, TypeFormat.parseDouble("1E500"), 0);
            TestContext.assertEquals(0, TypeFormat.parseDouble("1E-500"), 0);
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    TypeFormat.parseDouble("+"); // Invalid String.
                }
            });
        }
    }

    class DoubleParseDouble extends TestCase {

        double[] _expected = new double[N];

        double[] _actual = new double[N];

        String[] _strings = new String[N];

        public String getName() {
            return "Double.parseDouble(String)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = Double.longBitsToDouble(MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE));
                _strings[i] = String.valueOf(_expected[i]);
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _actual[i] = Double.parseDouble(_strings[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            TestContext.assertArrayEquals(_expected, _actual, 0);
            TestContext.assertEquals(0, Double.parseDouble("0"), 0);
            TestContext.assertEquals(Double.NaN, Double.parseDouble("NaN"), 0);
            TestContext.assertEquals(Double.NEGATIVE_INFINITY, Double.parseDouble("-Infinity"), 0);
            TestContext.assertEquals(Double.POSITIVE_INFINITY, Double.parseDouble("Infinity"), 0);
            TestContext.assertEquals(Double.NEGATIVE_INFINITY, TypeFormat.parseDouble("-1E500"), 0);
            TestContext.assertEquals(Double.POSITIVE_INFINITY, TypeFormat.parseDouble("1E500"), 0);
            TestContext.assertEquals(0, TypeFormat.parseDouble("1E-500"), 0);
            TestContext.assertException(NumberFormatException.class, new Runnable() {

                public void run() {
                    Double.parseDouble("123E4?"); // Invalid Character.
                }
            });
        }
    }

    //
    // FORMATTING
    //
    class FormatBoolean extends TestCase {

        boolean[] _expected = new boolean[N];

        boolean[] _actual = new boolean[N];

        TextBuilder[] _appendables = new TextBuilder[N];

        public String getName() {
            return "TypeFormat.format(boolean, Appendable)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(0, 1) == 0 ? false : true;
                _appendables[i] = TextBuilder.newInstance();
            }
        }

        public void execute() throws Exception {
            for (int i = 0; i < N; i++) {
                TypeFormat.format(_expected[i], _appendables[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() throws Exception {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseBoolean(_appendables[i]);
            }
            TestContext.assertArrayEquals(_expected, _actual);
        }
    }

    class StringBufferAppendBoolean extends TestCase {

        boolean[] _expected = new boolean[N];

        boolean[] _actual = new boolean[N];

        StringBuffer[] _appendables = new StringBuffer[N];

        public String getName() {
            return "StringBuffer.append(boolean)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(0, 1) == 0 ? false : true;
                _appendables[i] = new StringBuffer();
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _appendables[i].append(_expected[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseBoolean(_appendables[i] + "");
            }
            TestContext.assertArrayEquals(_expected, _actual);
        }
    }

    class FormatInt extends TestCase {

        int[] _expected = new int[N];

        int[] _actual = new int[N];

        TextBuilder[] _appendables = new TextBuilder[N];

        public String getName() {
            return "TypeFormat.format(int, Appendable)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE);
                _appendables[i] = TextBuilder.newInstance();
            }
        }

        public void execute() throws Exception {
            for (int i = 0; i < N; i++) {
                TypeFormat.format(_expected[i], _appendables[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() throws Exception {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseInt(_appendables[i]);
            }
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals("" + Integer.MIN_VALUE, TypeFormat.format(Integer.MIN_VALUE, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("0", TypeFormat.format(0, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("" + Integer.MAX_VALUE, TypeFormat.format(Integer.MAX_VALUE, TextBuilder.newInstance()).toString());
        }
    }

    class StringBufferAppendInt extends TestCase {

        int[] _expected = new int[N];

        int[] _actual = new int[N];

        StringBuffer[] _appendables = new StringBuffer[N];

        public String getName() {
            return "StringBuffer.append(int)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Integer.MIN_VALUE, Integer.MAX_VALUE);
                _appendables[i] = new StringBuffer();
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _appendables[i].append(_expected[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseInt(_appendables[i].toString());
            }
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals("" + Integer.MIN_VALUE, new StringBuffer().append(Integer.MIN_VALUE).toString());
            TestContext.assertEquals("0", new StringBuffer().append(0).toString());
            TestContext.assertEquals("" + Integer.MAX_VALUE, new StringBuffer().append(Integer.MAX_VALUE).toString());
        }
    }

    class FormatLong extends TestCase {

        long[] _expected = new long[N];

        long[] _actual = new long[N];

        TextBuilder[] _appendables = new TextBuilder[N];

        public String getName() {
            return "TypeFormat.format(long, Appendable)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);
                _appendables[i] = TextBuilder.newInstance();
            }
        }

        public void execute() throws Exception {
            for (int i = 0; i < N; i++) {
                TypeFormat.format(_expected[i], _appendables[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() throws Exception {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseLong(_appendables[i]);
            }
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals("" + Long.MIN_VALUE, TypeFormat.format(Long.MIN_VALUE, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("0", TypeFormat.format(0L, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("" + Long.MAX_VALUE, TypeFormat.format(Long.MAX_VALUE, TextBuilder.newInstance()).toString());
        }
    }

    class StringBufferAppendLong extends TestCase {

        long[] _expected = new long[N];

        long[] _actual = new long[N];

        StringBuffer[] _appendables = new StringBuffer[N];

        public String getName() {
            return "StringBuffer.append(long)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.random(Long.MIN_VALUE, Long.MAX_VALUE);
                _appendables[i] = new StringBuffer();
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _appendables[i].append(_expected[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseLong(_appendables[i].toString());
            }
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals("" + Long.MIN_VALUE, new StringBuffer().append(Long.MIN_VALUE).toString());
            TestContext.assertEquals("0", new StringBuffer().append(0L).toString());
            TestContext.assertEquals("" + Long.MAX_VALUE, new StringBuffer().append(Long.MAX_VALUE).toString());
        }
    }

    class FormatLongHexa extends FormatLong {

        public String getName() {
            return "TypeFormat.format(long, 16, Appendable)";
        }

        public void execute() throws Exception {
            for (int i = 0; i < N; i++) {
                TypeFormat.format(_expected[i], 16, _appendables[i]);
            }
        }

        public void validate() throws Exception {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseLong(_appendables[i], 16);
            }
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals("-8000000000000000", TypeFormat.format(Long.MIN_VALUE, 16, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("0", TypeFormat.format(0L, 16, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("7fffffffffffffff", TypeFormat.format(Long.MAX_VALUE, 16, TextBuilder.newInstance()).toString());
        }
    }

    class StringBufferAppendLongHexa extends StringBufferAppendLong {

        public String getName() {
            return "StringBuffer.append(Long.toString(long, 16))";
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _appendables[i].append(Long.toString(_expected[i], 16));
            }
        }

        public void validate() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseLong(_appendables[i].toString(), 16);
            }
            TestContext.assertArrayEquals(_expected, _actual);
            TestContext.assertEquals("-8000000000000000", new StringBuffer().append(Long.toString(Long.MIN_VALUE, 16)).toString());
            TestContext.assertEquals("0", new StringBuffer().append(Long.toString(0, 16)).toString());
            TestContext.assertEquals("7fffffffffffffff", new StringBuffer().append(Long.toString(Long.MAX_VALUE, 16)).toString());
        }
    }

    class FormatDouble extends TestCase {

        double[] _expected = new double[N];

        double[] _actual = new double[N];

        TextBuilder[] _appendables = new TextBuilder[N];

        public String getName() {
            return "TypeFormat.format(double, Appendable)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.pow(MathLib.PI, MathLib.random(-300, 300));
                _appendables[i] = TextBuilder.newInstance();
            }
        }

        public void execute() throws Exception {
            for (int i = 0; i < N; i++) {
                TypeFormat.format(_expected[i], _appendables[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() throws Exception {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseDouble(_appendables[i]);
            }
            TestContext.assertArrayEquals(_expected, _actual, 0);
            TestContext.assertEquals("-Infinity", TypeFormat.format(Double.NEGATIVE_INFINITY, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("0.0", TypeFormat.format(0.0, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("Infinity", TypeFormat.format(Double.POSITIVE_INFINITY, TextBuilder.newInstance()).toString());
            TestContext.assertEquals("NaN", TypeFormat.format(Double.NaN, TextBuilder.newInstance()).toString());
        }
    }

    class StringBufferAppendDouble extends TestCase {

        double[] _expected = new double[N];

        double[] _actual = new double[N];

        StringBuffer[] _appendables = new StringBuffer[N];

        public String getName() {
            return "StringBuffer.append(double)";
        }

        public void setUp() {
            for (int i = 0; i < _expected.length; i++) {
                _expected[i] = MathLib.pow(MathLib.PI, MathLib.random(-300, 300));
                _appendables[i] = new StringBuffer();
            }
        }

        public void execute() {
            for (int i = 0; i < N; i++) {
                _appendables[i].append(_expected[i]);
            }
        }

        public int count() {
            return _expected.length;
        }

        public void validate() {
            for (int i = 0; i < N; i++) {
                _actual[i] = TypeFormat.parseDouble(_appendables[i].toString());
            }
            TestContext.assertArrayEquals(_expected, _actual, 0);
            TestContext.assertEquals("-Infinity", new StringBuffer().append(Double.NEGATIVE_INFINITY).toString());
            TestContext.assertEquals("0.0", new StringBuffer().append(0.0).toString());
            TestContext.assertEquals("Infinity", new StringBuffer().append(Double.POSITIVE_INFINITY).toString());
            TestContext.assertEquals("NaN", new StringBuffer().append(Double.NaN).toString());
        }
    }
}
