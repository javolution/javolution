/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "iostream"
#include "javolution/log/Logging.hpp"
#include "javolution/time/TimeUTC.hpp"

using namespace javolution::lang;
using namespace javolution::log;
using namespace javolution::time;

Type::boolean isDebugEnabled = true;
Type::boolean isInfoEnabled = true;
Type::boolean isWarningEnabled = true;
Type::boolean isErrorEnabled = true;
Type::Mutex Logging_API::MUTEX;

void Logging_API::debug(const wchar_t* subject, const wchar_t* msg) {
    if (!isDebugEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - DEBUG [" << subject << L"] " << msg << std::endl;
}
void Logging_API::debug(const wchar_t* subject, const wchar_t* msg, Object const& obj) {
    if (!isDebugEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - DEBUG [" << subject << L"] " << msg << obj << std::endl;
}
void Logging_API::debug(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2) {
    if (!isDebugEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - DEBUG [" << subject << L"] " << msg1 << obj1 << msg2 << std::endl;
}
void Logging_API::debug(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2, Object const& obj2) {
    if (!isDebugEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - DEBUG [" << subject << L"] " << msg1 << obj1 << msg2 << obj2 << std::endl;
}
void Logging_API::info(const wchar_t* subject, const wchar_t* msg) {
    if (!isInfoEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - INFO [" << subject << L"] " << msg << std::endl;
}
void Logging_API::info(const wchar_t* subject, const wchar_t* msg, Object const& obj) {
    if (!isInfoEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - INFO [" << subject << L"] " << msg << obj << std::endl;
}
void Logging_API::info(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2) {
    if (!isInfoEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - INFO [" << subject << L"] " << msg1 << obj1 << msg2 << std::endl;
}
void Logging_API::info(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2, Object const& obj2) {
    if (!isInfoEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - INFO [" << subject << L"] " << msg1 << obj1 << msg2 << obj2 << std::endl;
}
void Logging_API::warning(const wchar_t* subject, const wchar_t* msg) {
    if (!isWarningEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - WARNING [" << subject << L"] " << msg << std::endl;
}
void Logging_API::warning(const wchar_t* subject, const wchar_t* msg, Object const& obj) {
    if (!isWarningEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - WARNING [" << subject << L"] " << msg << obj << std::endl;
}
void Logging_API::warning(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2) {
    if (!isWarningEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - WARNING [" << subject << L"] " << msg1 << obj1 << msg2 << std::endl;
}
void Logging_API::warning(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2, Object const& obj2) {
    if (!isWarningEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - WARNING [" << subject << L"] " << msg1 << obj1 << msg2 << obj2 << std::endl;
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg << std::endl;
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg, Object const& obj) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg << obj << std::endl;
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg1 << obj1 << msg2 << std::endl;
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2, Object const& obj2) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg1 << obj1 << msg2 << obj2 << std::endl;
}
void Logging_API::error(const wchar_t* subject, Throwable const& thrown) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << thrown << std::endl;
    thrown->printStackTrace();
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg, Throwable const& thrown) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg << thrown << std::endl;
    thrown->printStackTrace();
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg, Object const& obj, Throwable const& thrown) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg << obj << L" - " << thrown << std::endl;
    thrown->printStackTrace();
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2, Throwable const& thrown) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg1 << obj1 << msg2 << thrown << std::endl;
    thrown->printStackTrace();
}
void Logging_API::error(const wchar_t* subject, const wchar_t* msg1, Object const& obj1, const wchar_t* msg2, Object const& obj2, Throwable const& thrown) {
    if (!isErrorEnabled) return;
    Type::ScopedLock lock(MUTEX);
    std::wcout << TimeUTC_API::current() << L" - ERROR [" << subject << L"] " << msg1 << obj1 << msg2 << obj2 << L" - " << thrown << std::endl;
    thrown->printStackTrace();
}
void Logging_API::setLogErrorEnabled(const wchar_t*, Type::boolean value) {
    isErrorEnabled = value;
}
Type::boolean Logging_API::isLogErrorEnabled(const wchar_t*) {
    return isErrorEnabled;
}
void Logging_API::setLogWarningEnabled(const wchar_t*, Type::boolean value) {
    isWarningEnabled = value;
}
Type::boolean Logging_API::isLogWarningEnabled(const wchar_t*) {
    return isWarningEnabled;
}
void Logging_API::setLogInfoEnabled(const wchar_t*, Type::boolean value) {
    isInfoEnabled = value;
}
Type::boolean Logging_API::isLogInfoEnabled(const wchar_t*) {
    return isInfoEnabled;
}
void Logging_API::setLogDebugEnabled(const wchar_t*, Type::boolean value) {
    isDebugEnabled = value;
}
Type::boolean Logging_API::isLogDebugEnabled(const wchar_t*) {
    return isDebugEnabled;
}
