/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;

import j2me.lang.UnsupportedOperationException;
import j2me.nio.ByteBuffer;
import j2me.nio.ByteOrder;
import j2me.util.Iterator;
import j2me.util.List;
import javolution.JavolutionError;
import javolution.lang.Enum;
import javolution.lang.TextBuilder;
import javolution.lang.TypeFormat;
import javolution.realtime.LocalContext;
import javolution.util.FastList;
import javolution.util.Reflection;

import java.io.IOException;

/**
 * <p> This class represents a <code>C/C++ struct</code>; it confers
 *     interoperability between Java classes and C/C++ struct.</p>
 * <p> Unlike <code>C/C++</code>, the storage layout of Java objects is not
 *     determined by the compiler. The layout of objects in memory is deferred
 *     to run time and determined by the interpreter (or just-in-time compiler).
 *     This approach allows for dynamic loading and binding; but also makes
 *     interfacing with <code>C/C++</code> code difficult. Hence, this class for
 *     which the memory layout is defined by the initialization order of the
 *     {@link Struct}'s {@link Member members} and follows the same alignment
 *      rules as <code>C/C++ structs</code>.</p>
 * <p> This class (as well as the {@link Union} sub-class) facilitates:
 *     <ul>
 *     <li> Memory sharing between Java applications and native libraries.</li>
 *     <li> Direct encoding/decoding of streams for which the structure
 *          is defined by legacy C/C++ code.</li>
 *     <li> Serialization/deserialization of Java objects (complete control,
 *          e.g. no class header)</li>
 *     <li> Mapping of Java objects to physical addresses (with JNI).</li>
 *     </ul></p>
 * <p> Because of its one-to-one mapping, it is relatively easy to convert C 
 *     header files (e.g. OpenGL bindings) to Java {@link Struct}/{@link Union}
 *     using simple text macros. Here is an example of C struct:<pre>
 *     struct Date {
 *         unsigned short year;
 *         unsigned char month;
 *         unsigned char day;
 *     };
 *     struct Student {
 *         char        name[64];
 *         struct Date birth;
 *         float       grades[10];
 *         Student*    next;
 *     };</pre>
 *     and here is the Java equivalent using this class:<pre>
 *     public static class Date extends Struct {
 *         public final Unsigned16 year = new Unsigned16();
 *         public final Unsigned8 month = new Unsigned8();
 *         public final Unsigned8 day   = new Unsigned8();
 *     }
 *     public static class Student extends Struct {
 *         public final Utf8String  name   = new Utf8String(64);
 *         public final Date        birth  = (Date) new StructMember(Date.class).get();
 *         public final Float32[]   grades = (Float32[]) new ArrayMember(new Float32[10]).get();
 *         public final Reference32 next   =  new Reference32(Student.class);
 *     }</pre>
 *     Struct's members are directly accessible:<pre>
 *     Student student = new Student();
 *     student.name.set("John Doe"); // Null terminated (C compatible)
 *     int age = 2003 - student.birth.year.get();
 *     student.grades[2].set(12.5f);
 *     student = (Student) student.next.get();</pre></p>
 * <p> Applications may also work with the raw {@link #getByteBuffer() bytes}
 *     directly. The following illustrate how {@link Struct} can be used to 
 *     decode/encode UDP messages directly:<pre>
 *     class MyUdpMessage extends Struct {
 *         ... // UDP message fields.
 *     }
 *     public void run() {
 *         byte[] bytes = new byte[1024];
 *         DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
 *         MyUdpMessage message = new MyUdpMessage();
 *         message.setByteBuffer(ByteBuffer.wrap(bytes), 0);
 *         // packet and message are now two different views of the same data. 
 *         while (true) {
 *             _socket.receive(packet);
 *             ... // Process message fields directly.
 *             packet.setLength(bytes.length); // Reset length to buffer's length.
 *         }
 *     }</pre></p> 
 * <p> It is relatively easy to map instances of this class to any physical
 *     address using
 *     <a href="http://java.sun.com/docs/books/tutorial/native1.1/index.html">
 *     JNI</a>. Here is an example:<pre>
 *     import java.nio.ByteBuffer;
 *     class Clock extends Struct { // Hardware clock mapped to memory.
 *         Unsigned16 seconds  = new Unsigned16(5); // unsigned short seconds:5
 *         Unsigned16 minutes  = new Unsigned16(5); // unsigned short minutes:5
 *         Unsigned16 hours    = new Unsigned16(4); // unsigned short hours:4
 *         Clock() {
 *             setByteBuffer(Clock.nativeBuffer(), 0);
 *         }
 *         private static native ByteBuffer nativeBuffer();
 *     }</pre>
 *     Below is the <code>nativeBuffer()</code> implementation
 *     (<code>Clock.c</code>):<pre>
 *     #include &lt;jni.h&gt;
 *     #include "Clock.h" // Generated using javah
 *     JNIEXPORT jobject JNICALL Java_Clock_nativeBuffer (JNIEnv *env, jclass) {
 *         return (*env)->NewDirectByteBuffer(env, clock_address, buffer_size)
 *     }</pre></p>
 * <p> Bit-fields are supported (see <code>Clock</code> example above).
 *     Bit-fields allocation order is defined by the Struct {@link #byteOrder}
 *     return value (leftmost bit to rightmost bit if
 *     <code>BIG_ENDIAN</code> and rightmost bit to leftmost bit if
 *      <code>LITTLE_ENDIAN</code>).
 *     Unless the Struct {@link #isPacked packing} directive is overriden,
 *     bit-fields cannot straddle the storage-unit boundary as defined by their
 *     base type (padding is inserted at the end of the first bit-field
 *     and the second bit-field is put into the next storage unit).</p>
 * <p> Finally, it is possible to {@link #setByteBuffer change} the {@link 
 *     ByteBuffer} or the {@link Struct}'s position in the {@link ByteBuffer}
 *     to allow for a single {@link Struct} object to encode/decode multiple
 *     memory mapped instances.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 20, 2004
 */
public class Struct {

    /**
     * Holds the outer struct during construction (if any).
     */
    private static final LocalContext.Variable OUTER = new LocalContext.Variable();

    /**
     * Holds the inner struct of this struct.
     */
    private final FastList _inners = new FastList();

    /**
     * Holds the byte buffer backing the struct.
     */
    private ByteBuffer _byteBuffer;

    /**
     * Holds the position of this struct within the byte buffer.
     */
    private int _byteBufferPosition;

    /**
     * Holds the number of bits currently used (for size calculation).
     */
    private int _bitsUsed;

    /**
     * Holds this struct alignment (largest alignment of its members).
     */
    private int _alignment;

    /**
     * Holds the current bit index position (during construction).
     */
    private int _bitIndex;

    /**
     * Indicates if the index has to be reset for each new field.
     */
    private boolean _resetIndex;

    /**
     * Holds the outer struct if any.
     */
    private final Struct _outer;

    /**
     * Holds the offset of this struct in the outer struct if any.
     */
    private int _outerOffset;

    /**
     * Default constructor.
     */
    public Struct() {
        _resetIndex = (this instanceof Union);
        _outer = (Struct) OUTER.getValue();
    }

    /**
     * Returns the size in bytes of this {@link Struct}. The size includes
     * tail padding to satisfy the {@link Struct} alignment requirement
     * (defined by the largest alignment of its {@link Member members}).
     *
     * @return the C/C++ <code>sizeof(this)</code>.
     */
    public final int size() {
        int nbrOfBytes = (_bitsUsed + 7) >> 3;
        return ((nbrOfBytes % _alignment) == 0) ? nbrOfBytes : // Already aligned or packed.
                nbrOfBytes + _alignment - (nbrOfBytes % _alignment); // Tail padding.
    }

    /**
     * Returns the {@link ByteBuffer} for this {@link Struct}.
     * This method will allocate a new buffer if none has been set.
     *
     * <p> Changes to the buffer's content are visible in this {@link Struct},
     *     and vice versa.</p>
     * <p> The buffer of an inner {@link Struct} is the same as its parent
     *     {@link Struct}.</p>
     * <p> The position of a {@link Struct.Member struct's member} within the 
     *     byte buffer is given by {@link Struct.Member#position
     *     member.position()}</p>
     *
     * @return the current byte buffer or a new direct buffer if none set.
     * @see #setByteBuffer
     */
    public final ByteBuffer getByteBuffer() {
        return (_byteBuffer != null) ? _byteBuffer : newBuffer();
    }

    private ByteBuffer newBuffer() {
        if (_outer == null) { // Top struct.
            int size = size();
            // Covers misaligned 64 bits access when packed.
            int capacity = isPacked() ? (((size & 0x7) == 0) ? size : size + 8
                    - (size & 0x7)) : size;
            ByteBuffer bf = ByteBuffer.allocateDirect(capacity);
            bf.order(byteOrder());
            setByteBuffer(bf, 0);
            return _byteBuffer;
        } else {
            return _outer.newBuffer();
        }
    }

    /**
     * Sets the current {@link ByteBuffer} for this {@link Struct}.
     * The specified byte buffer can be mapped to memory for direct memory
     * access or can wrap a shared byte array for I/O purpose 
     * (e.g. <code>DatagramPacket</code>).
     *
     * @param byteBuffer the new byte buffer.
     * @param position the position of this struct in the specified byte buffer. 
     */
    public final void setByteBuffer(ByteBuffer byteBuffer, int position) {
        _byteBuffer = byteBuffer;
        _byteBufferPosition = position;
        // Changes bytebuffer for inner structs.
        for (Iterator it = _inners.fastIterator(); it.hasNext();) {
            Struct inner = (Struct) it.next();
            inner.setByteBuffer(byteBuffer, position + inner._outerOffset);
        }
    }

    /**
     * Returns the position of this struct within the 
     * {@link #getByteBuffer byte buffer}.
     *
     * @return the absolute position of this struct in the byte buffer. 
     */
    public final int getByteBufferPosition() {
        return _byteBufferPosition;
    }

    /**
     * Returns this {@link Struct} address. This method allows for 
     * {@link Struct} to be referenced (e.g. pointer) from other {@link Struct}.
     *
     * @return the struct memory address.
     * @throws UnsupportedOperationException if the struct's buffer is not 
     *         a direct buffer.
     * @see    Reference32
     * @see    Reference64
     */
    public final long address() {
        ByteBuffer thisBuffer = this.getByteBuffer();
        if (ADDRESS_METHOD != null) {
            Long start = (Long) ADDRESS_METHOD.invoke(thisBuffer);
            return start.longValue() + _byteBufferPosition;
        } else {
            throw new UnsupportedOperationException(
                    "Operation not supported for " + thisBuffer.getClass());
        }
    }

    private static final Reflection.Method ADDRESS_METHOD = Reflection
            .getMethod("sun.nio.ch.DirectBuffer.address()");

    /**
     * Returns the <code>String</code> representation of this {@link Struct}
     * in the form of its constituing bytes (hexadecimal). For example:<pre>
     *     public static class Student extends Struct {
     *         Utf8String name  = new Utf8String(16);
     *         Unsigned16 year  = new Unsigned16();
     *         Float32    grade = new Float32();
     *     }
     *     Student student = new Student();
     *     student.name.set("John Doe");
     *     student.year.set(2003);
     *     student.grade.set(12.5f);
     *     System.out.println(student);
     *
     *     4a 6f 68 6e 20 44 6f 65 00 00 00 00 00 00 00 00
     *     07 d3 00 00 41 48 00 00</pre>
     *
     * @return a hexadecimal representation of the bytes content for this
     *         {@link Struct}.
     */
    public String toString() {
        try {
            TextBuilder chars = TextBuilder.newInstance();
            final int size = size();
            final ByteBuffer buffer = getByteBuffer();
            for (int i = 0; i < size; i++) {
                int b = buffer.get(i + _byteBufferPosition) & 0xFF;
                if (b < 0x10) {
                    // Ensures 2 digits per byte for alignment purpose.
                    chars.append('0');
                }
                TypeFormat.format(b, 16, chars);
                if ((i & 0xF) == 0xF) {
                    // 16 bytes per line.
                    chars.append('\n');
                } else {
                    chars.append(' ');
                }
            }
            return chars.toString();
        } catch (IOException e) {
            throw new JavolutionError(e);
        }
    }

    ///////////////////
    // CONFIGURATION //
    ///////////////////

    /**
     * Returns the byte order for this {@link Struct}. The byte order is
     * inherited by inner structs. Sub-classes may change the byte order
     * by overriding this method. For example:<pre>
     * public class TopStruct extends Struct {
     *     ... // Members initialization.
     *     public ByteOrder byteOrder() {
     *         // TopStruct and its inner structs use hardware byte order.
     *         return ByteOrder.nativeOrder();
     *    }
     * }}</pre></p></p>
     *
     * @return the byte order when reading/writing multibyte values
     *         (default: network byte order, <code>BIG_ENDIAN</code>).
     */
    public ByteOrder byteOrder() {
        if (_outer != null) {
            return _outer.byteOrder();
        } else {
            return ByteOrder.BIG_ENDIAN;
        }
    }

    /**
     * Indicates if this {@link Struct} is packed.
     * By default, {@link Member} of a {@link Struct} are aligned on the
     * boundary corresponding to the member's base type; padding is performed
     * if necessary. This directive is inherited by inner structs.
     * Sub-classes may change the packing directive by overriding this method.
     * For example:<pre>
     * public class TopStruct extends Struct {
     *     ... // Members initialization.
     *     public boolean isPacked() {
     *         // TopStruct and its inner structs are packed.
     *         return true;
     *     }
     * }}</pre></p></p>
     *
     * @return <code>true</code> if alignment requirements are ignored.
     *         <code>false</code> otherwise (default).
     */
    public boolean isPacked() {
        if (_outer != null) {
            return _outer.isPacked();
        } else {
            return false;
        }
    }

    /**
     * Returns a new instance of the specified member for this {@link Struct}.
     * Sub-classes may override this method for new members types to allow 
     * their use with {@link ArrayMember}. For example:<pre>
     * public class TextStruct extends Struct {
     *     public class Line extends Utf8String { // New member type.
     *          public Line() { 
     *              super(80); // 80 characters line.
     *          }
     *     }
     *     protected Object newInstance(Class memberClass) {
     *         return (memberClass == Line.class) ? 
     *             this.new Line() : super.newInstance(memberClass);
     *     }
     * }
     * ...
     * public class Page extends TextStruct { // 40 lines page.
     *     public final Unsigned16 number = new Unsigned16();     
     *     public final Line[] lines = (Line[]) new ArrayMember(new Line[40]).get();
     * }</pre>
     *
     * @param memberClass the class identifying the new instance to create. 
     * @return a new member instance for this struct.
     * @throws UnsupportedOperationException if the specified class is not 
     *         a predefined member class or it does not have a default 
     *         constructor.
     */
    protected Object newInstance(Class memberClass) {
        if (memberClass == BOOL) {
            return this.new Bool();
        } else if (memberClass == SIGNED_8) {
            return this.new Signed8();
        } else if (memberClass == UNSIGNED_8) {
            return this.new Unsigned8();
        } else if (memberClass == SIGNED_16) {
            return this.new Signed16();
        } else if (memberClass == UNSIGNED_16) {
            return this.new Unsigned16();
        } else if (memberClass == SIGNED_32) {
            return this.new Signed32();
        } else if (memberClass == UNSIGNED_32) {
            return this.new Unsigned32();
        } else if (memberClass == SIGNED_64) {
            return this.new Signed64();
        } else if (memberClass == FLOAT_32) {
            return this.new Float32();
        } else if (memberClass == FLOAT_64) {
            return this.new Float64();
        } else {
            throw new UnsupportedOperationException(memberClass
                    + " not recognized or invalid");
        }
    }
    // Cannot use class literal (.class) with CLDC 1.0
    private static final Struct STRUCT = new Struct();
    private static final Class MEMBER = STRUCT.new Member().getClass();  
    private static final Class BOOL = STRUCT.new Bool().getClass();  
    private static final Class SIGNED_8 = STRUCT.new Signed8().getClass();  
    private static final Class UNSIGNED_8 = STRUCT.new Unsigned8().getClass();  
    private static final Class SIGNED_16 = STRUCT.new Signed16().getClass();  
    private static final Class UNSIGNED_16 = STRUCT.new Unsigned16().getClass();  
    private static final Class SIGNED_32 = STRUCT.new Signed32().getClass();  
    private static final Class UNSIGNED_32 = STRUCT.new Unsigned32().getClass();  
    private static final Class SIGNED_64 = STRUCT.new Unsigned32().getClass();  
    private static final Class FLOAT_32 = STRUCT.new Float32().getClass();  
    private static final Class FLOAT_64 = STRUCT.new Float64().getClass();  

    /////////////
    // MEMBERS //
    /////////////

    /**
     * This inner class represents the base class for all {@link Struct}
     * members. It allows applications to define additional member types.
     * For example:<pre>
     *    public class MyStruct extends Struct {
     *        BitSet bits = new BitSet(256);
     *        ...
     *        public BitSet extends Member {
     *            public BitSet(int nbrBits) {
     *                super(1, (nbrBits+7)>>3);
     *            }
     *            public boolean get(int i) { ... }
     *            public void set(int i, boolean value) { ...}
     *        }
     *    }</pre>
     */
    protected class Member {

        /**
         * Holds the relative offset of this member within its struct.
         */
        private int _offset;

        /**
         * Default constructor (used internally for inner struct and bit-fields).
         */
        Member() {
        }

        /**
         * Base constructor for custom member types.
         *
         * @param  alignment the desired alignment in bytes.
         * @param  size the size of this member in bytes.
         */
        protected Member(int alignment, int size) {
            final int nbrOfBits = size << 3;
            updateIndexes(alignment, nbrOfBits, nbrOfBits);
        }

        /**
         * Returns the relative offset of this member within its struct.
         *
         * @return the relative offset in bytes.
         */
        public final int offset() {
            return _offset;
        }

        /**
         * Returns the absolute position of this member in the 
         * byte buffer or the member's {@link #offset offset} 
         * if the struct's byte buffer is not set.
         *
         * @return the absolute position in the byte buffer.
         */
        public final int position() {
            return _byteBufferPosition + _offset;
        }

        /**
         * Updates the {@link Struct}'s indexes and size during construction.
         *
         * @param  alignment  the desired alignment in bytes.
         * @param  nbrOfBits  the size in bits.
         * @param  capacity   the word size maximum capacity in bits
         *                    (equal to nbrOfBits for non-bitfields).
         * @throws IllegalArgumentException if
         *         <code>nbrOfBits &gt; capacity</code>
         */
        void updateIndexes(int alignment, int nbrOfBits, int capacity) {
            if (nbrOfBits > capacity) {
                throw new IllegalArgumentException("nbrOfBits: " + nbrOfBits
                        + " exceeds capacity: " + capacity);
            }

            // Resets index if union.
            if (_resetIndex) {
                _bitIndex = 0;
            }

            // Caculates offset based on alignment constraints.
            alignment = isPacked() ? 1 : alignment;
            _offset = (_bitIndex / (alignment << 3)) * alignment;

            // Calculates bits already used from the offset position.
            int usedBits = _bitIndex - (_offset << 3);

            // Checks if bits can be adjacents. 
            // A bit size of 0 forces realignment, ref. C/C++ Standard.
            if ((capacity < usedBits + nbrOfBits)
                    || ((nbrOfBits == 0) && (usedBits != 0))) {
                // Padding to next alignment boundary.
                _offset += alignment;
                _bitIndex = (_offset << 3) + nbrOfBits;
            } else { // Adjacent bits.
                _bitIndex += nbrOfBits;
            }

            // Updates bits used (for size calculation).
            if (_bitsUsed < _bitIndex) {
                _bitsUsed = _bitIndex;
            }

            // Updates Struct's alignment.
            if (Struct.this._alignment < alignment) {
                Struct.this._alignment = alignment;
            }
        }

        /**
         * Creates a new instance of the specified type.
         *
         * @param  type the class to instantiate.
         * @return an object of the specified type created using its
         *         default constructor.
         * @throws ClassCastException if classType is not derived from
         *         {@link Struct} or {@link Member}
         */
        Object newInstance(Class type) {
            if (MEMBER.isAssignableFrom(type)) {
                // Member.
                return Struct.this.newInstance(type);
            } else {
                Struct struct;
                LocalContext.enter();
                try {
                    OUTER.setValue(Struct.this);
                    struct = (Struct) type.newInstance();
                } catch (Throwable e) {
                    throw new JavolutionError(e);
                } finally {
                    LocalContext.exit();
                }
                final int bitSize = struct.size() << 3;
                updateIndexes(struct._alignment, bitSize, bitSize);
                struct._outerOffset = (_bitIndex - bitSize) >> 3;
                _inners.add(struct);
                return struct;
            }
        }
    }

    /**
     * This class represents an inner struct member.
     */
    public final class StructMember extends Member {

        /**
         * Holds the inner struct.
         */
        private final Struct _struct;

        /**
         * Creates a {@link Struct.StructMember} having an inner struct
         * of specified type.
         *
         * @param  structType the class of the inner struct.
         * @throws ClassCastException if the specified class is not derived from
         *         {@link Struct}.
         */
        public StructMember(Class structType) {
            _struct = (Struct) newInstance(structType);
        }

        /**
         * Returns the struct represented by this {@link Struct.StructMember}.
         *
         * @return the inner struct.
         */
        public Struct get() {
            return _struct;
        }
    }

    /**
     * This class represents an array member. Array's element can be
     * {@link Struct.Member} or {@link Struct}/{@link Union}. For example the
     *  following C code:<pre>
     *     struct Vertex {
     *         float x;
     *         float y;
     *     };
     *     struct Rectangle {
     *         int    colors[4];
     *         struct Vertex vertices[2][2];
     *         char   text[4][20];
     *     };</pre>
     *     would be represented by:<pre>
     *     public class Vertex extends Struct {
     *         public final Float32 x = new Float32();
     *         public final Float32 y = new Float32();
     *     }
     *     public class Rectangle extends Struct {
     *         public final Signed32[] colors = (Signed32[]) new ArrayMember(new Signed32[4]).get();
     *         public final Vertex[][] vertices = (Vertex[][]) new ArrayMember(new Vertex[2][2]).get();
     *     }</pre>
     *     Arrays elements are directly accessible:<pre>
     *     float x01 = myRectangle.vertices[0][1].x.get();
     *     myRectangle.colors[2].set(0xFF00FF);</pre>
     */
    public final class ArrayMember extends Member {

        /**
         * Holds the array.
         */
        private final Object _array;

        /**
         * Holds the component type.
         */
        private final Class _componentType;

        /**
         * Creates an {@link Struct.ArrayMember} for the specified single 
         * dimensional array.
         *
         * @param  array the actual array.
         * @throws ClassCastException if the componentType is not derived
         *         from {@link Struct.Member} or {@link Struct}.
         */
        public ArrayMember(Object[] array) {
            String arrayName = array.getClass().getName();
            String compName = arrayName.substring(2, arrayName.length() - 1);
            try {
                _componentType = Reflection.getClass(compName);
            } catch (ClassNotFoundException e) {
                throw new JavolutionError(e);
            }
            _array = array;
            if (_resetIndex) {
                _bitIndex = 0;
                _resetIndex = false; // Ensures elements are sequential.
                for (int i = 0; i < array.length; i++) {
                    array[i] = newInstance(_componentType);
                }
                _resetIndex = true;
            } else {
                for (int i = 0; i < array.length; i++) {
                    array[i] = newInstance(_componentType);
                }
            }
        }

        /**
         * Creates an {@link Struct.ArrayMember} for the specified 
         * 2-dimensional array.
         *
         * @param  array the actual array.
         * @throws ClassCastException if the componentType is not derived
         *         from {@link Struct.Member} or {@link Struct}.
         */
        public ArrayMember(Object[][] array) {
            String arrayName = array.getClass().getName();
            String compName = arrayName.substring(3, arrayName.length() - 1);
            try {
                _componentType = Reflection.getClass(compName);
            } catch (ClassNotFoundException e) {
                throw new JavolutionError(e);
            }
            _array = array;
            if (_resetIndex) {
                _bitIndex = 0;
                _resetIndex = false; // Ensures elements are sequential.
                for (int i = 0; i < array.length; i++) {
                    for (int j = 0; i < array[i].length; j++) {
                        array[i][j] = newInstance(_componentType);
                    }
                }
                _resetIndex = true;
            } else {
                for (int i = 0; i < array.length; i++) {
                    for (int j = 0; i < array[i].length; j++) {
                        array[i][j] = newInstance(_componentType);
                    }
                }
            }
        }

        /**
         * Creates an {@link Struct.ArrayMember} for the specified 
         * 3-dimensional array.
         *
         * @param  array the actual array.
         * @throws ClassCastException if the componentType is not derived
         *         from {@link Struct.Member} or {@link Struct}.
         */
        public ArrayMember(Object[][][] array) {
            String arrayName = array.getClass().getName();
            String compName = arrayName.substring(4, arrayName.length() - 1);
            try {
                _componentType = Reflection.getClass(compName);
            } catch (ClassNotFoundException e) {
                throw new JavolutionError(e);
            }
            _array = array;
            if (_resetIndex) {
                _bitIndex = 0;
                _resetIndex = false; // Ensures elements are sequential.
                for (int i = 0; i < array.length; i++) {
                    for (int j = 0; i < array[i].length; j++) {
                        for (int k = 0; k < array[i][j].length; k++) {
                            array[i][j][k] = newInstance(_componentType);
                        }
                    }
                }
                _resetIndex = true;
            } else {
                for (int i = 0; i < array.length; i++) {
                    for (int j = 0; i < array[i].length; j++) {
                        for (int k = 0; k < array[i][j].length; k++) {
                            array[i][j][k] = newInstance(_componentType);
                        }
                    }
                }
            }
        }

        /**
         * Returns the array represented by this {@link Struct.ArrayMember}.
         *
         * @return the array member.
         */
        public Object get() {
            return _array;
        }

        /**
         * Returns the component type of the array represented by this 
         * {@link Struct.ArrayMember}.
         *
         * @return the array's component type.
         */
        public Class componentType() {
            return _componentType;
        }
    }

    ///////////////////////
    // PREDEFINED FIELDS //
    ///////////////////////

    /**
     * This class represents a UTF-8 character string, null terminated
     * (for C/C++ compatibility)
     */
    public class Utf8String extends Member {
        private final Utf8ByteBufferWriter _writer = new Utf8ByteBufferWriter();

        private final Utf8ByteBufferReader _reader = new Utf8ByteBufferReader();

        private final char[] _chars;

        private final int _length;

        public Utf8String(int length) {
            super(1, length);
            _length = length; // Takes into account 0 terminator.
            _chars = new char[length];
        }

        public void set(String string) {
            final ByteBuffer buffer = getByteBuffer();
            synchronized (buffer) {
                try {
                    buffer.position(position());
                    _writer.setByteBuffer(buffer);
                    if (string.length() < _length) {
                        _writer.write(string);
                        _writer.write(0); // Marks end of string.
                    } else if (string.length() > _length) { // Truncates.
                        _writer.write(string.substring(0, _length));
                    } else { // Exact same length.
                        _writer.write(string);
                    }
                } catch (IOException e) {
                    throw new JavolutionError(e);
                }
            }
        }

        public String get() {
            final ByteBuffer buffer = getByteBuffer();
            synchronized (buffer) {
                try {
                    buffer.position(position());
                    _reader.setByteBuffer(buffer);
                    for (int i=0; i < _length;) {
                        char c = (char)_reader.read();
                        if (c == 0) { // Null terminator.
                            return new String(_chars, 0, i);
                        } else {
                            _chars[i++] = c;
                        }
                    }
                    return new String(_chars, 0, _length);
                } catch (IOException e) {
                    throw new JavolutionError(e);
                }
            }
        }
    }

    /**
     * This class represents a 8 bits boolean with <code>true</code> represented
     * by <code>1</code> and <code>false</code> represented by <code>0</code>.
     */
    public class Bool extends Member {
        private final int _mask;

        private final int _shift;

        public Bool() {
            this(8);
        }

        public Bool(int nbrOfBits) {
            updateIndexes(1, nbrOfBits, 8);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 8 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = ((1 << nbrOfBits) - 1) << _shift;
        }

        public boolean get() {
            return (getByteBuffer().get(position()) & _mask) != 0;
        }

        public void set(boolean value) {
            if (_mask == 0xFF) { // Non bit-field.
                getByteBuffer().put(position(), (byte) (value ? 1 : 0));
            } else { // Bit-field.
                int prevCleared = getByteBuffer().get(position()) & (~_mask);
                if (value) {
                    getByteBuffer().put(position(),
                            (byte) (prevCleared | (1 << _shift)));
                } else {
                    getByteBuffer().put(position(), (byte) (prevCleared));
                }
            }
        }
    }

    /**
     * This class represents a 8 bits signed integer.
     */
    public class Signed8 extends Member {
        private final int _mask;

        private final int _shift;

        private final int _signShift;

        public Signed8() {
            this(8);
        }

        public Signed8(int nbrOfBits) {
            updateIndexes(1, nbrOfBits, 8);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 8 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = ((1 << nbrOfBits) - 1) << _shift;
            _signShift = 32 - _shift - nbrOfBits;
        }

        public byte get() {
            if (_mask == 0xFF) { // Non bit-field.
                return getByteBuffer().get(position());
            } else { // Bit-field.
                int value = getByteBuffer().get(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return (byte) value;
            }
        }

        public void set(byte value) {
            if (_mask == 0xFF) { // Non bit-field.
                getByteBuffer().put(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().get(position()) & (~_mask);
                getByteBuffer().put(position(), (byte) (orMask | value));
            }
        }
    }

    /**
     * This class represents a 8 bits unsigned integer.
     */
    public class Unsigned8 extends Member {
        private final int _shift;

        private final int _mask;

        public Unsigned8() {
            this(8);
        }

        public Unsigned8(int nbrOfBits) {
            updateIndexes(1, nbrOfBits, 8);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 8 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = ((1 << nbrOfBits) - 1) << _shift;
        }

        public short get() {
            int value = getByteBuffer().get(position());
            return (short) ((value & _mask) >>> _shift);
        }

        public void set(short value) {
            if (_mask == 0xFF) { // Non bit-field.
                getByteBuffer().put(position(), (byte) value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().get(position()) & (~_mask);
                getByteBuffer().put(position(), (byte) (orMask | value));
            }
        }
    }

    /**
     * This class represents a 16 bits signed integer.
     */
    public class Signed16 extends Member {
        private final int _mask;

        private final int _shift;

        private final int _signShift;

        public Signed16() {
            this(16);
        }

        public Signed16(int nbrOfBits) {
            updateIndexes(2, nbrOfBits, 16);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 16 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = ((1 << nbrOfBits) - 1) << _shift;
            _signShift = 32 - _shift - nbrOfBits;
        }

        public short get() {
            if (_mask == 0xFFFF) { // Non bit-field.
                return getByteBuffer().getShort(position());
            } else { // Bit-field.
                int value = getByteBuffer().getShort(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return (short) value;
            }
        }

        public void set(short value) {
            if (_mask == 0xFFFF) { // Non bit-field.
                getByteBuffer().putShort(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().getShort(position()) & (~_mask);
                getByteBuffer().putShort(position(), (short) (orMask | value));
            }
        }
    }

    /**
     * This class represents a 16 bits unsigned integer.
     */
    public class Unsigned16 extends Member {
        private final int _shift;

        private final int _mask;

        public Unsigned16() {
            this(16);
        }

        public Unsigned16(int nbrOfBits) {
            updateIndexes(2, nbrOfBits, 16);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 16 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = ((1 << nbrOfBits) - 1) << _shift;
        }

        public int get() {
            int value = getByteBuffer().getShort(position());
            return (value & _mask) >>> _shift;
        }

        public void set(int value) {
            if (_mask == 0xFFFF) { // Non bit-field.
                getByteBuffer().putShort(position(), (short) value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().getShort(position()) & (~_mask);
                getByteBuffer().putShort(position(), (short) (orMask | value));
            }
        }
    }

    /**
     * This class represents a 32 bits signed integer.
     */
    public class Signed32 extends Member {
        private final int _mask;

        private final int _shift;

        private final int _signShift;

        public Signed32() {
            this(32);
        }

        public Signed32(int nbrOfBits) {
            updateIndexes(4, nbrOfBits, 32);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 32 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = (nbrOfBits == 32) ? 0xFFFFFFFF
                    : ((1 << nbrOfBits) - 1) << _shift;
            _signShift = 32 - _shift - nbrOfBits;
        }

        public int get() {
            if (_mask == 0xFFFFFFFF) { // Non bit-field.
                return getByteBuffer().getInt(position());
            } else { // Bit-field.
                int value = getByteBuffer().getInt(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return value;
            }
        }

        public void set(int value) {
            if (_mask == 0xFFFFFFFF) { // Non bit-field.
                getByteBuffer().putInt(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().getInt(position()) & (~_mask);
                getByteBuffer().putInt(position(), orMask | value);
            }
        }
    }

    /**
     * This class represents a 32 bits unsigned integer.
     */
    public class Unsigned32 extends Member {
        private final int _shift;

        private final long _mask;

        public Unsigned32() {
            this(32);
        }

        public Unsigned32(int nbrOfBits) {
            updateIndexes(4, nbrOfBits, 32);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 32 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = (nbrOfBits == 32) ? 0xFFFFFFFFl
                    : ((1l << nbrOfBits) - 1l) << _shift;
        }

        public long get() {
            int value = getByteBuffer().getInt(position());
            return (value & _mask) >>> _shift;
        }

        public void set(long value) {
            if (_mask == 0xFFFFFFFF) { // Non bit-field.
                getByteBuffer().putInt(position(), (int) value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().getInt(position())
                        & (~(int) _mask);
                getByteBuffer().putInt(position(), (int) (orMask | value));
            }
        }
    }

    /**
     * This class represents a 64 bits signed integer.
     */
    public class Signed64 extends Member {
        private final long _mask;

        private final int _shift;

        private final int _signShift;

        public Signed64() {
            this(64);
        }

        public Signed64(int nbrOfBits) {
            updateIndexes(8, nbrOfBits, 64);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 64 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = (nbrOfBits == 64) ? 0xFFFFFFFFFFFFFFFFl
                    : ((1l << nbrOfBits) - 1l) << _shift;
            _signShift = 64 - _shift - nbrOfBits;
        }

        public long get() {
            if (_mask == 0xFFFFFFFFFFFFFFFFl) { // Non bit-field.
                return getByteBuffer().getLong(position());
            } else { // Bit-field.
                long value = getByteBuffer().getLong(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return value;
            }
        }

        public void set(long value) {
            if (_mask == 0xFFFFFFFFFFFFFFFFl) { // Non bit-field.
                getByteBuffer().putLong(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                long orMask = getByteBuffer().getLong(position()) & (~_mask);
                getByteBuffer().putLong(position(), orMask | value);
            }
        }
    }

    /**
     * This class represents a 32 bits float (C/C++/Java <code>float</code>).
     */
    public class Float32 extends Member {
        public Float32() {
            super(4, 4);
        }
        /*@FLOATING_POINT@
        public void set(float value) {
            getByteBuffer().putFloat(position(), value);
        }

        public float get() {
            return getByteBuffer().getFloat(position());
        }
        /**/
    }

    /**
     * This class represents a 64 bits float (C/C++/Java <code>double</code>).
     */
    public class Float64 extends Member {
        public Float64() {
            super(8, 8);
        }
        /*@FLOATING_POINT@
        public void set(double value) {
            getByteBuffer().putDouble(position(), value);
        }
        public double get() {
            return getByteBuffer().getDouble(position());
        }
        /**/
    }

    /**
     * <p> This class represents a 32 bits reference (C/C++ pointer) to 
     *     a {@link Struct} object (other types may require a {@link Struct}
     *     wrapper).</p>
     * <p> Note: For references which can be externally modified, an application
     *           may want to check the {@link #isUpToDate up-to-date} status of
     *           the reference. For out-of-date references, a new {@link Struct}
     *           can be created at the address specified by {@link #value} 
     *           (using JNI) and then {@link #set set} to the reference.</p>
     */
    public class Reference32 extends Member {
        private final Class _structClass;

        private Struct _struct;

        public Reference32(Class structClass) {
            super(4, 4);
            _structClass = structClass;
        }

        public void set(Struct struct) {
            if (_structClass.isInstance(struct)) {
                getByteBuffer().putInt(position(), (int) struct.address());
            } else if (struct == null) {
                getByteBuffer().putInt(position(), 0);
            } else {
                throw new IllegalArgumentException("struct: Is an instance of "
                        + struct.getClass() + ", instance of " + _structClass
                        + " expected");
            }
            _struct = struct;
        }

        public Struct get() {
            return _struct;
        }

        public int value() {
            return getByteBuffer().getInt(position());
        }

        public boolean isUpToDate() {
            if (_struct != null) {
                return getByteBuffer().getInt(position()) == (int) _struct
                        .address();
            } else {
                return getByteBuffer().getInt(position()) == 0;
            }
        }
    }

    /**
     * <p> This class represents a 64 bits reference (C/C++ pointer) to 
     *     a {@link Struct} object (other types may require a {@link Struct}
     *     wrapper).</p>
     * <p> Note: For references which can be externally modified, an application
     *           may want to check the {@link #isUpToDate up-to-date} status of
     *           the reference. For out-of-date references, a new {@link Struct}
     *           can be created at the address specified by {@link #value} 
     *           (using JNI) and then {@link #set set} to the reference.</p>
     */
    public class Reference64 extends Member {
        private final Class _structClass;

        private Struct _struct;

        public Reference64(Class structClass) {
            super(8, 8);
            _structClass = structClass;
        }

        public void set(Struct struct) {
            if (_structClass.isInstance(struct)) {
                getByteBuffer().putLong(position(), struct.address());
            } else if (struct == null) {
                getByteBuffer().putLong(position(), 0L);
            } else {
                throw new IllegalArgumentException("struct: Is an instance of "
                        + struct.getClass() + ", instance of " + _structClass
                        + " expected");
            }
            _struct = struct;
        }

        public Struct get() {
            return _struct;
        }

        public long value() {
            return getByteBuffer().getLong(position());
        }

        public boolean isUpToDate() {
            if (_struct != null) {
                return getByteBuffer().getLong(position()) == _struct.address();
            } else {
                return getByteBuffer().getLong(position()) == 0L;
            }
        }
    }

    /**
     * This class represents a 8 bits {@link Enum}.
     */
    public class Enum8 extends Member {
        private final int _mask;

        private final int _shift;

        private final int _signShift;

        private final List _enumValues;

        public Enum8(List enumValues) {
            this(enumValues, 8);
        }

        public Enum8(List enumValues, int nbrOfBits) {
            _enumValues = enumValues;
            updateIndexes(1, nbrOfBits, 8);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 8 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = ((1 << nbrOfBits) - 1) << _shift;
            _signShift = 32 - _shift - nbrOfBits;
        }

        public Enum get() {
            if (_mask == 0xFF) { // Non bit-field.
                return (Enum) _enumValues.get(getByteBuffer().get(position()));
            } else { // Bit-field.
                int value = getByteBuffer().get(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return (Enum) _enumValues.get(value);
            }
        }

        public void set(Enum e) {
            int index = e.ordinal();
            if (_enumValues.get(index) != e) {
                throw new IllegalArgumentException(
                        "enum: "
                                + e
                                + ", ordinal value does not reflect enum values position");
            }
            byte value = (byte) index;
            if (_mask == 0xFF) { // Non bit-field.
                getByteBuffer().put(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().get(position()) & (~_mask);
                getByteBuffer().put(position(), (byte) (orMask | value));
            }
        }
    }

    /**
     * This class represents a 16 bits {@link Enum}.
     */
    public class Enum16 extends Member {
        private final int _mask;

        private final int _shift;

        private final int _signShift;

        private final List _enumValues;

        public Enum16(List enumValues) {
            this(enumValues, 16);
        }

        public Enum16(List enumValues, int nbrOfBits) {
            _enumValues = enumValues;
            updateIndexes(2, nbrOfBits, 16);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 16 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = ((1 << nbrOfBits) - 1) << _shift;
            _signShift = 32 - _shift - nbrOfBits;
        }

        public Enum get() {
            if (_mask == 0xFFFF) { // Non bit-field.
                return (Enum) _enumValues.get(getByteBuffer().getShort(
                        position()));
            } else { // Bit-field.
                int value = getByteBuffer().getShort(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return (Enum) _enumValues.get(value);
            }
        }

        public void set(Enum e) {
            int index = e.ordinal();
            if (_enumValues.get(index) != e) {
                throw new IllegalArgumentException(
                        "enum: "
                                + e
                                + ", ordinal value does not reflect enum values position");
            }
            short value = (short) index;
            if (_mask == 0xFFFF) { // Non bit-field.
                getByteBuffer().putShort(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().getShort(position()) & (~_mask);
                getByteBuffer().putShort(position(), (short) (orMask | value));
            }
        }
    }

    /**
     * This class represents a 32 bits {@link Enum}.
     */
    public class Enum32 extends Member {
        private final int _mask;

        private final int _shift;

        private final int _signShift;

        private final List _enumValues;

        public Enum32(List enumValues) {
            this(enumValues, 32);
        }

        public Enum32(List enumValues, int nbrOfBits) {
            _enumValues = enumValues;
            updateIndexes(4, nbrOfBits, 32);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 32 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = (nbrOfBits == 32) ? 0xFFFFFFFF
                    : ((1 << nbrOfBits) - 1) << _shift;
            _signShift = 32 - _shift - nbrOfBits;
        }

        public Enum get() {
            if (_mask == 0xFFFFFFFF) { // Non bit-field.
                return (Enum) _enumValues.get(getByteBuffer()
                        .getInt(position()));
            } else { // Bit-field.
                int value = getByteBuffer().getInt(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return (Enum) _enumValues.get(value);
            }
        }

        public void set(Enum e) {
            int index = e.ordinal();
            if (_enumValues.get(index) != e) {
                throw new IllegalArgumentException(
                        "enum: "
                                + e
                                + ", ordinal value does not reflect enum values position");
            }
            int value = index;
            if (_mask == 0xFFFFFFFF) { // Non bit-field.
                getByteBuffer().putInt(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                int orMask = getByteBuffer().getInt(position()) & (~_mask);
                getByteBuffer().putInt(position(), orMask | value);
            }
        }
    }

    /**
     * This class represents a 64 bits {@link Enum}.
     */
    public class Enum64 extends Member {
        private final long _mask;

        private final int _shift;

        private final int _signShift;

        private final List _enumValues;

        public Enum64(List enumValues) {
            this(enumValues, 64);
        }

        public Enum64(List enumValues, int nbrOfBits) {
            _enumValues = enumValues;
            updateIndexes(8, nbrOfBits, 64);
            final int startBit = offset() << 3;
            _shift = (byteOrder() == ByteOrder.BIG_ENDIAN) ? 64 - _bitIndex
                    + startBit : _bitIndex - startBit - nbrOfBits;
            _mask = (nbrOfBits == 64) ? 0xFFFFFFFFFFFFFFFFl
                    : ((1l << nbrOfBits) - 1l) << _shift;
            _signShift = 64 - _shift - nbrOfBits;
        }

        public Enum get() {
            if (_mask == 0xFFFFFFFFFFFFFFFFl) { // Non bit-field.
                return (Enum) _enumValues.get((int) getByteBuffer().getLong(
                        position()));
            } else { // Bit-field.
                long value = getByteBuffer().getLong(position());
                value &= _mask;
                value <<= _signShift;
                value >>= _signShift + _shift; // Keeps sign.
                return (Enum) _enumValues.get((int) value);
            }
        }

        public void set(Enum e) {
            int index = e.ordinal();
            if (_enumValues.get(index) != e) {
                throw new IllegalArgumentException(
                        "enum: "
                                + e
                                + ", ordinal value does not reflect enum values position");
            }
            long value = index;
            if (_mask == 0xFFFFFFFFFFFFFFFFl) { // Non bit-field.
                getByteBuffer().putLong(position(), value);
            } else { // Bit-field.
                value <<= _shift;
                value &= _mask;
                long orMask = getByteBuffer().getLong(position()) & (~_mask);
                getByteBuffer().putLong(position(), orMask | value);
            }
        }
    }
}