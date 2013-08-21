/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_UTIL_FAST_SET_TEST_HPP
#define _JAVOLUTION_UTIL_FAST_SET_TEST_HPP

#include "javolution/util/FastSet.hpp"
#include "junit/framework/TestCase.hpp"

namespace javolution {
    namespace util {
        class FastSetTest_API;
    }
}

class javolution::util::FastSetTest_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(javolution::util::FastSetTest_API)
    ADD_TEST(testIsEmpty)
    ADD_TEST(testContains)
    ADD_TEST(testSize)
    END_SUITE()

    void testIsEmpty();
    void testContains();
    void testSize();

protected:
    void setUp();
private:
    javolution::util::FastSet<javolution::lang::String> set;
};

#endif
