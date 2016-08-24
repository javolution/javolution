/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "junit/framework/TestResult.hpp"
#include "javolution/lang/StringTest.hpp"
#include "javolution/lang/EnumTest.hpp"
#include "javolution/lang/ThrowableTest.hpp"
#include "javolution/util/FastTableTest.hpp"
#include "javolution/util/FastMapTest.hpp"
#include "javolution/util/FastSetTest.hpp"
#include "javolution/lang/Integer32Test.hpp"
#include "javolution/osgi/OSGiTest.hpp"

using namespace javolution::lang;
using namespace junit::framework;
using namespace javolution::lang;

/* Unit test main routine */
int main(int, char**) {
    TestResult result = new TestResult_API();
    javolution::lang::StringTest_API::suite()->run(result);
    javolution::lang::EnumTest_API::suite()->run(result);
    javolution::util::FastTableTest_API::suite()->run(result);
    javolution::util::FastMapTest_API::suite()->run(result);
    javolution::util::FastSetTest_API::suite()->run(result);
    javolution::lang::Integer32Test_API::suite()->run(result);
    javolution::osgi::OSGiTest_API::suite()->run(result);
    javolution::lang::ThrowableTest_API::suite()->run(result);
    return result->printSummary();
}
