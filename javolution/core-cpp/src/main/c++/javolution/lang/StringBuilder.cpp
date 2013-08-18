/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/System.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "javolution/lang/System.hpp"
#include <sstream>

using namespace javolution::lang;

StringBuilder StringBuilder_API::append(Object const& obj) {
	if (obj == Type::Null) return append(L"null");
	String str = obj->toString();
	while (_capacity < _length + str->length()) increaseCapacity();
	System_API::arraycopy(str->_wchars, 0, _buffer, _length, str->length());
	_length += str->length();
	return this;
}

StringBuilder StringBuilder_API::append(const wchar_t* wchars) {
	if (wchars == 0) return append(L"null");
    for (Type::int32 i=0; wchars[i] != 0;) {
    	if (_length >= _capacity) increaseCapacity();
    	_buffer[_length++] = wchars[i++];
	}
	return this;
}

StringBuilder StringBuilder_API::append(const char* chars) {
     if (chars == 0) return append(L"null");
     Type::wchar w = 0;
     Type::int32 bytes = 0;
     for (Type::int32 i=0;;) {
         unsigned char c = (unsigned char) chars[i++];
         if (c == 0) break;
         if (_length >= _capacity) increaseCapacity();
         if (c <= 0x7f) { // One byte.
        	 if (bytes != 0) throw IllegalArgumentException_API::newInstance("Invalid UTF-8 sequence");
     		 _buffer[_length++] = (Type::wchar) c;
         } else if (c <= 0xbf) { // Multi-bytes content.
        	 if (bytes == 0) throw IllegalArgumentException_API::newInstance("Invalid UTF-8 sequence");
             w = ((w << 6) | (c & 0x3f));
             bytes--;
             if (bytes == 0) _buffer[_length++] = w;
         } else if (c <= 0xdf) { // Two bytes sequence start.
             bytes = 1;
             w = c & 0x1f;
         } else if (c <= 0xef) { // Three bytes sequence start.
             bytes = 2;
             w = c & 0x0f;
         } else if (c <= 0xf7) { // Four bytes sequence start.
             bytes = 3;
             w = c & 0x07;
         } else {
        	 throw IllegalArgumentException_API::newInstance("Invalid UTF-8 sequence");
         }
     }
     if (bytes !=0) throw IllegalArgumentException_API::newInstance("Early termination of UTF-8 character sequence");
     return this;
 }

StringBuilder StringBuilder_API::append(Type::int32 value) {
	std::wostringstream out;
	out << value;
	return append(out.str());
}

StringBuilder StringBuilder_API::append(Type::int64 value) {
	std::wostringstream out;
	out << value;
	return append(out.str());
}

StringBuilder StringBuilder_API::append(Type::float32 value) {
	std::wostringstream out;
	out << value;
	return append(out.str());
}

StringBuilder StringBuilder_API::append(Type::float64 value) {
	std::wostringstream out;
	out << value;
	return append(out.str());
}

String StringBuilder_API::toString() const {
	Type::wchar* wchars = new Type::wchar[_length];
	System_API::arraycopy(_buffer, 0, wchars, 0, _length);
	return new String_API(wchars, _length);
}

void StringBuilder_API::increaseCapacity() {
	_capacity *= 2;
	Type::wchar* buffer = new Type::wchar[_capacity];
	System_API::arraycopy(_buffer, 0, buffer, 0, _length);
	delete [] _buffer;
	_buffer = buffer;
}

