/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
////////////////////////////////////////////////////
 // This code is derived from Javolution classes  //
 // See copyright below.                          //
////////////////////////////////////////////////////

/* Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

#include "javolution/lang/String.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "javolution/lang/IllegalArgumentException.hpp"
#include "javolution/lang/IndexOutOfBoundsException.hpp"
#include "javolution/lang/UnsupportedOperationException.hpp"
#include "javolution/util/FastMap.hpp"
#include "javolution/lang/Boolean.hpp"
#include "javolution/lang/Character.hpp"
#include "javolution/lang/Integer32.hpp"
#include "javolution/lang/Integer64.hpp"
#include "javolution/lang/Float32.hpp"
#include "javolution/lang/Float64.hpp"

using namespace javolution::lang;
using namespace javolution::util;
using namespace javolution::lang;

String String_API::valueOf(Object value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(const wchar_t* value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(const std::wstring& value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(const char* value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(const std::string& value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(Type::wchar value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(char value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(Type::int8 value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(Type::int16 value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(Type::int32 value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}
String String_API::valueOf(Type::int64 value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(Type::float32 value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(Type::float64 value) {
    StringBuilder sb = new StringBuilder_API();
    return sb->append(value)->toString();
}

String String_API::valueOf(Type::boolean value) {
	static String TRUE_STRING = String_API::valueOf(L"true")->intern();
	static String FALSE_STRING = String_API::valueOf(L"false")->intern();
    return value ? TRUE_STRING : FALSE_STRING;
}

String String_API::valueOf(Boolean b) {
	return valueOf(b->booleanValue());
}

String String_API::valueOf(Character c) {
	return valueOf(c->charValue());
}

String String_API::valueOf(Integer32 i32) {
	return valueOf(i32->intValue());
}

String String_API::valueOf(Integer64 i64) {
	return valueOf(i64->longValue());
}

String String_API::valueOf(Float32 f32) {
	return valueOf(f32->floatValue());
}

String String_API::valueOf(Float64 f64) {
	return valueOf(f64->doubleValue());
}

String String_API::substring(Type::int32 beginIndex) const {
    return substring(beginIndex, length());
}

String String_API::substring(Type::int32 beginIndex, Type::int32 endIndex) const {
	if (beginIndex < 0)
		throw IndexOutOfBoundsException_API::newInstance(L"beginIndex is negative");
	if (endIndex > length())
		throw IndexOutOfBoundsException_API::newInstance(L"endIndex is larger than the length of this String object");
	if (beginIndex > endIndex)
		throw IndexOutOfBoundsException_API::newInstance(L"beginIndex is larger than endIndex.");
	Type::int32 length = endIndex - beginIndex;
	Type::wchar* wchars = new Type::wchar[length];
	System_API::arraycopy(_wchars, beginIndex, wchars, 0, length);
    return new String_API(wchars, length);
}

String String_API::concat(String const& that) const {
	Type::int32 length = this->length() + that->length();
	Type::wchar* wchars = new Type::wchar[length];
	System_API::arraycopy(this->_wchars, 0, wchars, 0, this->_length);
	System_API::arraycopy(that->_wchars, 0, wchars, this->_length, that->_length);
    return new String_API(wchars, length);
}

Type::boolean String_API::startsWith(String const& prefix, Type::int32 offset) const {
	Type::int32 prefixLength = prefix->length();
	if (prefixLength + offset > length()) return false;
	for (int i=0; i < prefixLength; i++) {
		if (this->_wchars[i+offset] != prefix->_wchars[i]) return false;
	}
	return true;
}

String String_API::intern() const {
	static FastMap<String, String> STRINGS_INTERN
	    = new FastMap_API<String, String>();
	String str = const_cast<String_API*>(this);
	synchronized (STRINGS_INTERN) {
		String strIntern = STRINGS_INTERN->get(str);
		if (strIntern != Type::Null) return strIntern;
		STRINGS_INTERN->put(str , str);
	}
    return str;
}

Type::boolean String_API::equals(Object obj) const {
	String that = Type::dynamic_handle_cast<String_API>(obj);
    if (that == Type::Null) return false;
    return equals(that);
}

Type::boolean String_API::equals(String const& that) const {
    if (that == Type::Null) return false;
    if (this == that.get()) return true; // Same (possibly intern())
    if (this->length() != that->length()) return false;
    for (Type::int32 i=0, n = length(); i < n; i++) {
    	if (this->_wchars[i] != that->_wchars[i]) return false;
    }
    return true;
}

Type::int32 String_API::hashCode() const {
    if (_hash != 0) return _hash;
    int h = 0;
    for (Type::int32 i = 0, n = length(); i < n; i++) {
        h = 31 * h + _wchars[i];
    }
    return const_cast<String_API*>(this)->_hash = h;
}

std::string String_API::toUTF8() const {
	static const Type::int32 BUFFER_LENGTH = 1024;
	if (_length * 4 > BUFFER_LENGTH) { // Possible buffer overflow, split the work.
		Type::int32 half = _length >> 1;
		return substring(0, half)->toUTF8() + substring(half, _length)->toUTF8();
	}
    char buffer[BUFFER_LENGTH];
	for (Type::int32 i = 0, j = 0;;) {
		if (i >= _length) return std::string(buffer, j); // Done.
		Type::wchar w = _wchars[i++];
		if (w <= 0x7f) {
			buffer[j++] = (char) w;
		} else if (w <= 0x7ff) {
			buffer[j++] = (char) (0xc0 | ((w >> 6) & 0x1f));
			buffer[j++] = (char) (0x80 | (w & 0x3f));
		} else if (w <= 0xffff) {
			buffer[j++] = (char) (0xe0 | ((w >> 12) & 0x0f));
			buffer[j++] = (char) (0x80 | ((w >> 6) & 0x3f));
			buffer[j++] = (char) (0x80 | (w & 0x3f));
#ifndef _WINDOWS
		} else if (w <= 0x10ffff) { // On windows wchar_t is 16 bits!
			buffer[j++] = (char) (0xf0 | ((w >> 18) & 0x07));
			buffer[j++] = (char) (0x80 | ((w >> 12) & 0x3f));
			buffer[j++] = (char) (0x80 | ((w >> 6) & 0x3f));
			buffer[j++] = (char) (0x80 | (w & 0x3f));
#endif
		} else {
			throw UnsupportedOperationException_API::newInstance(
					"Unsupported Wide Character");
		}
	}
}


// Cannot be included in the header file since IllegalArgumentException
// depends upon String.hpp (here to break header circularity).

void String_API::throwIllegalArgumentException(Type::int32 index) const {
    throw IllegalArgumentException_API::newInstance(
            StringBuilder_API::newInstance()
            ->append(L"String of length: ")
            ->append(length())
            ->append(L" but found index: ")
            ->append(index)->toString());
}

