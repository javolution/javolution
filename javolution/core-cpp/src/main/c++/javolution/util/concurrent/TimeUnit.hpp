/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_CONCURRENT_TIME_UNIT_HPP
#define _JAVOLUTION_UTIL_CONCURRENT_TIME_UNIT_HPP

#include "javolution/lang/Enum.hpp"
#include "javolution/lang/Thread.hpp"

namespace javolution {
    namespace util {
        namespace concurrent {
            class TimeUnit_API;
            typedef javolution::lang::Enum<TimeUnit_API> TimeUnit;
        }
    }
}

/**
 * This enum represents represents time durations at a given unit of
 * granularity.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/concurrent/TimeUnit.html">
 *       Java - TimeUnit</a>
 * @version 1.6
 */
class javolution::util::concurrent::TimeUnit_API : public javolution::lang::Enum_API<javolution::util::concurrent::TimeUnit_API> {
// Class final, no virtual method.
public:
    static const TimeUnit NANOSECONDS;
    static const TimeUnit MICROSECONDS;
    static const TimeUnit MILLISECONDS;
    static const TimeUnit SECONDS;
    static const TimeUnit MINUTES;
    static const TimeUnit HOURS;
    static const TimeUnit DAYS;
    JAVOLUTION_DLL static Type::Array<TimeUnit> values();

    /**
     * Convert the given time duration in the given unit to this unit.
     *
     * @param sourceDuration the time duration in the given <code>sourceUnit</code>
     * @param sourceUnit the unit of the <code>sourceDuration</code> argument
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
    JAVOLUTION_DLL Type::int64 convert(Type::int64 sourceDuration, javolution::util::concurrent::TimeUnit const& sourceUnit) const;

    /**
     * Equivalent to <code>NANOSECONDS.convert(duration, this)</code>.
     *
     * @param duration the duration
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
     JAVOLUTION_DLL Type::int64 toNanos(Type::int64 duration) const;

    /**
     * Equivalent to <code>MICROSECONDS.convert(duration, this)</code>.
     *
     * @param duration the duration
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
     JAVOLUTION_DLL Type::int64 toMicros(Type::int64 duration) const;

    /**
     * Equivalent to <code>MILLISECONDS.convert(duration, this)</code>.
     *
     * @param duration the duration
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
     JAVOLUTION_DLL Type::int64 toMillis(Type::int64 duration) const;

    /**
     * Equivalent to <code>SECONDS.convert(duration, this)</code>.
     *
     * @param duration the duration
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
     JAVOLUTION_DLL Type::int64 toSeconds(Type::int64 duration) const;

    /**
     * Equivalent to <code>MINUTES.convert(duration, this)</code>.
     *
     * @param duration the duration
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
     JAVOLUTION_DLL Type::int64 toMinutes(Type::int64 duration) const;

    /**
     * Equivalent to <code>HOURS.convert(duration, this)</code>.
     *
     * @param duration the duration
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
     JAVOLUTION_DLL Type::int64 toHours(Type::int64 duration) const;

    /**
     * Equivalent to <code>DAYS.convert(duration, this)</code>.
     *
     * @param duration the duration
     * @return the converted duration, or <code>Long.MIN_VALUE</code> if conversion
     *         would negatively overflow, or <code>Long.MAX_VALUE</code> if it
     *         would positively overflow.
     */
     JAVOLUTION_DLL Type::int64 toDays(Type::int64 duration) const;

    /**
     * Performs a timed <code>Object.wait</code> using this time unit.
     *
     * @param obj the object to wait on
     * @param timeout the maximum time to wait. If less than
     * or equal to zero, do not wait at all.
     */
    JAVOLUTION_DLL void timedWait(javolution::lang::Object const& obj, Type::int64 timeout) const;

    /**
     * Performs a timed <code>Thread.join</code> using this time unit.
     * This is a convenience method that converts time arguments into the
     * form required by the <code>Thread.join</code> method.
     * @param thread the thread to wait for
     * @param timeout the maximum time to wait. If less than
     * or equal to zero, do not wait at all.
     */
    JAVOLUTION_DLL void timedJoin(javolution::lang::Thread const& thread, Type::int64 timeout) const;

    /**
     * Performs a <code>Thread.sleep</code> using this unit.
     * This is a convenience method that converts time arguments into the
     * form required by the <code>Thread.sleep</code> method.
     * @param timeout the minimum time to sleep. If less than
     * or equal to zero, do not sleep at all.
     * @throws InterruptedException if interrupted while sleeping.
     */
    JAVOLUTION_DLL void sleep(Type::int64 timeout) const;

};

#endif
