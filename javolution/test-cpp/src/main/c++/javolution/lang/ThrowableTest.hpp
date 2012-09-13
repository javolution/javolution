/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_LANG_THROWABLE_TEST_HPP
#define _JAVOLUTION_LANG_THROWABLE_TEST_HPP

#include "javolution/lang/Throwable.hpp"
#include "junit/framework/TestCase.hpp"

namespace javolution {
    namespace lang {
        class ThrowableTest_API;
    }
}

class javolution::lang::ThrowableTest_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(javolution::lang::ThrowableTest_API)
    ADD_TEST(testPrintStack)
    END_SUITE()

    void testPrintStack();

};

#endif
