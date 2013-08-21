/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_CONCURRENT_EXECUTORS_HPP
#define _JAVOLUTION_UTIL_CONCURRENT_EXECUTORS_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/util/concurrent/ExecutorService.hpp"

namespace javolution {
    namespace util {
        namespace concurrent {
            class Executors_API;
        }
    }
}

/**
 * This utility class provides factory methods to create <code>Executor</code>
 * instances.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/concurrent/Executors.html">
 *       Java - Executors</a>
 * @version 1.6
 */
class javolution::util::concurrent::Executors_API : public virtual javolution::lang::Object_API {

	/**
	 * Private constructor (utility class).
	 */
	Executors_API() {
    }

public:

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue.  At any point, at most
     * <code>nThreads</code> threads will be active processing tasks.
     * If additional tasks are submitted when all threads are active,
     * they will wait in the queue until a thread is available.
     * If any thread terminates due to a failure during execution
     * prior to shutdown, a new one will take its place if needed to
     * execute subsequent tasks.  The threads in the pool will exist
     * until it is explicitly {@link ExecutorService#shutdown shutdown}.
     *
     * @param nThreads the number of threads in the pool
     * @return the newly created thread pool
     * @throws IllegalArgumentException if <code>nThreads &lt;= 0</code>
     */
    JAVOLUTION_DLL static javolution::util::concurrent::ExecutorService newFixedThreadPool(Type::int32 nThreads);

    /**
     * Creates an Executor that uses a single worker thread operating
     * off an unbounded queue. (Note however that if this single
     * thread terminates due to a failure during execution prior to
     * shutdown, a new one will take its place if needed to execute
     * subsequent tasks.)  Tasks are guaranteed to execute
     * sequentially, and no more than one task will be active at any
     * given time. Unlike the otherwise equivalent
     * <code>newFixedThreadPool(1)</code> the returned executor is
     * guaranteed not to be reconfigurable to use additional threads.
     *
     * @return the newly created single-threaded Executor
     */
    JAVOLUTION_DLL static javolution::util::concurrent::ExecutorService newSingleThreadExecutor();

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to <code>execute</code> will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.
     *
     * @return the newly created thread pool
     */
    JAVOLUTION_DLL static javolution::util::concurrent::ExecutorService newCachedThreadPool();

};

#endif
