/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/util/concurrent/TimeUnit.hpp"
#include "javolution/lang/UnsupportedOperationException.hpp"

using namespace javolution::lang;
using namespace javolution::util::concurrent;

const Class<TimeUnit_API> TIME_UNIT_CLASS = Class_API<TimeUnit_API>::forName(L"javolution::util::concurrent::TimeUnit_API");
const TimeUnit TimeUnit_API::NANOSECONDS = Enum_API<TimeUnit_API>::newInstance(TIME_UNIT_CLASS, L"nanoseconds");
const TimeUnit TimeUnit_API::MICROSECONDS = Enum_API<TimeUnit_API>::newInstance(TIME_UNIT_CLASS, L"microseconds");
const TimeUnit TimeUnit_API::MILLISECONDS = Enum_API<TimeUnit_API>::newInstance(TIME_UNIT_CLASS, L"milliseconds");
const TimeUnit TimeUnit_API::SECONDS = Enum_API<TimeUnit_API>::newInstance(TIME_UNIT_CLASS, L"seconds");
const TimeUnit TimeUnit_API::MINUTES = Enum_API<TimeUnit_API>::newInstance(TIME_UNIT_CLASS, L"minutes");
const TimeUnit TimeUnit_API::HOURS = Enum_API<TimeUnit_API>::newInstance(TIME_UNIT_CLASS, L"hours");
const TimeUnit TimeUnit_API::DAYS = Enum_API<TimeUnit_API>::newInstance(TIME_UNIT_CLASS, L"days");
Type::Array<TimeUnit> TIME_UNIT_VALUES = Enum_API<TimeUnit_API>::values(TIME_UNIT_CLASS); // Must be last.
Type::Array<TimeUnit> TimeUnit_API::values() {
    return TIME_UNIT_VALUES;
}
// TODO: Implements methods below.
Type::int64 TimeUnit_API::convert(Type::int64 , javolution::util::concurrent::TimeUnit const&) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
Type::int64 TimeUnit_API::toNanos(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
Type::int64 TimeUnit_API::toMicros(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
Type::int64 TimeUnit_API::toMillis(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
Type::int64 TimeUnit_API::toSeconds(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
Type::int64 TimeUnit_API::toMinutes(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
Type::int64 TimeUnit_API::toHours(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
Type::int64 TimeUnit_API::toDays(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
void TimeUnit_API::timedWait(javolution::lang::Object, Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
void TimeUnit_API::timedJoin(javolution::lang::Thread, Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
void TimeUnit_API::sleep(Type::int64) const {
    throw UnsupportedOperationException_API::newInstance(L"Not implemented yet");
}
