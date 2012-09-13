/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "JAVOLUTION.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include "javolution/lang/NullPointerException.hpp"
#include "javolution/lang/ArrayIndexOutOfBoundsException.hpp"
#include "javolution/lang/RuntimeException.hpp"
#include "javolution/lang/Integer32.hpp"
#include "javolution/log/Logging.hpp"

using namespace javolution::lang;
using namespace javolution::lang;
using namespace javolution::log;

// The generic null handle instance.
Type::NullHandle Type::Null = Type::NullHandle();

// Exception mapped to javolution::lang::NullPointerException (default implementation).
void Type::NullHandle::throwNullPointerException(const char* msg) {
    throw NullPointerException_API::newInstance(msg);
}

// Exceptions is mapped to javolution::lang::ArrayIndexOutOfBoundsException (default implementation).
void Type::NullHandle::throwArrayIndexOutOfBoundsException(Type::int32 index, Type::int32 length) {
    StringBuilder tmp = new StringBuilder_API();
    tmp->append(L"Index: ")->append(index)->append(" for array of Length: ")->append(length);
    throw ArrayIndexOutOfBoundsException_API::newInstance(tmp->toString());
 }

void JAVOLUTION::MemoryCache::enable(Type::boolean isEnabled) {
	if ((isEnabled) && (_cacheMin == 0)) { // First time, allocate the cache.
		 Block* cache = new Block[SIZE];
		 _queue = new void*[SIZE];
		 for (int i=0; i < SIZE; i++) {
			 _queue[i] = &cache[i];
		 }
	     _cacheMin = &cache[0];
	     _cacheMax = &cache[SIZE-1];
    }
	if (isEnabled) {
		Logging_API::info(L"JAVOLUTION", L"Enable Memory Cache, free: ",
				(Integer32)freeCount(), L", used: ", (Integer32)useCount());
	} else {
		Logging_API::info(L"JAVOLUTION", L"Disable Memory Cache, free: ",
				(Integer32)freeCount(), L", used: ", (Integer32)useCount());
		if (_debugMaxUsage != 0) {
			Logging_API::info(L"JAVOLUTION", L"Memory Cache number of allocations: ", (Integer32)_newCount);
			Logging_API::info(L"JAVOLUTION", L"Memory Cache number of deallocations: ", (Integer32)_deleteCount);
			Logging_API::info(L"JAVOLUTION", L"Memory Cache peak use: ", (Integer32)_debugMaxUsage);
		}
	}
    _isEnabled = isEnabled;
}
#ifdef _SOLARIS // Add missing define in Solaris 10 (pthreads.h)
#define PTHREAD_MUTEX_RECURSIVE_NP PTHREAD_MUTEX_RECURSIVE
#endif

#ifndef _WINDOWS
Type::Mutex::Mutex() {
	Type::int32 rc = pthread_mutexattr_init(&attr);
	if (rc != 0) throw RuntimeException_API::newInstance(L"pthread_mutexattr_init returns " + rc);
    rc = pthread_mutexattr_settype (&attr, PTHREAD_MUTEX_RECURSIVE_NP);
	if (rc != 0) throw RuntimeException_API::newInstance(L"pthread_mutexattr_settype returns " + rc);
    rc = pthread_mutex_init (&mutex, &attr);
	if (rc != 0) throw RuntimeException_API::newInstance(L"pthread_mutex_init returns " + rc);
    rc = pthread_mutexattr_destroy(&attr);
	if (rc != 0) throw RuntimeException_API::newInstance(L"pthread_mutexattr_destroy returns " + rc);
}

Type::Mutex::~Mutex() {
	Type::int32 rc = pthread_mutex_destroy(&mutex);
	if (rc != 0) throw RuntimeException_API::newInstance(L"pthread_mutex_destroy returns " + rc);
}

Type::ScopedLock::ScopedLock(Mutex& m) : mutex(m.mutex){
	Type::int32 rc = pthread_mutex_lock(&mutex);
	if (rc != 0) throw RuntimeException_API::newInstance(L"pthread_mutex_lock returns " + rc);
    isLocked = true;
}

Type::ScopedLock::~ScopedLock() {
	Type::int32 rc = pthread_mutex_unlock(&mutex);
	if (rc != 0) throw RuntimeException_API::newInstance(L"pthread_mutex_unlock returns " + rc);
}
#endif
