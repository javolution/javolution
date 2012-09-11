/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JUNIT_FRAMEWORK_ASSERT_HPP
#define _JUNIT_FRAMEWORK_ASSERT_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "junit/framework/AssertionFailedError.hpp"

// Defines a string holding information on the current line number. It can be used as default assert messages, e.g. ASSERT_TRUE(test)
#define ASSERT_TRUE(condition) assertTrue(JAVOLUTION_LINE_INFO, condition)
#define ASSERT_FALSE(condition) assertFalse(JAVOLUTION_LINE_INFO, condition)
#define ASSERT_EQUALS(expected, actual) assertEquals(JAVOLUTION_LINE_INFO, expected, actual)
#define ASSERT_EQUALS_DELTA(expected, actual, delta) assertEquals(JAVOLUTION_LINE_INFO, expected, actual, delta)
#define ASSERT_NULL(object) assertNull(JAVOLUTION_LINE_INFO, object)
#define ASSERT_NOT_NULL(object) assertNotNull(JAVOLUTION_LINE_INFO, object)
#define ASSERT_SAME(expected, actual) assertSame(JAVOLUTION_LINE_INFO, expected, actual)
#define ASSERT_NOT_SAME(expected, actual) assertNotSame(JAVOLUTION_LINE_INFO, expected, actual)
#define FAIL() fail(JAVOLUTION_LINE_INFO)

namespace junit {
    namespace framework {
        class Assert_API;
        typedef Type::Handle<Assert_API> Assert;
    }
}

/**
 * This class represents a set of assert methods.
 * Messages are only displayed when an assert fails.
 *
 * @see  <a href="http://junit.org/apidocs/junit/framework/Assert.html">
 *       JUnit - Assert</a>
 * @version 1.0
 */
class junit::framework::Assert_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Asserts that a condition is true. If it isn't it throws
     * an AssertionFailedError with the given message.
     */
    static void assertTrue(javolution::lang::String message, Type::boolean condition) {
        if (!condition)
            fail(message);
    }

    /**
     * Asserts that a condition is true. If it isn't it throws
     * an AssertionFailedError.
     */
    static void assertTrue(Type::boolean condition) {
        assertTrue(L"assertTrue(Type::boolean)", condition);
    }

    /**
     * Asserts that a condition is false. If it isn't it throws
     * an AssertionFailedError with the given message.
     */
    static void assertFalse(javolution::lang::String message, Type::boolean condition) {
        assertTrue(message, !condition);
    }

    /**
     * Asserts that a condition is false. If it isn't it throws
     * an AssertionFailedError.
     */
    static void assertFalse(Type::boolean condition) {
        assertFalse(L"assertFalse(Type::boolean)", condition);
    }

    /**
     * Fails a test with the given message.
     */
    static void fail(javolution::lang::String message) {
        throw AssertionFailedError_API::newInstance(message);
    }

    /**
     * Fails a test with no message.
     */
    static void fail() {
        fail(L"fail()");
    }

    /**
     * Asserts that two objects are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertEquals(javolution::lang::String message, javolution::lang::Object expected, javolution::lang::Object actual) {
        if (expected == Type::Null && actual == Type::Null)
            return;
        if (expected != Type::Null && expected->equals(actual))
            return;
        failNotEquals(message, expected, actual);
    }

    /**
     * Asserts that two objects are equal. If they are not
     * an AssertionFailedError is thrown.
     */
    static void assertEquals(javolution::lang::Object expected, javolution::lang::Object actual) {
        assertEquals(L"assertEquals(javolution::lang::Object, javolution::lang::Object)", expected, actual);
    }

    /**
     * Asserts that two strings are equal.
     */
    static void assertEquals(javolution::lang::String message, javolution::lang::String const& expected, javolution::lang::String const& actual) {
        if (expected == Type::Null && actual == Type::Null)
            return;
        if (expected != Type::Null && expected->equals(actual))
            return;
        throw AssertionFailedError_API::newInstance(format(message, expected, actual));
    }

    /**
     * Asserts that two javolution::lang::Strings are equal.
     */
    static void assertEquals(javolution::lang::String expected, javolution::lang::String actual) {
        assertEquals(L"assertEquals(javolution::lang::String, javolution::lang::String)", expected, actual);
    }

    /**
     * Asserts that two doubles are equal concerning a delta.  If they are not
     * an AssertionFailedError is thrown with the given message.  If the expected
     * value is infinity then the delta value is ignored.
     */
    static void assertEquals(javolution::lang::String message, Type::float64 expected, Type::float64 actual, Type::float64 delta) {
        if (!(abs(expected - actual) <= delta))
            failNotEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two doubles are equal concerning a delta. If the expected
     * value is infinity then the delta value is ignored.
     */
    static void assertEquals(Type::float64 expected, Type::float64 actual, Type::float64 delta) {
        assertEquals(L"assertEquals(Type::float64, Type::float64, Type::float64)", expected, actual, delta);
    }

    /**
     * Asserts that two floats are equal concerning a positive delta. If they
     * are not an AssertionFailedError is thrown with the given message. If the
     * expected value is infinity then the delta value is ignored.
     */
    static void assertEquals(javolution::lang::String message, Type::float32 expected, Type::float32 actual, Type::float32 delta) {
        if (!(abs(expected - actual) <= delta))
            failNotEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two floats are equal concerning a delta. If the expected
     * value is infinity then the delta value is ignored.
     */
    static void assertEquals(Type::float32 expected, Type::float32 actual, Type::float32 delta) {
        assertEquals(L"assertEquals(Type::float32, Type::float32, Type::float32)", expected, actual, delta);
    }

    /**
     * Asserts that two longs are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertEquals(javolution::lang::String message, Type::int64 expected, Type::int64 actual) {
        assertEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two longs are equal.
     */
    static void assertEquals(Type::int64 expected, Type::int64 actual) {
        assertEquals(L"assertEquals(Type::int64, Type::int64)", expected, actual);
    }

    /**
     * Asserts that two Type::boolean are equals. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertEquals(javolution::lang::String message, Type::boolean expected, Type::boolean actual) {
        if (expected != actual)
            failNotEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two Type::boolean are equals.
     */
    static void assertEquals(Type::boolean expected, Type::boolean actual) {
        assertEquals(L"assertEquals(Type::boolean, Type::boolean)", expected, actual);
    }

    /**
     * Asserts that two bytes are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertEquals(javolution::lang::String message, Type::int8 expected, Type::int8 actual) {
        assertEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two bytes are equal.
     */
    static void assertEquals(Type::int8 expected, Type::int8 actual) {
        assertEquals(L"assertEquals(Type::int8, Type::int8)", expected, actual);
    }

    /**
     * Asserts that two chars are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertEquals(javolution::lang::String message, Type::wchar expected, Type::wchar actual) {
        assertEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two chars are equal.
     */
    static void assertEquals(Type::wchar expected, Type::wchar actual) {
        assertEquals(L"assertEquals(Type::wchar, Type::wchar)", expected, actual);
    }

    /**
     * Asserts that two shorts are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertEquals(javolution::lang::String message, Type::int16 expected, Type::int16 actual) {
        assertEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two shorts are equal.
     */
    static void assertEquals(Type::int16 expected, Type::int16 actual) {
        assertEquals(L"assertEquals(Type::int16, Type::int16)", expected, actual);
    }

    /**
     * Asserts that two ints are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertEquals(javolution::lang::String message, Type::int32 expected, Type::int32 actual) {
        assertEquals(message, javolution::lang::String_API::valueOf(expected), javolution::lang::String_API::valueOf(actual));
    }

    /**
     * Asserts that two ints are equal.
     */
    static void assertEquals(Type::int32 expected, Type::int32 actual) {
        assertEquals(L"assertEquals(Type::int32, Type::int32)", expected, actual);
    }

    /**
     * Asserts that an object isn't Type::Null.
     */
    static void assertNotNull(javolution::lang::Object object) {
        assertNotNull(L"assertNotNull(javolution::lang::Object)", object);
    }

    /**
     * Asserts that an object isn't Type::Null. If it is
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertNotNull(javolution::lang::String message, javolution::lang::Object object) {
        assertTrue(message, object != Type::Null);
    }

    /**
     * Asserts that an object is Type::Null.
     */
    static void assertNull(javolution::lang::Object object) {
        assertNull(L"assertNull(javolution::lang::Object)", object);
    }

    /**
     * Asserts that an object is Type::Null.  If it is not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertNull(javolution::lang::String message, javolution::lang::Object object) {
        assertTrue(message, object == Type::Null);
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static void assertSame(javolution::lang::String message, javolution::lang::Object expected, javolution::lang::Object actual) {
        if (expected == actual)
            return;
        failNotSame(message, expected, actual);
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * the same an AssertionFailedError is thrown.
     */
    static void assertSame(javolution::lang::Object expected, javolution::lang::Object actual) {
        assertSame(L"assertSame(javolution::lang::Object, javolution::lang::Object)", expected, actual);
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError is thrown with the
     * given message.
     */
    static void assertNotSame(javolution::lang::String message, javolution::lang::Object expected, javolution::lang::Object actual) {
        if (expected != actual) return;
        failSame(message);
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError is thrown.
     */
    static void assertNotSame(javolution::lang::Object expected, javolution::lang::Object actual) {
        assertNotSame(L"assertSame(javolution::lang::Object, javolution::lang::Object)", expected, actual);
    }


private:

    static void failSame(javolution::lang::String message) {
        javolution::lang::StringBuilder tmp = new javolution::lang::StringBuilder_API();
        tmp->append(message)->append(L" expected not same");
        fail(tmp->toString());
    }

    static void failNotSame(javolution::lang::String message, javolution::lang::Object expected, javolution::lang::Object actual) {
        javolution::lang::StringBuilder tmp = new javolution::lang::StringBuilder_API();
        tmp->append(message)
                ->append(L" expected same:<")
                ->append(expected)
                ->append(L"> was not:<")
                ->append(actual)
                ->append(L">");
        fail(tmp->toString());
    }

    static void failNotEquals(javolution::lang::String message, javolution::lang::Object expected, javolution::lang::Object actual) {
        fail(format(message, expected, actual));
    }

    static javolution::lang::String format(javolution::lang::String message, javolution::lang::Object expected, javolution::lang::Object actual) {
        javolution::lang::StringBuilder tmp = new javolution::lang::StringBuilder_API();
        tmp->append(message)
                ->append(L" expected:<")
                ->append(expected)
                ->append(L"> but was:<")
                ->append(actual)
                ->append(L">");
        return tmp->toString();
    }

    static Type::float64 abs(Type::float64 value) {
        return value < 0 ? -value : value;
    }

    static Type::float32 abs(Type::float32 value) {
        return value < 0 ? -value : value;
    }

};

#endif
