/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_STRING_BUILDER_HPP
#define _JAVOLUTION_LANG_STRING_BUILDER_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/Boolean.hpp"
#include "javolution/lang/Character.hpp"
#include "javolution/lang/IllegalArgumentException.hpp"
#include "javolution/lang/IndexOutOfBoundsException.hpp"
#include "javolution/lang/Integer32.hpp"
#include "javolution/lang/Integer64.hpp"
#include "javolution/lang/Float32.hpp"
#include "javolution/lang/Float64.hpp"

namespace javolution {
    namespace lang {
        class StringBuilder_API;
        typedef Type::Handle<StringBuilder_API> StringBuilder;
    }
}

/**
 * This class represents a mutable sequence of characters.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/StringBuilder.html">
 *       Java - StringBuilder</a>
 * @version 1.0
 */
class javolution::lang::StringBuilder_API : public virtual javolution::lang::Object_API {

    /**
     * Holds default capacity.
     */
    static const Type::int32 DEFAULT_CAPACITY = 16;

    /**
     * Holds the wide character buffer.
     */
    Type::int32 _capacity;

    /**
     * Holds the current length.
     */
    Type::int32 _length;

    /**
     * Holds the wide character buffer.
     */
    Type::wchar* _buffer;

protected:
    
    /**
     * Default constructor.
     */
    StringBuilder_API() : _capacity(DEFAULT_CAPACITY), _length(0),
    _buffer(new Type::wchar[DEFAULT_CAPACITY]) {
    };

public:

    /**
     * Returns a new empty string builder instance.
     */
    static StringBuilder newInstance() {
        return new StringBuilder_API();
    }

    /**
     * Returns the length (character count).
     *
     * @return the number of wide characters.
     */
     Type::int32 length() {
        return _length;
    }

    /**
     * Returns the character at the specified index.
     *
     * @param  i the index of the character.
     * @return the character at the specified index.
     * @throws IndexOutOfBoundsException if <code>(i < 0) || (i >= this.length())</code>.
     */
    Type::wchar charAt(Type::int32 i) {
        if ((i < 0) || (i >= _length))
            throw IndexOutOfBoundsException_API::newInstance();
        return _buffer[i];
    }

    /**
     * Appends the textual representation of the specified object
     * (<code>null</code> or <code>obj->toString()</code>).
     *
     * @param obj the object or <code>Type::Null</code>
     * @return <code>this</code> 
     */
    JAVOLUTION_DLL StringBuilder append(Object const& obj);

    /**
     * Appends the specified C++ wide characters (null terminated).
     *
     * @param wchars the wide characters (cannot be NULL).
     * @return <code>this</code>
     */
    JAVOLUTION_DLL StringBuilder append(const wchar_t* wchars);

    /**
     * Appends the specified C++ wide string.
     *
     * @param wstr the wide string.
     * @return <code>this</code>
     */
    StringBuilder append(const std::wstring& wstr) {
    	return append(wstr.c_str());
    }

    /**
     * Appends the specified C++ UTF-8 simple characters (null terminated).
     *
     * @param chars the UTF-8 characters (cannot be NULL).
     * @return <code>this</code>
     */
   JAVOLUTION_DLL StringBuilder append(const char* chars);

    /**
     * Appends the specified C++ string (UTF-8).
     *
     * @param str the string.
     * @return <code>this</code>
     */
    StringBuilder append(const std::string& str) {
    	return append(str.c_str());
    }

    /**
     * Appends the specified ASCII character.
     *
     * @param value the character value.
     * @return <code>this</code>
     * @throw IllegalArgumentException if the specified value is not an ASCII
     *        character.
     */
    StringBuilder append(char value) {
    	if (value < 0) throw IllegalArgumentException_API::newInstance(L"Non-ASCII Character");
    	if (_length >= _capacity) increaseCapacity();
    	_buffer[_length++] = (Type::wchar) value;
    	return this;
    }

    /**
     * Appends the specified wide character.
     *
     * @param value the character value.
     * @return <code>this</code>
     */
    StringBuilder append(Type::wchar value) {
    	if (_length >= _capacity) increaseCapacity();
    	_buffer[_length++] = value;
    	return this;
    }

    /**
     * Appends the specified 8 bits integer value.
     *
     * @param value the 8 bits integer value.
     * @return <code>this</code> or equivalent.
     */
    StringBuilder append(Type::int8 value) {
        return append((Type::int32) value);
    }

    /**
     * Appends the specified 16 bits integer value.
     *
     * @param value the 16 bits integer value.
     * @return <code>this</code> or equivalent.
     */
    StringBuilder append(Type::int16 value) {
    	return append((Type::int32) value);
    }

    /**
     * Appends the specified 32 bits integer value.
     *
     * @param value the 32 bits integer value.
     * @return <code>this</code> or equivalent.
     */
    JAVOLUTION_DLL StringBuilder append(Type::int32 value);

    /**
     * Appends the specified 64 bits integer value.
     *
     * @param value the 64 bits integer value.
     * @return <code>this</code> or equivalent.
     */
    JAVOLUTION_DLL StringBuilder append(Type::int64 value);

    /**
     * Appends the specified 32 bits float value.
     *
     * @param value the 32 bits float value.
     * @return <code>this</code> or equivalent.
     */
    JAVOLUTION_DLL StringBuilder append(Type::float32 value);

    /**
     * Appends the specified 64 bits float value.
     *
     * @param value the 64 bits float value.
     * @return <code>this</code> or equivalent.
     */
    JAVOLUTION_DLL StringBuilder append(Type::float64 value);

    /**
     * Appends the specified boolean object.
     *
     * @param value the boolean value.
     * @return <code>this</code>
     */
    StringBuilder append(Type::boolean value) {
    	return append(value ? L"true" : L"false");
    }

    /**
     * Returns a string representation of this string builder.
     *
     * @return the corresponding string.
     */
    JAVOLUTION_DLL virtual String toString() const;

    /**
     * Overrides destructor to ensure internal array deallocation.
     */
	virtual ~StringBuilder_API() {
		delete [] _buffer;
	}

	/**
	 * Equivalent to <code>append(b->booleanValue())</code>
	 */
	StringBuilder append(Boolean const& b) {
		return append(b->booleanValue());
	}

	/**
	 * Equivalent to <code>append(c->charValue())</code>
	 */
	StringBuilder append(Character const& c) {
		return append(c->charValue());
	}

	/**
	 * Equivalent to <code>append(i32->intValue())</code>
	 */
	StringBuilder append(javolution::lang::Integer32 const& i32) {
		return append(i32->intValue());
	}

	/**
	 * Equivalent to <code>append(i64->longValue())</code>
	 */
	StringBuilder append(javolution::lang::Integer64 const& i64) {
		return append(i64->longValue());
	}

	/**
	 * Equivalent to <code>append(f32->floatValue())</code>
	 */
	StringBuilder append(javolution::lang::Float32 const& f32) {
		return append(f32->floatValue());
	}

	/**
	 * Equivalent to <code>append(f64->doubleValue())</code>
	 */
	StringBuilder append(javolution::lang::Float64 const& f64) {
		return append(f64->doubleValue());
	}

private:

    /**
     * Increases the capacity of the buffer.
     */
    JAVOLUTION_DLL void increaseCapacity();

};

#endif
