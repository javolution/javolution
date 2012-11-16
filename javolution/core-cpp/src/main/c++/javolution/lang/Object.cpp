/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "javolution/lang/Class.hpp"
#include "javolution/lang/UnsupportedOperationException.hpp"
#include <typeinfo> // Used for Object_API::getClass() (C++ Reflection)

using namespace Javolution;
using namespace std;
using namespace javolution::lang;

Javolution::MemoryCache javolution::lang::Object_API::_memoryCache;

Class_ANY Object_API::getClass() const {
    String name = String_API::valueOf(typeid(*this).name());
    if (name->startsWith(L"class ")) {
    	name = name->substring(6);
    }
    return Class_ANY_API::forName(name);
}

String Object_API::toString() const {
    void const* thisAddress = dynamic_cast<void const*> (this);
    intptr_t address = (intptr_t) thisAddress;
    StringBuilder sb = StringBuilder_API::newInstance();
    return sb->append(L"Object#")->append((Type::int32) address)->toString();
}

Type::int32 Object_API::hashCode() const {
    void const * address = dynamic_cast<void const *> (this);
    intptr_t a = (intptr_t) address;
    // Ensures uniform distribution.
    Type::int32 h = (Type::int32) a;
    h += ~(h << 9);
    h ^= (h >> 14);
    h += (h << 4);
    return h ^ (h >> 10);
}

Type::Mutex& Object_API::getMutex() const {
    throw javolution::lang::UnsupportedOperationException_API::newInstance(L"getMutex() not implemented");
}

std::ostream& operator<<(std::ostream& out, javolution::lang::Object_API const& that) {
	return out << that.toString()->toUTF8();
}
std::wostream& operator<<(std::wostream& wout, javolution::lang::Object_API const& that) {
    return wout << that.toString()->toWString();
}

std::string operator+(std::string const& left, javolution::lang::Object_API const& right) {
    return left + right.toString()->toUTF8();
}

std::string operator+(javolution::lang::Object_API const& left, std::string const& right) {
    return left.toString()->toUTF8() + right;
}

std::wstring operator+(std::wstring const& left, javolution::lang::Object_API const& right) {
    return left + right.toString()->toWString();
}

std::wstring operator+(javolution::lang::Object_API const& left, std::wstring const& right) {
    return left.toString()->toWString() + right;
}

