/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_UTIL_HASH_MAP_TEST_HPP
#define _JAVOLUTION_UTIL_HASH_MAP_TEST_HPP

#include "javolution/util/FastMap.hpp"
#include "junit/framework/TestCase.hpp"

namespace javolution {
    namespace util {
        class FastMapTest_API;
    }
}

class javolution::util::FastMapTest_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(javolution::util::FastMapTest_API)
    ADD_TEST(testIsEmpty)
    ADD_TEST(testGet)
    END_SUITE()

    void testIsEmpty();
    void testGet();

protected:
    void setUp();
private:
    javolution::util::Map<javolution::lang::String, javolution::lang::String> map;
};
#endif
