/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_INTEGER32_HPP
#define _JAVOLUTION_LANG_INTEGER32_HPP

#include "javolution/lang/Number.hpp"
#include "javolution/lang/String.hpp"

namespace javolution {
    namespace lang {
        class Integer32;
        class Integer32_API;
    }
}
/**
 * This class represents a 32 bits integer value. Unlike java.lang.Integer
 * an exception is raised if a conversion to a primitive type would result
 * in a truncation.
 * 
 * Autoboxing and direct comparisons with <code>Type::int32</code> type
 * is supported. For example: <pre><code>
 *      Integer32 i = 13;
 *      ...
 *      if (i >= 0) { ... }
 * <code></pre>
 *
 * @version 1.0
 */
class javolution::lang::Integer32_API : public javolution::lang::Number_API {

	/**
      * Holds small integers values (to reduce allocations/deallocations).
      */
	static Integer32 BYTES_VALUES[256];

	/**
      * Holds the 32 bits integer value.
      */
     Type::int32 _value;

     /**
     * Private constructor (class final).
      */
     Integer32_API(Type::int32 value) : _value(value) {
     }

public:
    /**
     * Returns a 32 bits integer having the specified value.
     *
     * @param value the value.
     */
    static Integer32 valueOf(Type::int32 value);

    // Overrides.
    Type::int32 intValue() const {
        return _value;
    }

    // Overrides.
    Type::int64 longValue() const {
        return (Type::int64) _value;
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
    Type::boolean equals(Integer32 const& that) const;

    // Overrides
    Type::int32 hashCode() const {
        return _value;
    }

private:

    JAVOLUTION_DLL static Integer32* getBytesValues();

};

// Sub-class of Handle<Integer32_API> to support automatic conversions/comparisons.
class javolution::lang::Integer32 : public Type::Handle<javolution::lang::Integer32_API> {
public:
    Integer32(Type::NullHandle = Type::Null) : Type::Handle<Integer32_API>() {} // Null.
    Integer32(Integer32_API* ptr) : Type::Handle<Integer32_API>(ptr) {} // Construction from handle.

    // Autoboxing.
    Integer32(Type::int32 value) {
        *this = Integer32_API::valueOf(value);
    }

    Integer32& operator=(Type::int32 value) {
        return *this = Integer32_API::valueOf(value);
    }

    // Deboxing.
    operator Type::int32() const {
        return get()->intValue();
    }

};

inline javolution::lang::Integer32 javolution::lang::Integer32_API::valueOf(Type::int32 value) {
    static Integer32* bytesValues = getBytesValues(); // To avoid C++ initialization fiasco.
     return ((value >= -128) && (value < 127)) ? bytesValues[value+128] : Integer32(new Integer32_API(value));
}
inline Type::boolean javolution::lang::Integer32_API::equals(javolution::lang::Object obj) const {
    Integer32 that = Type::dynamic_handle_cast<Integer32_API>(obj);
    if (that == Type::Null) return false;
    return equals(that);
}
inline Type::boolean javolution::lang::Integer32_API::equals(javolution::lang::Integer32 const& that) const {
    return _value == that->_value;
}

#endif
