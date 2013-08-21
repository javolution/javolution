/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/lang/EnumTest.hpp"
#include "javolution/log/Logging.hpp"
#include "javolution/lang/Character.hpp"

using namespace javolution::lang;
using namespace javolution::util;
using namespace javolution::log;

// Implementation Color Enum ///
const Class<EnumTest_API::Color_API> CLASS = Class_API<EnumTest_API::Color_API>::forName(L"javolution.lang.EnumTest.Color");

const EnumTest_API::Color EnumTest_API::Color_API::UNKNOWN = Enum_API<EnumTest_API::Color_API>::newInstance(CLASS, L"unknown", -1);
const EnumTest_API::Color EnumTest_API::Color_API::BLUE = Enum_API<EnumTest_API::Color_API>::newInstance(CLASS, L"blue");
const EnumTest_API::Color EnumTest_API::Color_API::GREEN = Enum_API<EnumTest_API::Color_API>::newInstance(CLASS, L"green");

const Type::Array<EnumTest_API::Color> VALUES = Enum_API<EnumTest_API::Color_API>::values(CLASS);
const Type::Array<EnumTest_API::Color> EnumTest_API::Color_API::values() {
     return VALUES;
}

void EnumTest_API::testValues() {
    Type::Array<EnumTest_API::Color> colors =  EnumTest_API::Color_API::values();
    assertEquals(JAVOLUTION_LINE_INFO, 3, colors.length);
    assertSame(JAVOLUTION_LINE_INFO, Color_API::UNKNOWN, colors[0]);
    assertSame(JAVOLUTION_LINE_INFO, Color_API::BLUE, colors[1]);
    assertSame(JAVOLUTION_LINE_INFO, Color_API::GREEN, colors[2]);
    assertFalse(JAVOLUTION_LINE_INFO, Color_API::GREEN == colors[1]); // Sanity check.
}

void EnumTest_API::testValueOf() {
    assertSame(JAVOLUTION_LINE_INFO, Color_API::GREEN, EnumTest_API::Color_API::valueOf(CLASS, L"green"));
    try {
    	Logging_API::setLogErrorEnabled(L"Javolution", false); // To avoid polluting the trace.
        EnumTest_API::Color_API::valueOf(CLASS, L"????");
        fail(L"Exception should have been raised");
    } catch (IllegalArgumentException ex) {
    	Logging_API::setLogErrorEnabled(L"Javolution", true);
        // Ok.
    }
}

void EnumTest_API::testDeclaringClass() {
    assertSame(JAVOLUTION_LINE_INFO, CLASS, Color_API::UNKNOWN->getDeclaringClass());
    assertSame(JAVOLUTION_LINE_INFO, CLASS, Color_API::BLUE->getDeclaringClass());
    assertSame(JAVOLUTION_LINE_INFO, CLASS, Color_API::GREEN->getDeclaringClass());
}

void EnumTest_API::testName() {
    assertEquals(JAVOLUTION_LINE_INFO, L"unknown", Color_API::UNKNOWN->name());
    assertEquals(JAVOLUTION_LINE_INFO, L"blue", Color_API::BLUE->name());
    assertEquals(JAVOLUTION_LINE_INFO, L"green", Color_API::GREEN->name());
}

void EnumTest_API::testOrdinal() {
    assertEquals(JAVOLUTION_LINE_INFO, -1, Color_API::UNKNOWN->ordinal());
    assertEquals(JAVOLUTION_LINE_INFO, 0, Color_API::BLUE->ordinal());
    assertEquals(JAVOLUTION_LINE_INFO, 1, Color_API::GREEN->ordinal());
}

void EnumTest_API::testToString() {
    assertEquals(JAVOLUTION_LINE_INFO, L"unknown", Color_API::UNKNOWN->toString());
    assertEquals(JAVOLUTION_LINE_INFO, L"blue", Color_API::BLUE->toString());
    assertEquals(JAVOLUTION_LINE_INFO, L"green", Color_API::GREEN->toString());
}
