/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/util/concurrent/Executors.hpp"
#include "javolution/lang/UnsupportedOperationException.hpp"

using namespace javolution::lang;
using namespace javolution::util::concurrent;

// TODO: Implements methods below.
ExecutorService Executors_API::newFixedThreadPool(Type::int32) {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
ExecutorService Executors_API::newSingleThreadExecutor() {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
ExecutorService Executors_API::newCachedThreadPool() {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
