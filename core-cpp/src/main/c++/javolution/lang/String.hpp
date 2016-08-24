/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_STRING_HPP
#define _JAVOLUTION_LANG_STRING_HPP

#include "javolution/lang/Object.hpp"
#include <string>

namespace javolution {
    namespace lang {
        class String_API;
        class String;
        class StringBuilder_API;
        class Boolean;
        class Character;
        class Integer32;
        class Integer64;
        class Float32;
        class Float64;
    }
}

/**
 * This class represents character string (immutable).
 *
 * This class supports autoboxing with <code>char*</code> and 
 * <code>wchar_t*</code>, e.g. <code>String str = L"Éléphant";</code>
 *
 * To construct string instances, {@link StringBuilder} can be used:[code]
 *     StringBuilder sb = StringBuilder_API::newInstance();
 *     sb->append(L"abc")->append(2)->append(3.445);
 *     std::cout << sb->toString() << std::endl;
 *
 *     >> abc23.445
 * [code]
 * Direct concatenation of literal wide strings with any objects is also
 * supported:[code]
 *    if (cls != Type::Null)
 *        throw IllegalArgumentException_API::newInstance(
 *            L"The name: " + name + L" is already associated with the class " + cls);
 * [/code]
 *
 * @version 1.0
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/String.html">
 *       Java - String</a>
 */
class javolution::lang::String_API : public virtual javolution::lang::Object_API {
    friend class StringBuilder_API;

    /**
     * Holds the length.
     */
    Type::int32 _length;

    /**
     * Holds the hash code (different from 0 when calculated).
     */
    Type::int32 _hash;

    /**
     * Holds the wide characters.
     */
    Type::wchar* _wchars;

    /**
     * Creates a string holding the specified wide characters.
     * Private constructor solely used by StringBuilder (factory method 
     * should be used).
     *
     * @param wchars the wide characters array for this string (now managed
     *        by this string object).
     * @param length the number of wide characters.
     */
    String_API(Type::wchar* wchars, Type::int32 length) : _length(length), _hash(0), _wchars(wchars) {
    }

public:

    /**
     * Returns the string representing the specified object.
     * This method is equivalent to <code>obj.toString()</code> except
     * it returns <code>"null"</code> if the specified object is
     * <code>NULL</code>
     *
     * @param obj the object or <code>NULL</code>
     */
    JAVOLUTION_DLL static String valueOf(Object const& value);

    /**
     * Returns the string holding the specified wide characters
     * (null terminated).
     *
     * @param wchars the wide characters.
     */
    JAVOLUTION_DLL static String valueOf(const wchar_t* wchars);

    /**
     * Returns the string holding the specified C++ wide string.
     *
     * @param wstr the wide string.
     */
    JAVOLUTION_DLL static String valueOf(const std::wstring& wstr);

    /**
     * Returns the string holding the specified UTF-8 simple characters
     * (null terminated).
     *
     * @param chars the UTF-8 characters.
     */
    JAVOLUTION_DLL static String valueOf(const char* chars);

    /**
     * Returns the string holding the specified UTF-8 C++ string.
     *
     * @param str the string.
     */
    JAVOLUTION_DLL static String valueOf(const std::string& str);

    /**
     * Returns the string holding the specified wide character.
     *
     * @param value the character value.
     */
    JAVOLUTION_DLL static String valueOf(Type::wchar value);

    /**
     * Returns the string holding the specified ascii character.
     *
     * @param value the ascii character value.
     */
    JAVOLUTION_DLL static String valueOf(char value);

    /**
     * Returns the string holding the decimal representation of the specified
     * 8 bits integer value.
     *
     * @param value the 8 bits integer value.
     */
    JAVOLUTION_DLL static String valueOf(Type::int8 value);

    /**
     * Returns the string holding the decimal representation of the specified
     * 16 bits integer value.
     *
     * @param value the 32 bits integer value.
     */
    JAVOLUTION_DLL static String valueOf(Type::int16 value);

    /**
     * Returns the string holding the decimal representation of the specified
     * 32 bits integer value.
     *
     * @param value the 32 bits integer value.
     */
    JAVOLUTION_DLL static String valueOf(Type::int32 value);

    /**
     * Returns the string holding the decimal representation of the specified
     * 64 bits integer value.
     *
     * @param value the 32 bits integer value.
     */
    JAVOLUTION_DLL static String valueOf(Type::int64 value);

    /**
     * Returns the string holding the decimal representation of the specified
     * 32 bits floading point value.
     *
     * @param value the 32 bits floating point value.
     */
    JAVOLUTION_DLL static String valueOf(Type::float32 value);

    /**
     * Returns the string holding the decimal representation of the specified
     * 64 bits floading point value.
     *
     * @param value the 64 bits floating point value.
     */
    JAVOLUTION_DLL static String valueOf(Type::float64 value);

    /**
     * Returns the string holding the representation of the specified
     * boolean value.
     *
     * @param value the boolean value.
     */
    JAVOLUTION_DLL static String valueOf(Type::boolean value);

    /**
     * Equivalent to <code>valueOf(b->booleanValue())</code>
     */
    JAVOLUTION_DLL static String valueOf(Boolean const& b);

    /**
     * Equivalent to <code>valueOf(c->charValue())</code>
     */
    JAVOLUTION_DLL static String valueOf(Character const& c);

    /**
     * Equivalent to <code>valueOf(i32->intValue())</code>
     */
    JAVOLUTION_DLL static String valueOf(javolution::lang::Integer32 const& i32);

    /**
     * Equivalent to <code>valueOf(i64->longValue())</code>
     */
    JAVOLUTION_DLL static String valueOf(javolution::lang::Integer64 const& i64);

    /**
     * Equivalent to <code>valueOf(f32->floatValue())</code>
     */
    JAVOLUTION_DLL static String valueOf(javolution::lang::Float32 const& f32);

    /**
     * Equivalent to <code>append(f64->doubleValue())</code>
     */
    JAVOLUTION_DLL static String valueOf(javolution::lang::Float64 const& f64);

    /**
     * Returns the length of this string.
     *
     * @return the number of characters of this string.
     */
    Type::int32 length() const {
        return _length;
    }

    /**
     * Returns a new string that is a substring of this string.
     * The substring begins at the specified beginIndex and extends
     * to the character at <code>(this->length()-1)</code>.
     *
     * @return <code>substring(beginIndex, this->length())</code>
     * @throws IndexOutOfBoundsException if the beginIndex is negative,
     *         or larger than the length of this String object.
     */
    JAVOLUTION_DLL String substring(Type::int32 beginIndex) const;

    /**
     * Returns a new string that is a substring of this string.
     * The substring begins at the specified beginIndex and extends
     * to the character at index <code>(endIndex - 1)</code>.
     *
     * @return the number of characters of this string.
     * @throws IndexOutOfBoundsException  if the beginIndex is negative,
     *         or endIndex is larger than the length of this String object,
     *         or beginIndex is larger than endIndex.
     *
     */
    JAVOLUTION_DLL String substring(Type::int32 beginIndex, Type::int32 endIndex) const;

    /**
     * Returns the character at the specified position.
     *
     * @param index the index position.
     * @return the character at the specified position.
     * @throws IllegalArgumentException if the specified character is not
     *         in the string range.
     */
    Type::wchar charAt(Type::int32 index) const {
        if ((index < 0) || (index >= _length))
            throwIllegalArgumentException(index);
        return _wchars[index];
    }

    /**
     * Returns the result of the concatenation of this string with the one
     * specified.
     *
     * @param that the string to append.
     * @return a new string holding the concatenation result.
     */
    JAVOLUTION_DLL String concat(String const& that) const;

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param prefix the prefix to test.
     * @return <code>true</code>if this string starts with the specified prefix;
     *         <code>false</code> otherwise.
     */
    Type::boolean startsWith(String const& prefix) const {
        return startsWith(prefix, 0);
    }

    /**
     * Tests if the substring of this string beginning at the specified index
     * starts with the specified prefix.
     *
     * @param prefix the prefix to test.
     * @param offset the offset in this string.
     * @return <code>true</code>if this string at the specified offset
     *         starts with the specified prefix; <code>false</code> otherwise.
     */
    JAVOLUTION_DLL Type::boolean startsWith(String const& prefix, Type::int32 offset) const;

    /**
     * Indicates whether the specified object is a string holding the same
     * characters as this string.
     *
     * @param obj the reference object with which to compare or <code>Type::Null</code>
     * @return true if that object is a string identical to this one;
     *         <code>false</code> otherwise.
     */
    JAVOLUTION_DLL Type::boolean equals(Object const& obj) const;

    /**
     * Indicates whether the specified string is holding the same
     * characters as this string.
     *
     * @param that the reference string with which to compare or <code>Type::Null</code>.
     * @return <code>true</code> if this string and that string holds
     *         the same characters; <code>false</code> otherwise.
     */
    JAVOLUTION_DLL Type::boolean equals(String const& that) const;

    /**
     * Returns a hash code value for this string.
     *
     * @return the hash code value.
     */
    JAVOLUTION_DLL Type::int32 hashCode() const;

    /**
     * Returns the C++ char string (UTF-8 encoded) corresponding to this
     * string object.
     *
     * @return the UTF-8 representation of this string.
     */
    JAVOLUTION_DLL std::string toUTF8() const;

    /**
     * Returns the statically allocated String equals to this one.
     * If <code>str1.equal(str2)</code>
     * then <code>str1.intern() == str2.intern()</code>.
     *
     * @return an internal string equals to this one.
     */
    JAVOLUTION_DLL String intern() const;

    /**
     * Returns the C++ wide string corresponding to this string object.
     *
     * @return the wide string representation of this string.
     */
    std::wstring toWString() const {
        return std::wstring(_wchars, _length);
    }

    /**
     * Returns this string.
     *
     * @return <code>this<code>
     */
    String toString() const;

    /**
     * Overrides destructor to ensure internal array deallocation.
     */
    virtual ~String_API() {
        delete [] _wchars;
    }

private:

    /**
     * Throws an illegal argument exception for the specified index.
     *
     * @param index the index for which the illegal access is detected.
     */
    JAVOLUTION_DLL void throwIllegalArgumentException(Type::int32 index) const;

};

// Sub-class of Handle<String_API> to support automatic conversions/comparisons.

class javolution::lang::String : public Type::Handle<javolution::lang::String_API> {
public:

    String(Type::NullHandle = Type::Null) : Type::Handle<String_API>() {
    } // Null.

    String(String_API* ptr) : Type::Handle<String_API>(ptr) {
    } // Construction from handle.

    // Autoboxing.

    String& operator=(const char* chars) {
        return *this = String_API::valueOf(chars);
    }

    String(const char* chars) {
        *this = String_API::valueOf(chars);
    }

    String(std::string const& str) {
        *this = String_API::valueOf(str);
    }

    String& operator=(const wchar_t* wchars) {
        return *this = String_API::valueOf(wchars);
    }

    String(const wchar_t* wchars) {
        *this = String_API::valueOf(wchars);
    }

    String(std::wstring const& wstr) {
        *this = String_API::valueOf(wstr);
    }
};

inline javolution::lang::String javolution::lang::String_API::toString() const {
    return const_cast<String_API*> (this);
}

#endif
