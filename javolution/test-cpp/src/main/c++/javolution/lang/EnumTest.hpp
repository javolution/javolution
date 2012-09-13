/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_LANG_ENUM_TEST_HPP
#define _JAVOLUTION_LANG_ENUM_TEST_HPP

#include "javolution/lang/Enum.hpp"
#include "junit/framework/TestCase.hpp"

namespace javolution {
    namespace lang {
        class EnumTest_API;
    }
}

class javolution::lang::EnumTest_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(javolution::lang::EnumTest_API)
    ADD_TEST(testValues)
    ADD_TEST(testValueOf)
    ADD_TEST(testDeclaringClass)
    ADD_TEST(testName)
    ADD_TEST(testOrdinal)
    ADD_TEST(testToString)
    END_SUITE()

    class Color_API;
    typedef javolution::lang::Enum<Color_API> Color;
    class Color_API : public javolution::lang::Enum_API<Color_API> {
    public:
        static const Color UNKNOWN;
        static const Color BLUE;
        static const Color GREEN;
        static const Type::Array<Color> values();
    };

    void testForName();
    void testValues();
    void testValueOf();
    void testDeclaringClass();
    void testName();
    void testOrdinal();
    void testToString();
    void testEquals();
    void testHashCode();

};

#endif
