/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution;

import java.nio.ByteOrder;
import java.util.List;
import java.util.ArrayList;
import javolution.io.Struct;
import javolution.testing.TestCase;
import javolution.testing.TestContext;
import javolution.testing.TestSuite;
import javolution.text.Text;

/**
 * <p> This class holds the test cases for the {@link javolution.io io} classes.</p>
 *
 * @author Tom Palmer
 */
public final class StructTestSuite extends TestSuite {

    /** */
    enum TestEnum {

        D,
        B,
        A,
        C,
    }
    /**/

    public StructTestSuite() {
        addStructTests(ByteOrder.BIG_ENDIAN);
        addStructTests(ByteOrder.LITTLE_ENDIAN);
    }

    private void addStructTests(ByteOrder byteOrder) {
        addTest(new StructBoolTest(byteOrder));
        addTest(new StructFloat32Test(byteOrder));
        addTest(new StructFloat64Test(byteOrder));

        /** */
        addTest(new StructEnum8Test(byteOrder));
        addTest(new StructEnum16Test(byteOrder));
        addTest(new StructEnum32Test(byteOrder));
        addTest(new StructEnum64Test(byteOrder));
        /**/
        addTest(new StructSigned8Test(byteOrder));
        addTest(new StructSigned16Test(byteOrder));
        addTest(new StructSigned32Test(byteOrder));
        addTest(new StructSigned64Test(byteOrder));
        addTest(new StructUTF8StringTest(byteOrder));
        addTest(new StructUnsigned8Test(byteOrder));
        addTest(new StructUnsigned16Test(byteOrder));
        addTest(new StructUnsigned32Test(byteOrder));
    }

    private static class StructBoolTest extends BaseStructTest {

        private TestStruct struct;

        private StructBoolTest(ByteOrder order) {
            super("Bool", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.bool.set(true);
        }

        public void validate() throws Exception {
            TestContext.assertTrue(struct.bool.get(), Text.valueOf("bool setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Bool bool = new Bool();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    /** */
    private static class StructEnum8Test extends BaseStructTest {

        private TestStruct struct;

        private StructEnum8Test(ByteOrder order) {
            super("Enum8", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.enum8.set(TestEnum.A);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(TestEnum.A, struct.enum8.get(), "enum8 setting failed.");
        }

        private static class TestStruct extends BaseTestStruct {

            final Enum8 enum8 = new Enum8(TestEnum.values());

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructEnum16Test extends BaseStructTest {

        private TestStruct struct;

        private StructEnum16Test(ByteOrder order) {
            super("Enum16", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.enum16.set(TestEnum.B);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(TestEnum.B, struct.enum16.get(), "enum16 setting failed.");
        }

        private static class TestStruct extends BaseTestStruct {

            final Enum16 enum16 = new Enum16(TestEnum.values());

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructEnum32Test extends BaseStructTest {

        private TestStruct struct;

        private StructEnum32Test(ByteOrder order) {
            super("Enum32", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.enum32.set(TestEnum.B);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(TestEnum.B, struct.enum32.get(), "enum32 setting failed.");
        }

        private static class TestStruct extends BaseTestStruct {

            final Enum32 enum32 = new Enum32(TestEnum.values());

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructEnum64Test extends BaseStructTest {

        private TestStruct struct;

        private StructEnum64Test(ByteOrder order) {
            super("Enum64", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.enum64.set(TestEnum.B);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(TestEnum.B, struct.enum64.get(), "enum64 setting failed.");
        }

        private static class TestStruct extends BaseTestStruct {

            final Enum64 enum64 = new Enum64(TestEnum.values());

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }
    /**/

    private static class StructFloat32Test extends BaseStructTest {

        private TestStruct struct;

        private StructFloat32Test(ByteOrder order) {
            super("Float32", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.float32.set(1.2352f);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(1.2352f, struct.float32.get(), 1e-7, Text.valueOf("Float32 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Float32 float32 = new Float32();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructFloat64Test extends BaseStructTest {

        private TestStruct struct;

        private StructFloat64Test(ByteOrder order) {
            super("Float64", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.float64.set(1.2352f);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(1.2352f, struct.float64.get(), 1e-7, Text.valueOf("Float64 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Float64 float64 = new Float64();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructSigned8Test extends BaseStructTest {

        private TestStruct struct;

        private StructSigned8Test(ByteOrder order) {
            super("Signed8", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.signed8.set((byte) -123);
        }

        public void validate() throws Exception {
            TestContext.assertEquals((byte) -123, struct.signed8.get(), Text.valueOf("Signed8 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Signed8 signed8 = new Signed8();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructSigned16Test extends BaseStructTest {

        private TestStruct struct;

        private StructSigned16Test(ByteOrder order) {
            super("Signed16", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.signed16.set((short) -211);
        }

        public void validate() throws Exception {
            TestContext.assertEquals((short) -211, struct.signed16.get(), Text.valueOf("Signed16 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Signed16 signed16 = new Signed16();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructSigned32Test extends BaseStructTest {

        private TestStruct struct;

        private StructSigned32Test(ByteOrder order) {
            super("Signed32", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.signed32.set(-9872);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(-9872, struct.signed32.get(), Text.valueOf("Signed32 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Signed32 signed32 = new Signed32();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructSigned64Test extends BaseStructTest {

        private TestStruct struct;

        private StructSigned64Test(ByteOrder order) {
            super("Signed64", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.signed64.set(Integer.MIN_VALUE - 5);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(Integer.MIN_VALUE - 5, struct.signed64.get(), Text.valueOf("Signed64 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Signed64 signed64 = new Signed64();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructUTF8StringTest extends BaseStructTest {

        private TestStruct struct;

        private StructUTF8StringTest(ByteOrder order) {
            super("UTF8String", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.utf8.set("Hello World");
        }

        public void validate() throws Exception {
            TestContext.assertEquals("Hello World", struct.utf8.get(), Text.valueOf("UTF8String setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final UTF8String utf8 = new UTF8String(16);

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructUnsigned8Test extends BaseStructTest {

        private TestStruct struct;

        private StructUnsigned8Test(ByteOrder order) {
            super("Unsigned8", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.unsigned8.set((short) 0x88);
        }

        public void validate() throws Exception {
            TestContext.assertEquals((short) 0x88, struct.unsigned8.get(), Text.valueOf("Unsigned8 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Unsigned8 unsigned8 = new Unsigned8();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructUnsigned16Test extends BaseStructTest {

        private TestStruct struct;

        private StructUnsigned16Test(ByteOrder order) {
            super("Unsigned16", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.unsigned16.set(0x8888);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(0x8888, struct.unsigned16.get(), Text.valueOf("Unsigned16 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Unsigned16 unsigned16 = new Unsigned16();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static class StructUnsigned32Test extends BaseStructTest {

        private TestStruct struct;

        private StructUnsigned32Test(ByteOrder order) {
            super("Unsigned32", order);
        }

        public void execute() throws Exception {
            struct = new TestStruct(order);
            struct.unsigned32.set(0x88888888L);
        }

        public void validate() throws Exception {
            TestContext.assertEquals(0x88888888L, struct.unsigned32.get(), Text.valueOf("Unsigned32 setting failed."));
        }

        private static class TestStruct extends BaseTestStruct {

            final Unsigned32 unsigned32 = new Unsigned32();

            private TestStruct(ByteOrder order) {
                super(order);
            }
        }
    }

    private static abstract class BaseStructTest extends TestCase {

        protected final ByteOrder order;

        private final String type;

        protected BaseStructTest(String type, ByteOrder order) {
            this.type = type;
            this.order = order;
        }

        public String getName() {
            return "IoTest.Struct." + type + '(' + order + ')';
        }
    }

    private static abstract class BaseTestStruct extends Struct {

        private final ByteOrder order;

        private BaseTestStruct(ByteOrder order) {
            this.order = order;
        }

        public ByteOrder byteOrder() {
            return order;
        }
    }
}
