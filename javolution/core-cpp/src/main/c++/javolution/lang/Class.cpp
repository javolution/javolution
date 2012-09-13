/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Class.hpp"
#include "javolution/lang/Error.hpp"
#include "javolution/util/FastMap.hpp"

using namespace javolution::lang;
using namespace javolution::util;


Class_ANY Class_ANY_API::forName(String className) { // Maintains unicity.
    static FastMap<String, Class_ANY> nameToClass = FastMap_API<String, Class_ANY>::newInstance();
    Class_ANY newClass;
    synchronized (nameToClass) {
        Class_ANY existingClass = nameToClass->get(className);
        if (existingClass != Type::Null) return existingClass;
        newClass = new Class_ANY_API(className);
        nameToClass->put(className->intern(), newClass);
    }
    return newClass;
}
