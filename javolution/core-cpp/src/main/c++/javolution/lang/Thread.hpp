/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_THREAD_HPP
#define _JAVOLUTION_LANG_THREAD_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/Runnable.hpp"

namespace javolution {
    namespace lang {
        class Thread_API;
        typedef Type::Handle<Thread_API> Thread;
    }
}

/**
 * This class represents an execution thread.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Thread.html">
 *       Java - Thread</a>
 * @version 1.0
 */
class javolution::lang::Thread_API : public virtual javolution::lang::Runnable_API {

    /**
     * Holds the runnable if any.
     */
    Runnable _target;

    /**
     * Holds the thread name.
     */
    String _name;

    /**
     * Holds the pointer to the native thread object.
     */
    void* _nativeThreadPtr;

public:

    /**
     * Creates a thread having the specified target and name.
     *
     * @param target the runnable to be executed by this thread.
     * @param name the name of the thread.
     */
    JAVOLUTION_DLL Thread_API(Runnable target, String name);

    /**
     * Returns a new thread instance executing the specified runnable and 
     * having the specified name.
     *
     * @param target the runnable to be executed by this thread.
     * @param name the name of the thread.
     */
    static Thread newInstance(Runnable target, String name) {
        return new Thread_API(target, name);
    }

    /**
     * Returns this thread's name.
     */
    javolution::lang::String const& getName() const {
    	 return _name;
    }

    /**
     * Causes this thread to begin execution of the <code>run</code> method of
     * this thread. It is never legal to start a thread more than once.
     * In particular, a thread may not be restarted once it has completed
     * execution.
     */
    JAVOLUTION_DLL virtual void start();

    /**
     * Waits for this thread to die.
     */
    JAVOLUTION_DLL virtual void join();

    /**
     * If this thread was constructed using a separate Runnable run object,
     * then that Runnable object's run method is called; otherwise, this
     * method does nothing and returns.
     */
    JAVOLUTION_DLL virtual void run();

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution) for the specified number of milliseconds, subject to
     * the precision and accuracy of system timers and schedulers. The thread
     * does not lose ownership of any monitors.
     *
     * @param millis the length of time to sleep in milliseconds.
     */
    JAVOLUTION_DLL static void sleep(Type::int64 millis);

    /**
     * Default destructor.
     */
    JAVOLUTION_DLL ~Thread_API();

};

#endif
