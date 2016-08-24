/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JUNIT_FRAMEWORK_TEST_RESULT_HPP
#define _JUNIT_FRAMEWORK_TEST_RESULT_HPP

#include <iostream>
#include <fstream>
#include "javolution/lang/Object.hpp"
#include "javolution/lang/Throwable.hpp"
#include "junit/framework/Test.hpp"
#include "junit/framework/AssertionFailedError.hpp"

namespace junit {
    namespace framework {
        class TestResult_API;
        typedef Type::Handle<TestResult_API> TestResult;
        class TestCase_API; // Forward reference.
        typedef Type::Handle<TestCase_API> TestCase;
    }
}

/**
 * This class collects the results of executing test cases.
 *
 * This class can be extended in order to log the test results
 * in custom formats (e.g. XML). The default implementation
 * of startTest/endTest/addFailure/addError output the test results to
 * the standard <code>std::wcout</code> output.
 *
 * @see  <a href="http://junit.org/apidocs/junit/framework/TestResult.html">
 *       JUnit - TestResult</a>
 * @version 1.0
 */
class junit::framework::TestResult_API : public virtual javolution::lang::Object_API {
    friend class TestCase_API; // Gives access to protected member TestResult_API::run

    Type::int32 _runCount;
    Type::int32 _failureCount;
    Type::int32 _errorCount;

public:

    /**
      * Default constructor.
      */
     TestResult_API() : _runCount(0), _failureCount(0), _errorCount(0) {
     };

     /**
     * Returns a default test suite instance.
     */
    static TestResult newInstance() {
        return TestResult(new TestResult_API());
    }

    /**
     * Gets the number of test run.
     */
    Type::int32 runCount() {
        return _runCount;
    }

    /**
     * Gets the number of detected failures.
     */
    Type::int32 failureCount() {
        return _failureCount;
    }

    /**
     * Gets the number of error.
     */
    Type::int32 errorCount() {
        return _errorCount;
    }

    /**
     * Informs the result that a test was started.
     */
    JAVOLUTION_DLL virtual void startTest(Test test);

    /**
     * Informs the result that a test was completed.
     */
    JAVOLUTION_DLL virtual void endTest(Test test);

    /**
     * Adds an error to the list of errors. The passed in exception
     * is the caused of error.
     */
    JAVOLUTION_DLL virtual void addError(Test test, javolution::lang::Throwable error);

    /**
     * Adds an assertion failureto the list of failure. The passed in exception
     * is the caused of failure.
     */
    JAVOLUTION_DLL virtual void addFailure(Test test, AssertionFailedError failure);

    /**
     * Prints the test result summary (counters). Returns the number of
     * error plus failure (<code>0</code> if everything is fine).
     */
    JAVOLUTION_DLL virtual Type::int32 printSummary();

protected:

    /**
     * Runs a test case.
     */
    JAVOLUTION_DLL virtual void run(TestCase testCase);

};

#endif
