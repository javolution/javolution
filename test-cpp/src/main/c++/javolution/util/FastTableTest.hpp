/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_UTIL_FAST_MAP_TEST_HPP
#define _JAVOLUTION_UTIL_FAST_MAP_TEST_HPP

#include "javolution/util/FastTable.hpp"
#include "junit/framework/TestCase.hpp"

namespace javolution {
    namespace util {
        class FastTableTest_API;
    }
}

class javolution::util::FastTableTest_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(javolution::util::FastTableTest_API)
    ADD_TEST(testIsEmpty)
    ADD_TEST(testIterator)
    ADD_TEST(testSubList)
    ADD_TEST(testCreationDeletionSpeed)
    END_SUITE()

    void testIsEmpty();
    void testIterator();
    void testSubList();
    void testCreationDeletionSpeed();

protected:
    void setUp();
private:
    javolution::util::List<javolution::lang::String> list;
};
#endif
