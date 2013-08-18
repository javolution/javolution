/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/util/FastMapTest.hpp"

using namespace javolution::lang;
using namespace javolution::util;

void FastMapTest_API::setUp() {
    map = FastMap_API<String, String>::newInstance();
    for (Type::int32 i=0; i < 10000; i++) {
        map->put(String_API::valueOf(i), String_API::valueOf(i+1));
    }  // Map contains key strings from [0..10000[
    for (Type::int32 i=5000; i < 15000; i++) {
        map->remove(String_API::valueOf(i));
    }  // Map contains key strings from [0..5000[
    for (Type::int32 i=2500; i < 7500; i++) {
        map->put(String_API::valueOf(i), String_API::valueOf(i+1));
    } // Map contains key strings from [0..7500[
}

void FastMapTest_API::testIsEmpty() {
    assertFalse(JAVOLUTION_LINE_INFO, map->isEmpty());
    for (Type::int32 i=0; i < 7500; i++) { // Remove all elements.
        map->remove(String_API::valueOf(i));
    }
    assertTrue(JAVOLUTION_LINE_INFO, map->isEmpty());
    map->put(L"", Type::Null);
    assertFalse(JAVOLUTION_LINE_INFO, map->isEmpty());
    map->clear();
    assertTrue(JAVOLUTION_LINE_INFO, map->isEmpty());
}

void FastMapTest_API::testGet() {
    for (Type::int32 i=0; i < 7500; i++) {
        assertEquals(JAVOLUTION_LINE_INFO, String_API::valueOf(i+1), map->get(String_API::valueOf(i)));
    }
    for (Type::int32 i=7500; i < 15000; i++) {
        assertEquals(JAVOLUTION_LINE_INFO, Type::Null, map->get(String_API::valueOf(i)));
    }
}
