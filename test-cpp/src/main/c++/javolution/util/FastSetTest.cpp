/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/util/FastSetTest.hpp"

using namespace javolution::lang;
using namespace javolution::util;

void FastSetTest_API::setUp() {
    set = FastSet_API<String>::newInstance();
    set->add(L"First");
    set->add(L"Second");
    set->add(L"Second"); // Duplicate should be ignored.
 }

void FastSetTest_API::testIsEmpty() {
    assertFalse(JAVOLUTION_LINE_INFO, set->isEmpty());
    set->clear();
    assertTrue(JAVOLUTION_LINE_INFO, set->isEmpty());
}

void FastSetTest_API::testContains() {
    assertTrue(JAVOLUTION_LINE_INFO, set->contains(String_API::valueOf(L"First")));
    assertTrue(JAVOLUTION_LINE_INFO, set->contains(String_API::valueOf(L"Second")));
}

void FastSetTest_API::testSize() {
    assertEquals(JAVOLUTION_LINE_INFO, 2, set->size());
    set->clear();
    assertEquals(JAVOLUTION_LINE_INFO, 0, set->size());
 }
