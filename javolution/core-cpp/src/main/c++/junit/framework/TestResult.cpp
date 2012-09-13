/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "junit/framework/TestResult.hpp"
#include "javolution/lang/Error.hpp"
#include "junit/framework/TestCase.hpp"
#include "junit/framework/TestSuite.hpp"
#include "javolution/log/Logging.hpp"
#include "javolution/lang/Integer32.hpp"

using namespace javolution::lang;
using namespace junit::framework;
using namespace javolution::log;
using namespace javolution::lang;

void TestResult_API::startTest(Test test) {

    TestSuite_API* testSuite = dynamic_cast<TestSuite_API*>(test.get());
    if (testSuite != 0) {
        Logging_API::info(L"TEST", L"TestSuite: ", testSuite->getName());
        _tusarFile << "   <testsuite name='" << testSuite->getName()->toUTF8()
                << "' tests='" << testSuite->countTestCases() << "' >" << std::endl;
        return;
    }

    TestCase_API* testCase = dynamic_cast<TestCase_API*>(test.get());
    if (testCase != 0) {
        Logging_API::info(L"TEST", L"    TestCase: ", testCase->getName());
        _tusarFile << "      <testcase testname='" << testCase->getName()->toUTF8() << "' >" << std::endl;
        return;
    }
    throw Error_API::newInstance(L"Unknown Test Type");
}

void TestResult_API::endTest(Test test) {

    TestSuite_API* testSuite = dynamic_cast<TestSuite_API*>(test.get());
    if (testSuite != 0) {
        _tusarFile << "   </testsuite>" << std::endl;
        return;
    }

    TestCase_API* testCase = dynamic_cast<TestCase_API*>(test.get());
    if (testCase != 0) {
        _tusarFile << "      </testcase>" << std::endl;
        return;
    }

    throw Error_API::newInstance(L"Unknown Test Type");
}
void TestResult_API::addError(Test test, Throwable error) {
    Logging_API::error(L"TEST", L"ERROR - ", error);
    _tusarFile << "         <error message='" << error->toString()->toUTF8()
            << "' type='Error'>" << error->toString()->toUTF8()
            << "</error>" << std::endl;
}

void TestResult_API::addFailure(Test test, AssertionFailedError failure) {
    Logging_API::error(L"TEST", L"FAIL - ", failure->toString());
    _tusarFile << "         <failure message='" << failure->toString()->toUTF8()
            << "' type='Assertion'>" << failure->toString()->toUTF8()
            << "</failure>" << std::endl;
}

Type::int32 TestResult_API::printSummary() {
    Logging_API::info(L"TEST", L"Number of test cases run: ", Integer32_API::valueOf(_runCount));
    Logging_API::info(L"TEST", L"Error(s): ", Integer32_API::valueOf(_errorCount));
    Logging_API::info(L"TEST", L"Failures): ", Integer32_API::valueOf(_failureCount));
    _tusarFile << "</tests>" << std::endl;
    _tusarFile << "</tusar>" << std::endl;
    // _tusarFile should be automatically closed when TestResult is deleted.
    return _errorCount + _failureCount;
}
void TestResult_API::run(TestCase testCase) {
    startTest(testCase);
    try {
        // Avoid polluting output when exceptions are being tested.
        testCase->runBare();
    } catch (AssertionFailedError& failure) {
        _failureCount++;
        addFailure(testCase, failure);
    } catch (Throwable& error) {
        _errorCount++;
        addError(testCase, error);
    } catch (const std::exception& ex) {
        _errorCount++;
        addError(testCase, Error_API::newInstance(L"C++ Error: " + String_API::valueOf(ex.what())));
    } catch (...) {
        _errorCount++;
        addError(testCase, Error_API::newInstance(L"Unknown Error"));
    }
    _runCount++;
    endTest(testCase);
}
