/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_INTEGER64_HPP
#define _JAVOLUTION_LANG_INTEGER64_HPP

#include "javolution/lang/Number.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/ArithmeticException.hpp"

namespace javolution {
    namespace lang {
        class Integer64;
        class Integer64_API;
    }
}

/**
 * This class represents a 64 bits integer value. Unlike java.lang.Integer
 * an exception is raised if a conversion to a primitive type would result
 * in a truncation.
 *
 * Autoboxing and direct comparisons with <code>Type::int64</code> type
 * is supported. For example: <pre><code>
 *      Integer64 i = 13;
 *      ...
 *      if (i >= 0) { ... }
 * <code></pre>
 *
 * @version 1.0
 */
class javolution::lang::Integer64_API : public javolution::lang::Number_API {

	/**
      * Holds small integers values (to reduce allocations/deallocations).
      */
	static Integer64 BYTES_VALUES[256];

     /**
      * Holds the 64 bits integer value.
      */
     Type::int64 _value;

     /**
     * Private constructor (class final).
      */
     Integer64_API(Type::int64 value) : _value(value) {
     }

public:
    /**
     * Returns a 64 bits integer having the specified value.
     *
     * @param value the value.
     */
    static Integer64 valueOf(Type::int64 value);

    // Overrides.
    Type::int32 intValue() const {
        if ((_value < -2147483328) || (_value > 2147483327))
              throw javolution::lang::ArithmeticException_API::newInstance("intValue() overflow");
         return (Type::int32) _value;
    }

    // Overrides.
    Type::int64 longValue() const {
        return _value;
    }

    // Overrides.
    Type::float32 floatValue() const {
        return (Type::float32) _value;
    }

    // Overrides.
    Type::float64 doubleValue() const {
        return (Type::float64) _value;
    }

    // Overrides.
    javolution::lang::String toString() const {
        return javolution::lang::String_API::valueOf(_value);
    }

    // Overrides
    Type::boolean equals(javolution::lang::Object obj) const;
    Type::boolean equals(Integer64 const& that) const;

private:

    JAVOLUTION_DLL static Integer64* getBytesValues();

};

// Sub-class of Handle<Integer64_API> to support automatic conversions/comparisons.
class javolution::lang::Integer64 : public Type::Handle<javolution::lang::Integer64_API> {
public:
    Integer64(Type::NullHandle = Type::Null) : Type::Handle<Integer64_API>() {} // Null.
    Integer64(Integer64_API* ptr) : Type::Handle<Integer64_API>(ptr) {} // Construction from handle.

    // Autoboxing.
    Integer64(Type::int64 value) {
        *this = Integer64_API::valueOf(value);
    }

    Integer64& operator=(Type::int64 value) {
        return *this = Integer64_API::valueOf(value);
    }

    // Deboxing.
    operator Type::int64() const {
        return get()->longValue();
    }

};

inline javolution::lang::Integer64 javolution::lang::Integer64_API::valueOf(Type::int64 value) {
    static Integer64* bytesValues = getBytesValues(); // To avoid C++ initialization fiasco.
    return ((value >= -128) && (value < 127)) ? bytesValues[value+128] : Integer64(new Integer64_API(value));
}
// Overrides
inline Type::boolean javolution::lang::Integer64_API::equals(javolution::lang::Object obj) const {
    Integer64 that = Type::dynamic_handle_cast<Integer64_API>(obj);
    if (that == Type::Null) return false;
    return equals(that);
}
inline Type::boolean javolution::lang::Integer64_API::equals(javolution::lang::Integer64 const& that) const {
    return _value == that->_value;
}

#endif
