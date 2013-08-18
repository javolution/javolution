/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JUNIT_FRAMEWORK_TEST_CASE_HPP
#define _JUNIT_FRAMEWORK_TEST_CASE_HPP

#include "junit/framework/Assert.hpp"
#include "junit/framework/Test.hpp"
#include "junit/framework/TestResult.hpp"
#include "junit/framework/TestSuite.hpp" // See macro dependency.

// Defines macro to help definition of test suite.
#define BEGIN_SUITE(name)  \
    typedef name thisType; \
    typedef void (thisType::* TestMethod)(); \
    TestMethod method; \
    static junit::framework::TestSuite suite() { \
        junit::framework::TestSuite tests = new junit::framework::TestSuite_API( javolution::lang::String_API::valueOf(#name) );

#define ADD_TEST(testMethod)  \
        thisType* testMethod##TestCasePtr = new thisType(); \
        testMethod##TestCasePtr->name = javolution::lang::String_API::valueOf(#testMethod); \
        testMethod##TestCasePtr->method = &thisType::testMethod; \
        junit::framework::TestCase testMethod##TestCase = testMethod##TestCasePtr; \
        tests->addTest(testMethod##TestCase);

#define END_SUITE()  \
        return tests; \
    } \
    virtual void runTest() { \
        (*this.*method)(); \
    }

namespace junit {
    namespace framework {
        class TestCase_API;
        typedef Type::Handle<TestCase_API> TestCase;
    }
}

/**
 * This class represents the fixture to run multiple tests.
 * For example:<pre><code>
 *
 *     #include "com/bar/Foo.hpp" // Class to be tested.
 *     #include "junit/framework/TestCase.hpp"
 *
 *     namespace com {
 *         namespace bar {
 *            class FooTest_API;
 *        }
 *     }
 *     class com::bar::FooTest_API : public junit::framework::TestCase_API {
 *     public:
 *         BEGIN_SUITE(com::bar::FooTest_API) // Defines the test suite for Foo
 *         ADD_TEST(testXXX)
 *         ADD_TEST(testYYY)
 *         END_SUITE()
 *
 *         void testXXX() { ... };
 *         void testYYY() { ... };
 *     }
 *     </code></pre></p>
 *
 * @see  <a href="http://junit.org/apidocs/junit/framework/TestCase.html">
 *       JUnit - TestCase</a>
 * @version 1.0
 */
class junit::framework::TestCase_API : public junit::framework::Assert_API,
public virtual junit::framework::Test_API {
    friend class TestResult_API; // Gives access to protected member TestCase_API::runBare
protected:

    /**
     * Holds the test name (to be set before running the test).
     */
    javolution::lang::String name;

    javolution::lang::String dummy; // For alignment purpose. TODO Try to remove.

public:

    /**
     *  Returns the name of the test case.
     */
    virtual javolution::lang::String const& getName() {
        return name;
    }

    // Implements junit::framework::Test

    virtual Type::int32 countTestCases() {
        return 1;
    }

    // Implements junit::framework::Test
    virtual void run(TestResult const& result) {
        result->run(this);
    }

protected:

    /**
     * Runs the test (to be overriden by sub-classes to run the actual test method).
     */
    virtual void runTest() = 0;

    /**
     *  Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    virtual void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    virtual void tearDown() {
    }

    /**
     * Runs the bare test sequence.
     *
     * @throws RuntimeException if an exception occurs.
     */
    virtual void runBare() {
        setUp();
        try {
            runTest();
        } catch (...) {
            tearDown();
            throw; // Rethrows.
        }
        tearDown();
    }

};

#endif
