/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.java.nio;

/**
 * Clean-room implementation of ByteBuffer to support
 * <code>javolution.util.Struct</code> when <code>java.nio</code> is
 * not available.
 */
public final class ByteBuffer extends Buffer {

    private ByteOrder _order = ByteOrder.BIG_ENDIAN;

    private final byte[] _bytes;

    private ByteBuffer(byte[] bytes) {
        super(bytes.length, bytes.length, 0, -1);
        _bytes = bytes;
    }

    public static ByteBuffer allocateDirect(int capacity) {
        return new ByteBuffer(new byte[capacity]);
    }

    public static ByteBuffer allocate(int capacity) {
        return new ByteBuffer(new byte[capacity]);
    }

    final public static ByteBuffer wrap(byte[] array) {
        return new ByteBuffer(array);
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            dst[i] = get();
        }

        return this;
    }

    public ByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    public ByteBuffer put(ByteBuffer src) {
        if (src.remaining() > 0) {
            byte[] toPut = new byte[src.remaining()];
            src.get(toPut);
            src.put(toPut);
        }

        return this;
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            put(src[i]);
        }

        return this;
    }

    public final ByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return true;
    }

    public final byte[] array() {
        return _bytes;
    }

    public final int arrayOffset() {
        return 0;
    }

    public final ByteOrder order() {
        return _order;
    }

    public final ByteBuffer order(ByteOrder endian) {
        _order = endian;
        return this;
    }

    public byte get() {
        return _bytes[_position++];
    }

    public ByteBuffer put(byte b) {
        _bytes[_position++] = b;
        return this;
    }

    public byte get(int index) {
        return _bytes[index];
    }

    public ByteBuffer put(int index, byte b) {
        _bytes[index] = b;
        return this;
    }

    public boolean isDirect() {
        return false;
    }

    public char getChar() {
        char val = getChar(_position);
        _position += 2;
        return val;
    }

    public ByteBuffer putChar(char value) {
        putChar(_position, value);
        _position += 2;
        return this;
    }

    public char getChar(int index) {
        return (char) getShort(index);
    }

    public ByteBuffer putChar(int index, char value) {
        return putShort(index, (short) value);
    }

    public short getShort() {
        short val = getShort(_position);
        _position += 2;
        return val;
    }

    public ByteBuffer putShort(short value) {
        putShort(_position, value);
        _position += 2;
        return this;
    }

    public short getShort(int index) {
        if (_order == ByteOrder.LITTLE_ENDIAN) {
            return (short) ((_bytes[index] & 0xff) + (_bytes[++index] << 8));
        } else {
            return (short) ((_bytes[index] << 8) + (_bytes[++index] & 0xff));
        }
    }

    public ByteBuffer putShort(int index, short value) {
        if (_order == ByteOrder.LITTLE_ENDIAN) {
            _bytes[index] = (byte) value;
            _bytes[++index] = (byte) (value >> 8);
        } else {
            _bytes[index] = (byte) (value >> 8);
            _bytes[++index] = (byte) value;
        }
        return this;
    }

    public int getInt() {
        int val = getInt(_position);
        _position += 4;
        return val;
    }

    public ByteBuffer putInt(int value) {
        putInt(_position, value);
        _position += 4;
        return this;
    }

    public int getInt(int index) {
        if (_order == ByteOrder.LITTLE_ENDIAN) {
            return (_bytes[index] & 0xff) + ((_bytes[++index] & 0xff) << 8)
                    + ((_bytes[++index] & 0xff) << 16)
                    + ((_bytes[++index] & 0xff) << 24);
        } else {
            return (_bytes[index] << 24) + ((_bytes[++index] & 0xff) << 16)
                    + ((_bytes[++index] & 0xff) << 8)
                    + (_bytes[++index] & 0xff);
        }
    }

    public ByteBuffer putInt(int index, int value) {
        if (_order == ByteOrder.LITTLE_ENDIAN) {
            _bytes[index] = (byte) value;
            _bytes[++index] = (byte) (value >> 8);
            _bytes[++index] = (byte) (value >> 16);
            _bytes[++index] = (byte) (value >> 24);
        } else {
            _bytes[index] = (byte) (value >> 24);
            _bytes[++index] = (byte) (value >> 16);
            _bytes[++index] = (byte) (value >> 8);
            _bytes[++index] = (byte) value;
        }
        return this;
    }

    public long getLong() {
        long val = getLong(_position);
        _position += 8;
        return val;
    }

    public ByteBuffer putLong(long value) {
        putLong(_position, value);
        _position += 8;
        return this;
    }

    public long getLong(int index) {
        if (_order == ByteOrder.LITTLE_ENDIAN) {
            return (_bytes[index] & 0xff) + ((_bytes[++index] & 0xff) << 8)
                    + ((_bytes[++index] & 0xff) << 16)
                    + ((_bytes[++index] & 0xffL) << 24)
                    + ((_bytes[++index] & 0xffL) << 32)
                    + ((_bytes[++index] & 0xffL) << 40)
                    + ((_bytes[++index] & 0xffL) << 48)
                    + (((long) _bytes[++index]) << 56);
        } else {
            return (((long) _bytes[index]) << 56)
                    + ((_bytes[++index] & 0xffL) << 48)
                    + ((_bytes[++index] & 0xffL) << 40)
                    + ((_bytes[++index] & 0xffL) << 32)
                    + ((_bytes[++index] & 0xffL) << 24)
                    + ((_bytes[++index] & 0xff) << 16)
                    + ((_bytes[++index] & 0xff) << 8)
                    + (_bytes[++index] & 0xffL);
        }
    }

    public ByteBuffer putLong(int index, long value) {
        if (_order == ByteOrder.LITTLE_ENDIAN) {
            _bytes[index] = (byte) value;
            _bytes[++index] = (byte) (value >> 8);
            _bytes[++index] = (byte) (value >> 16);
            _bytes[++index] = (byte) (value >> 24);
            _bytes[++index] = (byte) (value >> 32);
            _bytes[++index] = (byte) (value >> 40);
            _bytes[++index] = (byte) (value >> 48);
            _bytes[++index] = (byte) (value >> 56);
        } else {
            _bytes[index] = (byte) (value >> 56);
            _bytes[++index] = (byte) (value >> 48);
            _bytes[++index] = (byte) (value >> 40);
            _bytes[++index] = (byte) (value >> 32);
            _bytes[++index] = (byte) (value >> 24);
            _bytes[++index] = (byte) (value >> 16);
            _bytes[++index] = (byte) (value >> 8);
            _bytes[++index] = (byte) value;
        }
        return this;
    }

    public float getFloat() {
        float val = getFloat(_position);
        _position += 4;
        return val;
    }

    public ByteBuffer putFloat(float value) {
        putFloat(_position, value);
        _position += 4;
        return this;
    }

    public float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    public ByteBuffer putFloat(int index, float value) {
        return putInt(index, Float.floatToIntBits(value));
    }

    public double getDouble() {
        double val = getDouble(_position);
        _position += 8;
        return val;
    }

    public ByteBuffer putDouble(double value) {
        putDouble(_position, value);
        _position += 8;
        return this;
    }

    public double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    public ByteBuffer putDouble(int index, double value) {
        return putLong(index, Double.doubleToLongBits(value));
    }
}
