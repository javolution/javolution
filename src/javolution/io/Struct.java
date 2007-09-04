/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;

import j2me.lang.UnsupportedOperationException;
import j2me.nio.ByteBuffer;
import j2me.nio.ByteOrder;
import j2me.util.List;
import javolution.JavolutionError;
import javolution.lang.Configurable;
import javolution.lang.Enum;
import javolution.lang.MathLib;
import javolution.lang.Reflection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
 *     using simple text macros. Here is an example of C struct:<code><pre>
 *     struct Date {
 *         unsigned short year;
 *         unsigned byte month;
 *         unsigned byte day;
 *     };
 *     struct Student {
 *         char        name[64];
 *         struct Date birth;
 *         float       grades[10];
 *         Student*    next;
 *     };</pre></code>
 *     and here is the Java equivalent using this class:[code]
 *     public static class Date extends Struct {
 *         public final Unsigned16 year = new Unsigned16();
 *         public final Unsigned8 month = new Unsigned8();
 *         public final Unsigned8 day   = new Unsigned8();
 *     }
 *     public static class Student extends Struct {
 *         public final Utf8String  name   = new UTF8String(64);
 *         public final Date        birth  = inner(new Date());
 *         public final Float32[]   grades = array(new Float32[10]);
 *         public final Reference32<Student> next =  new Reference32<Student>();
 *     }[/code]
 *     Struct's members are directly accessible:[code]
 *     Student student = new Student();
 *     student.name.set("John Doe"); // Null terminated (C compatible)
 *     int age = 2003 - student.birth.year.get();
 *     student.grades[2].set(12.5f);
 *     student = student.next.get();[/code]</p>
 * <p> Applications may also work with the raw {@link #getByteBuffer() bytes}
 *     directly. The following illustrate how {@link Struct} can be used to 
 *     decode/encode UDP messages directly:[code]
 *     class UDPMessage extends Struct {
 *          Unsigned16 xxx = new Unsigned16();
 *          ... 
 *     }
 *     public void run() {
 *         byte[] bytes = new byte[1024];
 *         DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
 *         UDPMessage message = new UDPMessage();
 *         message.setByteBuffer(ByteBuffer.wrap(bytes), 0);
 *         // packet and message are now two different views of the same data. 
 *         while (isListening) {
 *             multicastSocket.receive(packet);
 *             int xxx = message.xxx.get();
 *             ... // Process message fields directly.
 *         }
 *     }[/code]</p> 
 * <p> It is relatively easy to map instances of this class to any physical
 *     address using
 *     <a href="http://java.sun.com/docs/books/tutorial/native1.1/index.html">
 *     JNI</a>. Here is an example:[code]
 *     import java.nio.ByteBuffer;
 *     class Clock extends Struct { // Hardware clock mapped to memory.
 *         Unsigned16 seconds  = new Unsigned16(5); // unsigned short seconds:5
 *         Unsigned16 minutes  = new Unsigned16(5); // unsigned short minutes:5
 *         Unsigned16 hours    = new Unsigned16(4); // unsigned short hours:4
 *         Clock() {
 *             setByteBuffer(Clock.nativeBuffer(), 0);
 *         }
 *         private static native ByteBuffer nativeBuffer();
 *     }[/code]
 *     Below is the <code>nativeBuffer()</code> implementation
 *     (<code>Clock.c</code>):[code]
 *     #include <jni.h>
 *     #include "Clock.h" // Generated using javah
 *     JNIEXPORT jobject JNICALL Java_Clock_nativeBuffer (JNIEnv *env, jclass) {
 *         return (*env)->NewDirectByteBuffer(env, clock_address, buffer_size)
 *     }[/code]</p>
 * <p> Bit-fields are supported (see <code>Clock</code> example above).
 *     Bit-fields allocation order is defined by the Struct {@link #byteOrder}
 *     return value (leftmost bit to rightmost bit if
 *     <code>BIG_ENDIAN</code> and rightmost bit to leftmost bit if
 *      <code>LITTLE_ENDIAN</code>).
 *     Unless the Struct {@link #isPacked packing} directive is overriden,
 *     bit-fields cannot straddle the storage-unit boundary as defined by their
 *     base type (padding is inserted at the end of the first bit-field
 *     and the second bit-field is put into the next storage unit).</p>
 * <p> Finally, it is possible to change the {@link #setByteBuffer ByteBuffer} 
 *     and/or the Struct {@link #setByteBufferPosition position} in its 
 *     <code>ByteBuffer</code> to allow for a single {@link Struct} object to
 *     encode/decode multiple memory mapped instances.</p>
 *     
 * <p><i>Note: Because Struct/Union are basically wrappers around 
 *             <code>java.nio.ByteBuffer</code>, tutorials/usages for 
 *             the Java NIO package are directly applicable to Struct.</i></p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 5.1, July 17, 2007
 */
public class Struct {

    /**
     * Configurable holding the maximum alignment in bytes 
     * (default <code>4</code>).
     */
    public static final Configurable/*<Integer>*/MAXIMUM_ALIGNMENT 
        = new Configurable(new Integer(4));

    /**
     * Holds the outer struct if any.
     */
    private Struct _outer;

    /**
     * Holds the byte buffer backing the struct (top struct).
     */
    private ByteBuffer _byteBuffer;

    /**
     * Holds the offset of this struct relative to the outer struct or
     * to the byte buffer if there is no outer.
     */
    private int _outerOffset;

    /**
     * Holds the number of bits currently used (for size calculation).
     */
    private int _bitsUsed;

    /**
     * Holds this struct alignment (largest alignment of its members).
     */
    private int _alignment = 1;

    /**
     * Holds the current bit index position (during construction).
     */
    private int _bitIndex;

    /**
     * Indicates if the index has to be reset for each new field.
     */
    private boolean _resetIndex;

    /**
     * Holds bytes array for Stream I/O when byteBuffer has no intrinsic array.
     */
    private byte[] _bytes;

    /**
     * Default constructor.
     */
    public Struct() {
        _resetIndex = isUnion();
    }

    /**
     * Returns the size in bytes of this struct. The size includes
     * tail padding to satisfy the struct alignment requirement
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
     * Returns the byte buffer for this struct. This method will allocate 
     * a new <b>direct</b> buffer if none has been set.
     *
     * <p> Changes to the buffer's content are visible in this struct,
     *     and vice versa.</p>
     * <p> The buffer of an inner struct is the same as its parent struct.</p>
     * <p> The position of a {@link Struct.Member struct's member} within the 
     *     byte buffer is given by {@link Struct.Member#position
     *     member.position()}</p>
     *
     * @return the current byte buffer or a new direct buffer if none set.
     * @see #setByteBuffer
     */
    public final ByteBuffer getByteBuffer() {
        if (_outer != null)
            return _outer.getByteBuffer();
        return (_byteBuffer != null) ? _byteBuffer : newBuffer();
    }

    private synchronized ByteBuffer newBuffer() {
        if (_byteBuffer != null)
            return _byteBuffer; // Synchronized check.
        int size = size();
        // Covers misaligned 64 bits access when packed.
        int capacity = isPacked() ? (((size & 0x7) == 0) ? size : size + 8
                - (size & 0x7)) : size;
        ByteBuffer bf = ByteBuffer.allocateDirect(capacity);
        bf.order(byteOrder());
        setByteBuffer(bf, 0);
        return _byteBuffer;
    }

    /**
     * Sets the current byte buffer for this struct.
     * The specified byte buffer can be mapped to memory for direct memory
     * access or can wrap a shared byte array for I/O purpose 
     * (e.g. <code>DatagramPacket</code>).
     *
     * @param byteBuffer the new byte buffer.
     * @param position the position of this struct in the specified byte buffer.
     * @return <code>this</code>
     * @throws IllegalArgumentException if the specified byteBuffer has a 
     *         different byte order than this struct. 
     * @throws UnsupportedOperationException if this struct is an inner struct.
     * @see #byteOrder() 
     */
    public final Struct setByteBuffer(ByteBuffer byteBuffer, int position) {
        if (byteBuffer.order() != byteOrder())
            throw new IllegalArgumentException(
                    "The byte order of the specified byte buffer"
                            + " is different from this struct byte order");
        if (_outer != null)
            throw new UnsupportedOperationException(
                    "Inner struct byte buffer is inherited from outer");
        _byteBuffer = byteBuffer;
        _outerOffset = position;
        return this;
    }

    /**
     * Sets the position of this struct within its byte buffer.
     *
     * @param position the position of this struct in its byte buffer.
     * @return <code>this</code>
     * @throws UnsupportedOperationException if this struct is an inner struct.
     */
    public final Struct setByteBufferPosition(int position) {
        return setByteBuffer(this.getByteBuffer(), position);
    }

    /**
     * Returns the absolute position of this struct within its associated 
     * {@link #getByteBuffer byte buffer}.
     *
     * @return the absolute position of this struct in the byte buffer. 
     */
    public final int getByteBufferPosition() {
        return (_outer != null) ? _outer.getByteBufferPosition() + _outerOffset
                : _outerOffset;
    }

    /**
     * Reads this struct from the specified input stream  
     * (convenience method when using Stream I/O). For better performance,
     * use of Block I/O (e.g. <code>java.nio.channels.*</code>) is recommended.
     *
     * @param in the input stream being read from.
     * @return the number of bytes read (typically the {@link #size() size}
     *         of this struct.
     * @throws IOException if an I/O error occurs.
     */
    public int read(InputStream in) throws IOException {
        ByteBuffer buffer = getByteBuffer();
        if (buffer.hasArray()) {
            int offset = buffer.arrayOffset() + getByteBufferPosition();
            return in.read(buffer.array(), offset, size());
        } else {
            synchronized (buffer) {
                if (_bytes == null) {
                    _bytes = new byte[size()];
                }
                int bytesRead = in.read(_bytes);
                buffer.position(getByteBufferPosition());
                buffer.put(_bytes);
                return bytesRead;
            }
        }
    }

    /**
     * Writes this struct to the specified output stream  
     * (convenience method when using Stream I/O). For better performance,
     * use of Block I/O (e.g. <code>java.nio.channels.*</code>) is recommended.
     *
     * @param out the output stream to write to.
     * @throws IOException if an I/O error occurs.
     */
    public void write(OutputStream out) throws IOException {
        ByteBuffer buffer = getByteBuffer();
        if (buffer.hasArray()) {
            int offset = buffer.arrayOffset() + getByteBufferPosition();
            out.write(buffer.array(), offset, size());
        } else {
            synchronized (buffer) {
                if (_bytes == null) {
                    _bytes = new byte[size()];
                }
                buffer.position(getByteBufferPosition());
                buffer.get(_bytes);
                out.write(_bytes);
            }
        }
    }

    /**
     * Returns this struct address. This method allows for structs 
     * to be referenced (e.g. pointer) from other structs.
     *
     * @return the struct memory address.
     * @throws UnsupportedOperationException if the struct buffer is not 
     *         a direct buffer.
     * @see    Reference32
     * @see    Reference64
     */
    public final long address() {
        ByteBuffer thisBuffer = this.getByteBuffer();
        if (ADDRESS_METHOD != null) {
            Long start = (Long) ADDRESS_METHOD.invoke(thisBuffer);
            return start.longValue() + getByteBufferPosition();
        } else {
            throw new UnsupportedOperationException(
                    "Operation not supported for " + thisBuffer.getClass());
        }
    }

    private static final Reflection.Method ADDRESS_METHOD = Reflection
            .getMethod("sun.nio.ch.DirectBuffer.address()");

    /**
     * Returns the <code>String</code> representation of this struct
     * in the form of its constituing bytes (hexadecimal). For example:[code]
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
     *     4A 6F 68 6E 20 44 6F 65 00 00 00 00 00 00 00 00
     *     07 D3 00 00 41 48 00 00[/code]
     *
     * @return a hexadecimal representation of the bytes content for this 
     *         struct.
     */
    public String toString() {
        final int size = size();
        StringBuffer sb = new StringBuffer(size * 3);
        final ByteBuffer buffer = getByteBuffer();
        final int start = getByteBufferPosition();
        for (int i = 0; i < size; i++) {
            int b = buffer.get(start + i) & 0xFF;
            sb.append(HEXA[b >> 4]);
            sb.append(HEXA[b & 0xF]);
            sb.append(((i & 0xF) == 0xF) ? '\n' : ' ');
        }
        return sb.toString();
    }

    private static final char[] HEXA = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    ///////////////////
    // CONFIGURATION //
    ///////////////////

    /**
     * Indicates if this struct's members are mapped to the same location
     * in memory (default <code>false</code>). This method is useful for
     * applications extending {@link Struct} with new member types in order to 
     * create unions from these new structs. For example:[code]
     * public abstract class FortranStruct extends Struct {
     *     public class FortranString extends Member {...}
     *     protected FortranString[] array(FortranString[] array, int stringLength) { ... }
     * }
     * public abstract class FortranUnion extends FortranStruct {
     *     // Inherits new members and methods.
     *     public final isUnion() {
     *         return true;
     *     }
     * }[/code]
     *
     * @return <code>true</code> if this struct's members are mapped to 
     *         to the same location in memory; <code>false</code> 
     *         otherwise.
     * @see Union
     */
    public boolean isUnion() {
        return false;
    }

    /**
     * Returns the byte order for this struct (configurable). 
     * The byte order is inherited by inner structs. Sub-classes may change 
     * the byte order by overriding this method. For example:[code]
     * public class TopStruct extends Struct {
     *     ... // Members initialization.
     *     public ByteOrder byteOrder() {
     *         // TopStruct and its inner structs use hardware byte order.
     *         return ByteOrder.nativeOrder();
     *    }
     * }}[/code]</p></p>
     *
     * @return the byte order when reading/writing multibyte values
     *         (default: network byte order, <code>BIG_ENDIAN</code>).
     */
    public ByteOrder byteOrder() {
        return (_outer != null) ? _outer.byteOrder() : ByteOrder.BIG_ENDIAN;
    }

    /**
     * Indicates if this struct is packed (configurable).
     * By default, {@link Member members} of a struct are aligned on the
     * boundary corresponding to the member base type; padding is performed
     * if necessary. This directive is inherited by inner structs.
     * Sub-classes may change the packing directive by overriding this method.
     * For example:[code]
     * public class TopStruct extends Struct {
     *     ... // Members initialization.
     *     public boolean isPacked() {
     *         // TopStruct and its inner structs are packed.
     *         return true;
     *     }
     * }}[/code]
     *
     * @return <code>true</code> if alignment requirements are ignored.
     *         <code>false</code> otherwise (default).
     */
    public boolean isPacked() {
        return (_outer != null) ? _outer.isPacked() : false;
    }

    /**
     * Defines the specified struct as inner of this struct.
     *
     * @param struct the inner struct.
     * @return the specified struct. 
     * @throws IllegalArgumentException if the specified struct is already 
     *         an inner struct.
     */
    protected/* <S extends Struct> S*/Struct inner(/*S*/Struct struct) {
        if (struct._outer != null)
            throw new IllegalArgumentException(
                    "struct: Already an inner struct");
        struct._outer = this;
        final int bitSize = struct.size() << 3;
        updateIndexes(struct._alignment, bitSize, bitSize);
        struct._outerOffset = (_bitIndex - bitSize) >> 3;
        return (/*S*/Struct) struct;
    }

    /**
     * Defines the specified array of structs as inner structs. 
     * The array is populated if necessary using the struct component
     * default constructor (which must be public).
     *
     * @param structs the struct array.
     * @return the specified struct array. 
     * @throws IllegalArgumentException if the specified array contains 
     *         inner structs.
     */
    protected/* <S extends Struct> S*/Struct[] array(/*S*/Struct[] structs) {
        Class structClass = null;
        boolean resetIndexSaved = _resetIndex;
        if (_resetIndex) {
            _bitIndex = 0;
            _resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < structs.length;) {
            /*S*/Struct struct = structs[i];
            if (struct == null) {
                try {
                    if (structClass == null) {
                        String arrayName = structs.getClass().getName();
                        String structName = arrayName.substring(2, arrayName
                                .length() - 1);
                        structClass = Reflection.getClass(structName);
                        if (structClass == null)
                            throw new JavolutionError("Struct class: "
                                    + structName + " not found");
                    }
                    struct = (/*S*/Struct) structClass.newInstance();
                } catch (Exception e) {
                    throw new JavolutionError(e);
                }
            }
            structs[i++] = inner(struct);
        }
        _resetIndex = resetIndexSaved;
        return (/*S*/Struct[]) structs;
    }

    /**
     * Defines the specified two-dimensional array of structs as inner
     * structs. The array is populated if necessary using the struct component 
     * default constructor (which must be public).
     *
     * @param structs the two dimensional struct array.
     * @return the specified struct array. 
     * @throws IllegalArgumentException if the specified array contains 
     *         inner structs.
     */
    protected/* <S extends Struct> S*/Struct[][] array(
    /*S*/Struct[][] structs) {
        boolean resetIndexSaved = _resetIndex;
        if (_resetIndex) {
            _bitIndex = 0;
            _resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < structs.length; i++) {
            array(structs[i]);
        }
        _resetIndex = resetIndexSaved;
        return (/*S*/Struct[][]) structs;
    }

    /**
     * Defines the specified three dimensional array of structs as inner 
     * structs. The array is populated if necessary using the struct component 
     * default constructor (which must be public).
     *
     * @param structs the three dimensional struct array.
     * @return the specified struct array.
     * @throws IllegalArgumentException if the specified array contains 
     *         inner structs.
     */
    protected/* <S extends Struct> S*/Struct[][][] array(
    /*S*/Struct[][][] structs) {
        boolean resetIndexSaved = _resetIndex;
        if (_resetIndex) {
            _bitIndex = 0;
            _resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < structs.length; i++) {
            array(structs[i]);
        }
        _resetIndex = resetIndexSaved;
        return (/*S*/Struct[][][]) structs;
    }

    /**
     * Defines the specified array member. For predefined members, 
     * the array is populated when empty; custom members should use 
     * literal (populated) arrays.
     *
     * @param  arrayMember the array member.
     * @return the specified array member.
     * @throws UnsupportedOperationException if the specified array 
     *         is empty and the member type is unknown. 
     */
    protected/* <M extends Member> M*/Member[] array(
    /*M*/Member[] arrayMember) {
        boolean resetIndexSaved = _resetIndex;
        if (_resetIndex) {
            _bitIndex = 0;
            _resetIndex = false; // Ensures the array elements are sequential.
        }
        if (BOOL.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Bool();
        } else if (SIGNED_8.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Signed8();
        } else if (UNSIGNED_8.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Unsigned8();
        } else if (SIGNED_16.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Signed16();
        } else if (UNSIGNED_16.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Unsigned16();
        } else if (SIGNED_32.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Signed32();
        } else if (UNSIGNED_32.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Unsigned32();
        } else if (SIGNED_64.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Signed64();
        } else if (FLOAT_32.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Float32();
        } else if (FLOAT_64.isInstance(arrayMember)) {
            for (int i = 0; i < arrayMember.length;)
                arrayMember[i++] = (/*M*/Member) this.new Float64();
        } else {
            throw new UnsupportedOperationException(
                    "Cannot create member elements, the arrayMember should "
                            + "contain the member instances instead of null");
        }
        _resetIndex = resetIndexSaved;
        return (/*M*/Member[]) arrayMember;
    }

    private static final Class BOOL = new Bool[0].getClass();

    private static final Class SIGNED_8 = new Signed8[0].getClass();

    private static final Class UNSIGNED_8 = new Unsigned8[0].getClass();

    private static final Class SIGNED_16 = new Signed16[0].getClass();

    private static final Class UNSIGNED_16 = new Unsigned16[0].getClass();

    private static final Class SIGNED_32 = new Signed32[0].getClass();

    private static final Class UNSIGNED_32 = new Unsigned32[0].getClass();

    private static final Class SIGNED_64 = new Signed64[0].getClass();

    private static final Class FLOAT_32 = new Float32[0].getClass();

    private static final Class FLOAT_64 = new Float64[0].getClass();

    /**
     * Defines the specified two-dimensional array member. For predefined
     * members, the array is populated when empty; custom members should use 
     * literal (populated) arrays.
     *
     * @param  arrayMember the two-dimensional array member.
     * @return the specified array member.
     * @throws UnsupportedOperationException if the specified array 
     *         is empty and the member type is unknown. 
     */
    protected/* <M extends Member> M*/Member[][] array(
    /*M*/Member[][] arrayMember) {
        boolean resetIndexSaved = _resetIndex;
        if (_resetIndex) {
            _bitIndex = 0;
            _resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < arrayMember.length; i++) {
            array(arrayMember[i]);
        }
        _resetIndex = resetIndexSaved;
        return (/*M*/Member[][]) arrayMember;
    }

    /**
     * Defines the specified three-dimensional array member. For predefined
     * members, the array is populated when empty; custom members should use 
     * literal (populated) arrays.
     *
     * @param  arrayMember the three-dimensional array member.
     * @return the specified array member.
     * @throws UnsupportedOperationException if the specified array 
     *         is empty and the member type is unknown. 
     */
    protected/* <M extends Member> M*/Member[][][] array(
    /*M*/Member[][][] arrayMember) {
        boolean resetIndexSaved = _resetIndex;
        if (_resetIndex) {
            _bitIndex = 0;
            _resetIndex = false; // Ensures the array elements are sequential.
        }
        for (int i = 0; i < arrayMember.length; i++) {
            array(arrayMember[i]);
        }
        _resetIndex = resetIndexSaved;
        return (/*M*/Member[][][]) arrayMember;
    }

    /**
     * Defines the specified array of UTF-8 strings, all strings having the 
     * specified length (convenience method).
     *
     * @param  array the string array.
     * @param stringLength the length of the string elements.
     * @return the specified string array.
     */
    protected UTF8String[] array(UTF8String[] array, int stringLength) {
        for (int i = 0; i < array.length; i++) {
            array[i] = new UTF8String(stringLength);
        }
        return array;
    }

    /**
     * Updates this struct indexes after adding a member with the 
     * specified constraints.
     *
     * @param  alignment  the desired alignment in bytes.
     * @param  nbrOfBits  the size in bits.
     * @param  capacity   the word size maximum capacity in bits
     *                    (equal to nbrOfBits for non-bitfields).
     * @return offset the offset of the member.
     * @throws IllegalArgumentException if
     *         <code>nbrOfBits &gt; capacity</code>
     */
    private int updateIndexes(int alignment, int nbrOfBits, int capacity) {
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
        int offset = (_bitIndex / (alignment << 3)) * alignment;

        // Calculates bits already used from the offset position.
        int usedBits = _bitIndex - (offset << 3);

        // Checks if bits can be adjacents. 
        // A bit size of 0 forces realignment, ref. C/C++ Standard.
        if ((capacity < usedBits + nbrOfBits)
                || ((nbrOfBits == 0) && (usedBits != 0))) {
            // Padding to next alignment boundary.
            offset += alignment;
            _bitIndex = (offset << 3) + nbrOfBits;
        } else { // Adjacent bits.
            _bitIndex += nbrOfBits;
        }

        // Updates bits used (for size calculation).
        if (_bitsUsed < _bitIndex) {
            _bitsUsed = _bitIndex;
        }

        // Updates Struct's alignment.
        if (_alignment < alignment) {
            _alignment = MathLib.min(alignment,
                    ((Integer) MAXIMUM_ALIGNMENT.get()).intValue());
        }
        return offset;
    }

    /////////////
    // MEMBERS //
    /////////////

    /**
     * This inner class represents the base class for all {@link Struct}
     * members. It allows applications to define additional member types.
     * For example:[code]
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
     *    }[/code]
     */
    protected class Member {

        /**
         * Holds the relative offset of this member within its struct.
         */
        private final int _offset;

        /**
         * Base constructor for custom member types.
         *
         * @param  alignment the desired alignment in bytes.
         * @param  size the size of this member in bytes.
         */
        protected Member(int alignment, int size) {
            final int nbrOfBits = size << 3;
            _offset = updateIndexes(alignment, nbrOfBits, nbrOfBits);
        }

        /**
         * Base constructor for predefined member types with bit fields.
         *
         * @param  alignment  the desired alignment in bytes.
         * @param  nbrOfBits  the size in bits.
         * @param  capacity   the word size maximum capacity in bits
         *                    (equal to nbrOfBits for non-bitfields).
         */
        Member(int alignment, int nbrOfBits, int capacity) {
            _offset = updateIndexes(alignment, nbrOfBits, capacity);
        }

        /**
         * Returns the outer {@link Struct struct} container.
         *
         * @return the outer struct.
         */
        public final Struct struct() {
            return Struct.this;
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
         * byte buffer.
         *
         * @return the byte buffer position.
         */
        public final int position() {
            return getByteBufferPosition() + _offset;
        }
    }

    ///////////////////////
    // PREDEFINED FIELDS //
    ///////////////////////

    /**
     * This class represents a UTF-8 character string, null terminated
     * (for C/C++ compatibility)
     */
    public class UTF8String extends Member {
        private final UTF8ByteBufferWriter _writer = new UTF8ByteBufferWriter();

        private final UTF8ByteBufferReader _reader = new UTF8ByteBufferReader();

        private final char[] _chars;

        private final int _length;

        public UTF8String(int length) {
            super(1, length);
            _length = length; // Takes into account 0 terminator.
            _chars = new char[length];
        }

        public void set(String string) {
            final ByteBuffer buffer = getByteBuffer();
            synchronized (buffer) {
                try {
                    buffer.position(position());
                    _writer.setOutput(buffer);
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
                } finally {
                    _writer.reset();
                }
            }
        }

        public String get() {
            final ByteBuffer buffer = getByteBuffer();
            synchronized (buffer) {
                try {
                    buffer.position(position());
                    _reader.setInput(buffer);
                    for (int i = 0; i < _length;) {
                        char c = (char) _reader.read();
                        if (c == 0) { // Null terminator.
                            return new String(_chars, 0, i);
                        } else {
                            _chars[i++] = c;
                        }
                    }
                    return new String(_chars, 0, _length);
                } catch (IOException e) {
                    throw new JavolutionError(e);
                } finally {
                    _reader.reset();
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
            super(1, nbrOfBits, 8);
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
            super(1, nbrOfBits, 8);
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
            super(1, nbrOfBits, 8);
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
            super(2, nbrOfBits, 16);
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
            super(2, nbrOfBits, 16);
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
            super(4, nbrOfBits, 32);
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
            super(4, nbrOfBits, 32);
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
            super(8, nbrOfBits, 64);
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
        /*@JVM-1.1+@
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
        /*@JVM-1.1+@
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
     *           the reference. For out-of-date references, a {@link Struct}
     *           can be created at the address specified by {@link #value} 
     *           (using JNI) and the reference {@link #set set} accordingly.</p>
     */
    public class Reference32/*<S extends Struct>*/extends Member {

        private/*S*/Struct _struct;

        public Reference32() {
            super(4, 4);
        }

        public void set(/*S*/Struct struct) {
            if (struct != null) {
                getByteBuffer().putInt(position(), (int) struct.address());
            } else {
                getByteBuffer().putInt(position(), 0);
            }
            _struct = struct;
        }

        public/*S*/Struct get() {
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
    public class Reference64/*<S extends Struct>*/extends Member {
        private/*S*/Struct _struct;

        public Reference64() {
            super(8, 8);
        }

        public void set(/*S*/Struct struct) {
            if (struct != null) {
                getByteBuffer().putLong(position(), struct.address());
            } else if (struct == null) {
                getByteBuffer().putLong(position(), 0L);
            }
            _struct = struct;
        }

        public/*S*/Struct get() {
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
            super(1, nbrOfBits, 8);
            _enumValues = enumValues;
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
            super(2, nbrOfBits, 16);
            _enumValues = enumValues;
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
            super(4, nbrOfBits, 32);
            _enumValues = enumValues;
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
            super(8, nbrOfBits, 64);
            _enumValues = enumValues;
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