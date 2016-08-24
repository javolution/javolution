/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JUNIT_FRAMEWORK_TEST_HPP
#define _JUNIT_FRAMEWORK_TEST_HPP

#include "javolution/lang/Object.hpp"

namespace junit {
    namespace framework {
        class Test_API;
        typedef Type::Handle<Test_API> Test;
        class TestResult_API; // Forward reference.
        typedef Type::Handle<TestResult_API> TestResult;
    }
}

/**
 * This interfacerepresents a test which can be run and collect its results.
 *
 * @see  <a href="http://junit.org/apidocs/junit/framework/Test.html">
 *       JUnit - Test</a>
 * @version 1.0
 */
class junit::framework::Test_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Counts the number of test cases that will be run by this test.
     */
    virtual Type::int32 countTestCases() = 0;

    /**
     * Runs a test and collects its result.
     */
    virtual void run(TestResult const& result) = 0;

};

#endif
