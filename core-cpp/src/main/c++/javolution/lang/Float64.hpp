/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_FLOAT64_HPP
#define _JAVOLUTION_LANG_FLOAT64_HPP

#include "javolution/lang/Number.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/ArithmeticException.hpp"

namespace javolution {
    namespace lang {      
        class Float64;
        class Float64_API;
    }
}

/**
 * This class represents a 64 bits float value. Unlike java.lang.Float
 * an exception is raised if a conversion to a primitive type would result
 * in a truncation.
 *
 * Autoboxing and comparisons with <code>Type::float64</code> type are supported.
 * For example: <pre><code>
 *      Float64 d = 13.3;
 *      if (d >= 0.0) { ... }
 * <code></pre>
 *
 * @version 1.0
 */
class javolution::lang::Float64_API : public javolution::lang::Number_API {

    /**
	 * Holds the 64 bits float value.
	 */
	Type::float64 _value;

	/**
	 * Private constructor (class final).
	 */
	Float64_API(Type::float64 value) : _value(value) {
	}

public:

    /**
     * Returns a 64 bits float having the specified value.
     *
     * @param value the value.
     */
    static Float64 valueOf(Type::float64 value);

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
        return (Type::float32)_value;
    }

    // Overrides.
    Type::float64 doubleValue() const {
        return _value;
    }

    // Overrides.
    javolution::lang::String toString() const {
        return javolution::lang::String_API::valueOf(_value);
    }

    // Overrides
    Type::boolean equals(javolution::lang::Object obj) const;
    Type::boolean equals(Float64 const& that) const;

    // Overrides
    Type::int32 hashCode() const {
        Type::int32 *float_as_int = (Type::int32*) &_value;
        return *float_as_int;
    }

private:

    JAVOLUTION_DLL static Float64 createStaticZero();

};

// Sub-class of Handle<Float64_API> to support automatic conversions/comparisons.
class javolution::lang::Float64 : public Type::Handle<javolution::lang::Float64_API> {
public:
    Float64(Type::NullHandle = Type::Null) : Type::Handle<Float64_API>() {} // Null.
    Float64(Float64_API* ptr) : Type::Handle<Float64_API>(ptr) {} // Construction from handle.

    // Autoboxing.
    Float64(Type::float64 value) {
        *this = Float64_API::valueOf(value);
    }

    Float64& operator=(Type::float64 value) {
        return *this = Float64_API::valueOf(value);
    }

    // Deboxing.
    operator Type::float64() const {
        return get()->doubleValue();
    }

};

inline javolution::lang::Float64 javolution::lang::Float64_API::valueOf(Type::float64 thisValue) {
    static Float64 zero = createStaticZero(); // To avoid C++ initialization fiasco.
    return (thisValue == 0.0f) ? zero : Float64(new Float64_API(thisValue));
}
inline Type::boolean  javolution::lang::Float64_API::equals(javolution::lang::Object obj) const {
    Float64 that = Type::dynamic_handle_cast<Float64_API>(obj);
    if (that == Type::Null) return false;
    return equals(that);
}
inline Type::boolean  javolution::lang::Float64_API::equals( javolution::lang::Float64 const& that) const {
    return _value == that->_value;
}

#endif
