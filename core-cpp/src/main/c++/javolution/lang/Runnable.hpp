/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_RUNNABLE_HPP
#define _JAVOLUTION_LANG_RUNNABLE_HPP

#include "javolution/lang/Object.hpp"

namespace javolution {
    namespace lang {
        class Runnable_API;
        typedef Type::Handle<Runnable_API> Runnable;
    }
}

/**
 * This interface should be implemented by any class whose instances are
 * intended to be executed by a thread.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Runnable.html">
 *       Java - Runnable</a>
 * @version 1.0
 */
class javolution::lang::Runnable_API : public virtual javolution::lang::Object_API {
public:

    /**
     * When an object implementing interface Runnable is used to create a
     * thread, starting the thread causes the object's run method to be called
     *  in that separately executing thread.
     */
    virtual void run() = 0;
    
};

#endif
