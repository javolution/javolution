/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_CONCURRENT_EXECUTOR_SERVICE_HPP
#define _JAVOLUTION_UTIL_CONCURRENT_EXECUTOR_SERVICE_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/util/List.hpp"
#include "javolution/util/concurrent/Executor.hpp"
#include "javolution/util/concurrent/TimeUnit.hpp"

namespace javolution {
    namespace util {
        namespace concurrent {
            class ExecutorService_API;
            typedef Type::Handle<ExecutorService_API> ExecutorService;
        }
    }
}

/**
 * This class represents an object that executes submitted <code>Runnable</code>
 * tasks.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/concurrent/ExecutorService.html">
 *       Java - ExecutorService</a>
 * @version 1.6
 */
class javolution::util::concurrent::ExecutorService_API : public virtual javolution::util::concurrent::Executor_API {
public:

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     *
     * @throws SecurityException if a security manager exists and
     *         shutting down this ExecutorService may manipulate
     *         threads that the caller is not permitted to modify
     *         because it does not hold {@link
     *         java.lang.RuntimePermission}<code>("modifyThread")</code>,
     *         or the security manager's <code>checkAccess</code> method
     *         denies access.
     */
    virtual void shutdown() = 0;

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks that were
     * awaiting execution.
     *
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  For example, typical
     * implementations will cancel via {@link Thread#interrupt}, so any
     * task that fails to respond to interrupts may never terminate.
     *
     * @return list of tasks that never commenced execution
     */
    virtual javolution::util::List<javolution::lang::Runnable> shutdownNow() = 0;

    /**
     * Returns <code>true</code> if this executor has been shut down.
     *
     * @return <code>true</code> if this executor has been shut down
     */
    virtual Type::boolean isShutdown() const = 0;

    /**
     * Returns <code>true</code> if all tasks have completed following shut down.
     * Note that <code>isTerminated</code> is never <code>true</code> unless
     * either <code>shutdown</code> or <code>shutdownNow</code> was called first.
     *
     * @return <code>true</code> if all tasks have completed following shut down
     */
    virtual Type::boolean isTerminated() const = 0;

    /**
     * Blocks until all tasks have completed execution after a shutdown
     * request, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return <code>true</code> if this executor terminated and
     *         <code>false</code> if the timeout elapsed before termination
     */
    virtual Type::boolean awaitTermination(Type::int64 timeout, javolution::util::concurrent::TimeUnit const& unit) const = 0;

};

#endif
