/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include  "javolution/lang/Integer32Test.hpp"
#include  "javolution/lang/Float64.hpp"
#include  "javolution/log/Logging.hpp"
#include  "javolution/time/TimeUTC.hpp"

using namespace javolution::lang;
using namespace javolution::lang;
using namespace javolution::log;
using namespace javolution::time;

void Integer32Test_API::testZero() {
    assertEquals(JAVOLUTION_LINE_INFO, 0, Integer32_API::valueOf(0)->intValue());
}

void Integer32Test_API::testNegative() {
    for (Type::int32 i=0; i < 256; i++) {
        assertEquals(JAVOLUTION_LINE_INFO, -i, Integer32_API::valueOf(-i)->intValue());
    }
}

void Integer32Test_API::testPositive() {
    for (Type::int32 i=0; i < 256; i++) {
        assertEquals(JAVOLUTION_LINE_INFO, i, Integer32_API::valueOf(i)->intValue());
    }
}

void Integer32Test_API::testCreationDeletionSpeed() {
	TimeUTC start = TimeUTC_API::current();
    for (Type::int32 i=0; i < 4096; i++) {
        Type::Array<Integer32> tmp = Type::Array<Integer32>(256);
        for (Type::int32 j=0; j < 256; j++) {
        	tmp[j] = j + 256;
        }
    }
    TimeUTC stop = TimeUTC_API::current();
    double seconds = stop-> getSecondsSinceEpoch() - start-> getSecondsSinceEpoch();
    Float64 objPerSec = 4096.0 * 256.0 / seconds;
    Logging_API::info(L"TEST", L"No Memory Cache - Creation/Deletion Speed ", objPerSec, L" obj/sec.");

	Object_API::getMemoryCache().enable(true);
	start = TimeUTC_API::current();
    for (Type::int32 i=0; i < 4096; i++) {
        Type::Array<Integer32> tmp = Type::Array<Integer32>(256);
        for (Type::int32 j=0; j < 256; j++) {
        	tmp[j] = j + 256;
        }
    }
    stop = TimeUTC_API::current();
	Object_API::getMemoryCache().enable(false);
    seconds = stop-> getSecondsSinceEpoch() - start-> getSecondsSinceEpoch();
    objPerSec = 4096.0 * 256.0 / seconds;
    Logging_API::info(L"TEST", L"With Memory Cache - Creation/Deletion Speed ", objPerSec, L" obj/sec.");
}
