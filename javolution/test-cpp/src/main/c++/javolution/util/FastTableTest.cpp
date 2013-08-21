/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/util/FastTableTest.hpp"
#include  "javolution/lang/Float64.hpp"
#include "javolution/lang/Integer32.hpp"
#include  "javolution/log/Logging.hpp"
#include  "javolution/time/TimeUTC.hpp"

using namespace javolution::lang;
using namespace javolution::util;
using namespace javolution::lang;
using namespace javolution::log;
using namespace javolution::time;

void FastTableTest_API::setUp() {
    list = FastTable_API<String>::newInstance();
    list->add(L"First");
    list->add(Type::Null);
    list->add(L"Second");
}

void FastTableTest_API::testIsEmpty() {
    assertFalse(JAVOLUTION_LINE_INFO, list->isEmpty());
    list->clear();
    assertTrue(JAVOLUTION_LINE_INFO, list->isEmpty());
}

void FastTableTest_API::testIterator() {
    Iterator<String> i = list->iterator();
    assertTrue(JAVOLUTION_LINE_INFO, i->hasNext());
    assertEquals(JAVOLUTION_LINE_INFO, L"First", i->next());
    assertTrue(JAVOLUTION_LINE_INFO, i->hasNext());
    assertNull(JAVOLUTION_LINE_INFO, i->next());
    assertTrue(JAVOLUTION_LINE_INFO, i->hasNext());
    assertEquals(JAVOLUTION_LINE_INFO, L"Second", i->next());
    assertFalse(JAVOLUTION_LINE_INFO, i->hasNext());
}

void FastTableTest_API::testSubList() {
    Iterator<String> i = list->subList(0, 1)->iterator();
    assertTrue(JAVOLUTION_LINE_INFO, i->hasNext());
    assertEquals(JAVOLUTION_LINE_INFO, L"First", i->next());
    assertFalse(JAVOLUTION_LINE_INFO, i->hasNext());
}

void FastTableTest_API::testCreationDeletionSpeed() {
    Type::int32 count = 1000000;
	TimeUTC start = TimeUTC_API::current();
	for (int i=0; i < count; i++) {
		FastTable<Integer32> tmp = new FastTable_API<Integer32>();
	}
    TimeUTC stop = TimeUTC_API::current();
    double seconds = stop-> getSecondsSinceEpoch() - start-> getSecondsSinceEpoch();
    Float64 listPerSec = count / seconds;
    Logging_API::info(L"TEST", L"Creation/Deletion Speed ", listPerSec, L" list/sec.");
}

