/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JUNIT_FRAMEWORK_TEST_SUITE_HPP
#define _JUNIT_FRAMEWORK_TEST_SUITE_HPP

#include "junit/framework/TestResult.hpp"
#include "junit/framework/TestCase.hpp"
#include "junit/framework/Test.hpp"
#include "javolution/util/FastTable.hpp"

namespace junit {
    namespace framework {
        class TestSuite_API;
        typedef Type::Handle<TestSuite_API> TestSuite;
    }
}

/**
 * This class represents a composite of tests.
 * It runs a collection of test cases.
 *
 * @see  <a href="http://junit.org/apidocs/junit/framework/TestSuite.html">
 *       JUnit - TestSuite</a>
 * @version 1.0
 */
class junit::framework::TestSuite_API : public virtual junit::framework::Test_API {

    /**
     * Holds the name of the test suite.
     */
    javolution::lang::String _name;

    /**
     * Holds the tests.
     */
    javolution::util::FastTable<Test> _tests;

public:

    /**
     * Creates a test suite having the specified name.
     *
     * @param name the name of the test suite.
     */
    TestSuite_API(javolution::lang::String name) {
        _name = name;
        _tests = new javolution::util::FastTable_API<Test>();
    };

    /**
     * Returns the test suite having the specified name.
     *
     * @param name the name of the test suite.
     */
    static TestSuite newInstance(javolution::lang::String name) {
        return new TestSuite_API(name);
    }

    /**
     *  Returns the name of the test suite.
     */
    javolution::lang::String const& getName() {
        return _name;
    }

    /**
      * Adds a test to the suite.
      */
     virtual void addTest(TestCase test) {
         _tests->add(test);
     }

   // Implements test.
    virtual Type::int32 countTestCases() {
        int count= 0;
        for (int i=0; i < _tests->size(); i++) {
            count += _tests->get(i)->countTestCases();
        }
       return count;
    }

    // Implements test.
    virtual void run(TestResult result) {
       result->startTest(this);
       for (int i=0; i < _tests->size(); i++) {
            _tests->get(i)->run(result);
        }
       result->endTest(this);
    }
    
};

#endif
