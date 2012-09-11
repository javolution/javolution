/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_CHARACTER_HPP
#define _JAVOLUTION_LANG_CHARACTER_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/ArithmeticException.hpp"

namespace javolution {
    namespace lang {
        class Character_API;
        class Character;
    }
}
/**
 * This class wraps the value of the primitive type <code>Type::wchar</code>
 * in an object.
 *
 * Autoboxing and direct comparisons with <code>char</code> (ASCII) and
 * <code>Type::wchar</code> are supported. For example: <pre><code>
 *      Character b = L'x';
 *      ...
 *      if (b >= L'A') { ... }
 * <code></pre>
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Character.html">
 *       Java - Character</a>
 * @version 1.0
 */
class javolution::lang::Character_API : public virtual javolution::lang::Object_API {

    /**
     * Holds the Character value.
     */
    Type::wchar _value;

    /**
     * Private constructor (class final).
     */
    Character_API(Type::wchar value) : _value(value) {
    }

public:

    /**
     * Returns a Character for the specified ASCII character.
     *
     * @param ascii the ascii character.
     */
    static Character& valueOf(char ascii); // Reference ok. Returns static instance.

    /**
     * Returns a Character for the specified value.
     *
     * @param value the wide character value.
     */
    static Character valueOf(Type::wchar value);

    /**
      * Returns the ASCII character value for this character.
      * This method throws an overflow error if this character cannot
      * be represented as an ascii character.
      */
     char asciiValue() const {
     	if (_value > 127) throw ArithmeticException_API::newInstance("wchar to ascii overflow");
         return (char) _value;
     }

     /**
     * Returns the primitive wide character value for this character.
     */
    Type::wchar charValue() const {
        return _value;
    }
    Type::wchar wcharValue() const { // For backward compatibility.
          return _value; // TODO: Remove
      }


    /**
     * Returns the textual representation of this Character (string of length 1).
     */
    javolution::lang::String toString() const {
        return String_API::valueOf(_value);
    }

    // Overrides
    Type::boolean equals(javolution::lang::Object obj) const;
    Type::boolean equals(Character const& that) const;
   
    // Overrides
    Type::int32 hashCode() const {
        return (Type::int32) _value;
    }

private:

    JAVOLUTION_DLL static Character* getASCIICharacters();

};

// Sub-class of Handle<Character_API> to support automatic conversions/comparisons.
class javolution::lang::Character : public Type::Handle<javolution::lang::Character_API> {
public:
    Character(Type::NullHandle = Type::Null) : Type::Handle<Character_API>() {} // Null.
    Character(Character_API* ptr) : Type::Handle<Character_API>(ptr) {} // Construction from handle.

    // Autoboxing.
    Character(char ascii) {
        *this = Character_API::valueOf(ascii);
    }

    Character(Type::wchar value) {
        *this = Character_API::valueOf(value);
    }

    Character& operator=(char ascii) {
        return *this = Character_API::valueOf(ascii);
    }

    Character& operator=(Type::wchar value) {
        return *this = Character_API::valueOf(value);
    }

    // Deboxing.
    operator char() const {
        return get()->asciiValue();
    }

    operator Type::wchar() const {
        return get()->charValue();
    }

 };

inline javolution::lang::Character& javolution::lang::Character_API::valueOf(char ascii) { // Reference ok, static instance.
    static Character* characters = getASCIICharacters(); // To avoid C++ initialization fiasco.
    return characters[ascii]; // IndexOutOfBound exception if character is not ASCII.
}
inline javolution::lang::Character javolution::lang::Character_API::valueOf(Type::wchar value) {
    return (value < 128) ? Character_API::valueOf((char)value) : new Character_API(value);
}
inline Type::boolean javolution::lang::Character_API::equals(javolution::lang::Object obj) const {
    Character that = Type::dynamic_handle_cast<Character_API>(obj);
    if (that == Type::Null) return false;
    return equals(that);
}
inline Type::boolean javolution::lang::Character_API::equals(javolution::lang::Character const& that) const {
    return _value == that->_value;
}

#endif

