/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Enum.hpp"
#include "javolution/util/FastMap.hpp"
#include "javolution/lang/IllegalArgumentException.hpp"

using namespace javolution::lang;
using namespace javolution::util;


// Local method to return the list of enums for the given type.
FastTable<Enum_ANY> getEnums(Class_ANY enumType) {
    static FastMap<Class_ANY, FastTable<Enum_ANY> > classToEnums
        = FastMap_API<Class_ANY, FastTable<Enum_ANY> >::newInstance();
    FastTable<Enum_ANY> enums;
    synchronized(classToEnums) {
        enums = classToEnums->get(enumType);
        if (enums != Type::Null) return enums;
        enums = FastTable_API<Enum_ANY>::newInstance();
        classToEnums->put(enumType, enums);
    }
    return enums;
}

Enum_ANY Enum_ANY_API::newInstance(Class_ANY enumType, String name, Type::int32 ordinal) {
    FastTable<Enum_ANY> enums = getEnums(enumType);
    Enum_ANY newEnum = new Enum_ANY_API(enumType, name->intern(), ordinal);
    enums->add(newEnum);
    return newEnum;
}

Enum_ANY Enum_ANY_API::newInstance(Class_ANY enumType, String name) {
    FastTable<Enum_ANY> enums = getEnums(enumType);
    Type::int32 n = enums->size();
    Type::int32 ordinalValue = (n != 0) ? enums->get(n-1)->ordinal() + 1 : 0;
    Enum_ANY newEnum = new Enum_ANY_API(enumType, name->intern(), ordinalValue);
    enums->add(newEnum);
    return newEnum;
}

Type::Array<Enum_ANY> Enum_ANY_API::values(Class_ANY enumType) {
    FastTable<Enum_ANY> enums = getEnums(enumType);
    Type::int32 n = enums->size();
    Type::Array< Enum_ANY > enumArray = Type::Array<Enum_ANY>(n);
    for (Type::int32 i=0; i < n; i++) {
        enumArray[i] = enums->get(i);
    }
    return enumArray;
}

Enum_ANY Enum_ANY_API::valueOf(Class_ANY enumType, String name) {
    FastTable<Enum_ANY> enums = getEnums(enumType);
    Type::int32 n = enums->size();
    for (Type::int32 i=0; i < n; i++) {
        if (name->equals(enums->get(i)->name()))
            return enums->get(i);
    }
    throw IllegalArgumentException_API::newInstance("Enum with name " + name + " is not defined");
}
