/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#include "javolution/time/TimeUTC.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include <stdio.h>

using namespace javolution::lang;
using namespace javolution::time;

#if defined (_WINDOWS)

#include <sys/timeb.h>
#include <time.h>

TimeUTC TimeUTC_API::current() {
	struct _timeb timebuffer;
	_ftime64_s( &timebuffer );
    Type::float64 secondsSinceEpoch = (Type::float64) timebuffer.time;
    secondsSinceEpoch += ((Type::float64) timebuffer.millitm) / 1000.0; // Add milliseconds.
    return new TimeUTC_API(secondsSinceEpoch);
}

String TimeUTC_API::toString() const {
	time_t seconds = (time_t) _secondsSinceEpoch;
	Type::int32 milliseconds = (Type::int32)((_secondsSinceEpoch - ((Type::int64)seconds)) * 1000);
	char timeline[26];
	ctime_s( timeline, 26, &seconds);
	timeline[19] = timeline[24] = 0;
	String year = String_API::valueOf(&timeline[20]);
	String ms = String_API::valueOf(milliseconds);
	while (ms->length() < 4) ms = "0" + ms;
	timeline[19] = 0;
	StringBuilder tmp = StringBuilder_API::newInstance();
	tmp->append(timeline)->append('.')->append(ms)->append(' ')->append(year);
	return tmp->toString();
}

#else

#include <sys/time.h>

TimeUTC TimeUTC_API::current() {
	struct timeval tv;
	gettimeofday(&tv, NULL);
	Type::float64 secondsSinceEpoch = ((Type::float64) tv.tv_sec) + ((Type::float64) tv.tv_usec) / 1000000.0;
    return new TimeUTC_API(secondsSinceEpoch);
}

String TimeUTC_API::toString() const {
	time_t seconds = (time_t) _secondsSinceEpoch;
	Type::int32 microseconds = (Type::int32)((_secondsSinceEpoch - ((Type::int64)seconds)) * 1000000);

    struct tm *nowtm;
    char tmbuf[64], buf[64];

    nowtm = localtime(&seconds);
    strftime(tmbuf, sizeof tmbuf, "%Y-%m-%d %H:%M:%S", nowtm);
    snprintf(buf, sizeof buf, "%s.%06d", tmbuf, microseconds);
    return String_API::valueOf(buf);
}

#endif






