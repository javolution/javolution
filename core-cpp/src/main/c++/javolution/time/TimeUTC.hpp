/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_TIME_TIME_UTC_HPP
#define _JAVOLUTION_TIME_TIME_UTC_HPP

#include "javolution/lang/UnsupportedOperationException.hpp"
#include "javolution/lang/StringBuilder.hpp"
#include <time.h>


namespace javolution {
    namespace time {
        class TimeUTC_API;
        typedef Type::Handle<TimeUTC_API> TimeUTC;
    }
}

/**
 * This class represents a coordinated universal time (UTC).
 *
 * @version 1.0
 */
class javolution::time::TimeUTC_API : public virtual javolution::lang::Object_API {
    /**
     * Holds the duration in seconds since the epoch January 1, 1970, 00:00:00 GMT
     */
    Type::float64 _secondsSinceEpoch;

    /**
	 * Private constructor (class final).
     */
    TimeUTC_API(Type::float64 secondsSinceEpoch) : _secondsSinceEpoch(secondsSinceEpoch) {
    }

public:

    /**
     * This enumerate represents a month of year in the ISO-8601 calendar system.
     */
    enum MonthOfYear {
        JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
    };

    /**
     * Returns the current time (from OSGI time service).
     *
     * @param secondsSinceEpoch duration since epoch.
     */
    JAVOLUTION_DLL static TimeUTC current();

    /**
     * Returns the time after the specified duration in seconds since the epoch
     * January 1, 1970, 00:00:00 GMT.
     *
     * @param secondsSinceEpoch seconds since epoch.
     */
    static TimeUTC valueOf(Type::float64 secondsSinceEpoch) {
        return new TimeUTC_API(secondsSinceEpoch);
    }

    /**
     * Returns the duration in seconds since January 1st, 1970 UTC.
     *
     * @return the duration stated in seconds.
     */
    Type::float64 getSecondsSinceEpoch() const {
        return _secondsSinceEpoch;
    }

    // Overrides.
    JAVOLUTION_DLL javolution::lang::String toString() const;

    // Overrides
    Type::boolean equals(javolution::lang::Object obj) const {
        TimeUTC that = Type::dynamic_handle_cast<TimeUTC_API>(obj);
        if (that == Type::Null) return false;
        return equals(that);
    }
    Type::boolean equals(TimeUTC const& that) const {
        return _secondsSinceEpoch == that->_secondsSinceEpoch;
    }

    // Overrides
    Type::int32 hashCode() const {
        Type::int64 *float_as_int = (Type::int64*) &_secondsSinceEpoch;
         return (Type::int32) (*float_as_int ^ (*float_as_int >> 32));
    }

};

#endif
