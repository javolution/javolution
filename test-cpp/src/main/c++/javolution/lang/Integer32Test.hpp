/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_LANG_INTEGER32_TEST_HPP
#define _JAVOLUTION_LANG_INTEGER32_TEST_HPP

#include "javolution/lang/Integer32.hpp"
#include "junit/framework/TestCase.hpp"

namespace javolution {
    namespace lang {
        class Integer32Test_API;
    }
}

class javolution::lang::Integer32Test_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(javolution::lang::Integer32Test_API)
    ADD_TEST(testZero)
    ADD_TEST(testNegative)
    ADD_TEST(testPositive)
    ADD_TEST(testCreationDeletionSpeed)
    END_SUITE()

    void testZero();
    void testNegative();
    void testPositive();
    void testCreationDeletionSpeed();

};

#endif
