/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_FLOAT32_HPP
#define _JAVOLUTION_LANG_FLOAT32_HPP

#include "javolution/lang/Number.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/ArithmeticException.hpp"

namespace javolution {
    namespace lang {
        class Float32;
        class Float32_API;
    }
}

/**
 * This class represents a 32 bits float value. Unlike java.lang.Float
 * an exception is raised if a conversion to a primitive type would result
 * in a truncation.
 *
 * Autoboxing and comparisons with <code>Type::float32</code> type are supported.
 * For example: <pre><code>
 *      Float32 f = 13.3f;
 *      if (f >= 0.0f) { ... }
 * <code></pre>
 *
 * @version 1.0
 */
class javolution::lang::Float32_API : public javolution::lang::Number_API {

    /**
	 * Holds the 32 bits float value.
	 */
	Type::float32 _value;

	/**
	 * Private constructor (class final).
	 */
	Float32_API(Type::float32 value) : _value(value) {
	}

public:

    /**
     * Returns a 32 bits float having the specified value.
     *
     * @param value the value.
     */
    static Float32 valueOf(Type::float32 value);

    // Overrides.
    Type::int32 intValue() const {
        if ((_value < -2147483328.0) || (_value > 2147483327.0))
            throw javolution::lang::ArithmeticException_API::newInstance("intValue() overflow");
        return (Type::int32) _value;
    }

    // Overrides.
    Type::int64 longValue() const {
        if ((_value < -9223372036854775808.0) || (_value > 9223372036854775807.0))
            throw javolution::lang::ArithmeticException_API::newInstance("longValue() overflow");
        return (Type::int64) _value;
    }

    // Overrides.
    Type::float32 floatValue() const {
        return _value;
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
    Type::boolean equals(Float32 const& that) const;

    // Overrides
    Type::int32 hashCode() const {
        Type::int32 *float_as_int = (Type::int32*) &_value;
        return *float_as_int;
    }

private:

    JAVOLUTION_DLL static Float32 createStaticZero();

};

// Sub-class of Handle<Float32_API> to support automatic conversions/comparisons.
class javolution::lang::Float32 : public Type::Handle<javolution::lang::Float32_API> {
public:
    Float32(Type::NullHandle = Type::Null) : Type::Handle<Float32_API>() {} // Null.
    Float32(Float32_API* ptr) : Type::Handle<Float32_API>(ptr) {} // Construction from handle.

    // Autoboxing.
    Float32(Type::float32 value) {
        *this = Float32_API::valueOf(value);
    }

    Float32& operator=(Type::float32 value) {
        return *this = Float32_API::valueOf(value);
    }

    // Deboxing.
    operator Type::float32() const {
        return get()->floatValue();
    }

};

inline javolution::lang::Float32 javolution::lang::Float32_API::valueOf(Type::float32 thisValue) {
    static Float32 zero = createStaticZero(); // To avoid C++ initialization fiasco.
    return (thisValue == 0.0f) ? zero : Float32(new Float32_API(thisValue));
}
inline Type::boolean javolution::lang::Float32_API::equals(javolution::lang::Object obj) const {
    Float32 that = Type::dynamic_handle_cast<Float32_API>(obj);
    if (that == Type::Null) return false;
    return equals(that);
}
inline Type::boolean javolution::lang::Float32_API::equals(javolution::lang::Float32 const& that) const {
    return _value == that->_value;
}

#endif
