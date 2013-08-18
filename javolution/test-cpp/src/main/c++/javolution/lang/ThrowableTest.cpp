/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/lang/ThrowableTest.hpp"

using namespace javolution::lang;

void ThrowableTest_API::testPrintStack() {
    Throwable error = new Throwable_API(L"Throwable Test");
}
