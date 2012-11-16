/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_BOOLEAN_HPP
#define _JAVOLUTION_LANG_BOOLEAN_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"

namespace javolution {
    namespace lang {
        class Boolean_API;
        class Boolean; 
    }
}

/**
 * This class wraps a value of the primitive type <code>Type::boolean</code>
 * in an object.
 *
 * Autoboxing and direct comparisons with  <code>Type::boolean</code> type
 * are supported. For example: <pre><code>
 *      Boolean b = false;
 *      ...
 *      if (b == false) { ... }
 * <code></pre>
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Boolean.html">
 *       Java - Boolean</a>
 * @version 1.0
 */
class javolution::lang::Boolean_API : public virtual javolution::lang::Object_API {

    /**
     * Holds the boolean value.
     */
    Type::boolean _value;

    /**
     * Private constructor (factory methods should be used).
     */
    Boolean_API(Type::boolean value) : _value(value) {
    }

public:

    /**
     * Returns a boolean for the specified value.
     *
     * @param value the boolean value.
     */
    static Boolean const& valueOf(Type::boolean value); // Reference ok. Returns static instance.

    /**
     * Returns the primitive boolean value for this boolean object.
     */
    Type::boolean booleanValue() const {
        return _value;
    }

    /**
     * Returns the textual representation of this boolean.
     */
    javolution::lang::String toString() const {
    	return String_API::valueOf(_value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // No need to override Object.equals and Object.hashCode due to unicity. //
    ///////////////////////////////////////////////////////////////////////////

private:

    JAVOLUTION_DLL static Boolean newStaticInstance(Type::boolean value);

};

// Sub-class of Handle<Boolean_API> to support automatic conversions/comparisons.
class javolution::lang::Boolean : public Type::Handle<javolution::lang::Boolean_API> {
public:
    Boolean(Type::NullHandle = Type::Null) : Type::Handle<Boolean_API>() {} // Null.
    Boolean(Boolean_API* ptr) : Type::Handle<Boolean_API>(ptr) {} // Construction from handle.

    // Autoboxing.
    Boolean(Type::boolean value) {
        *this = Boolean_API::valueOf(value);
    }

    Boolean& operator=(Type::boolean value) {
        return *this = Boolean_API::valueOf(value);
    }

    // Deboxing.
    operator Type::boolean() const {
        return get()->booleanValue();
    }

};

inline javolution::lang::Boolean const& javolution::lang::Boolean_API::valueOf(Type::boolean value) {
    static Boolean booleanTrue = newStaticInstance(true);
    static Boolean booleanFalse = newStaticInstance(false);
    return value ? booleanTrue : booleanFalse;
}

#endif

