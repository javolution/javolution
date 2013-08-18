/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_CLASS_HPP
#define _JAVOLUTION_LANG_CLASS_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/lang/String.hpp"

namespace javolution {
    namespace lang {
        class Class_ANY_API; // Base class.
        typedef Type::Handle<Class_ANY_API> Class_ANY;
        template<class T> class Class_API;
        template<class T> class Class;
    }
}

/**
 * This class represents unique class instances. They can be used to perform
 * static synchronized operations (synchronization on the whole class)
 * For example:[code]
 *     static const Class<Foo_API> CLASS; // Class_API<Foo_API>::forName(L"fooPkg::Foo_API") in body.
 *     static Foo getInstance(String id) {
 *         synchronized (CLASS) { // Makes it equivalent to "static synchronized Foo getInstance(String id) { ... }"
 *             ...
 *         }
 *     }
 * [/code]
 *
 * This class maintains unicity (operator == can be used in place of Object.equals(obj)).
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Class.html">
 *       Java - Class</a>
 * @version 1.1
 */
class javolution::lang::Class_ANY_API : public virtual javolution::lang::Object_API {

     /**
	 * Holds the name of this class (static since the class itself is static).
	 */
	String _name;

	/**
	 * Holds the mutex associated.
	 */
	Type::Mutex _mutex;

	/**
	 * Private constructor (factory methods should be used).
	 */
	Class_ANY_API(String const& name) : _name(name) {
	}

 public:

    /**
     * Returns the unique class instance for the specified name.
     */
    JAVOLUTION_DLL static Class_ANY forName(String const& name);

    /**
     * Returns the Java class name of this instance (for example
     * java.lang.Boolean).
     */
    String getName() const {
        return _name;
    }

    /**
     * Returns the textual representation of this class.
     */
    String toString() const {
        return getName();
    }

    /**
     * Returns the mutex for this class object.
     */
    Type::Mutex& getMutex() const {
        return const_cast<Class_ANY_API*>(this)->_mutex;
    }

    ///////////////////////////////////////////////////////////////////////////
    // No need to override Object.equals and Object.hashCode due to unicity. //
    ///////////////////////////////////////////////////////////////////////////

 };

/////////////////////////
// Parameterized types //
/////////////////////////

template <class T> class javolution::lang::Class_API : public Class_ANY_API  {
    Class_API() {} // Private constructor (there is no instance of this class).
public:

    /**
     * Returns the unique class instance having the specified name
     * (e.g. <code>javolution::lang::Boolean_API</code>).
     *
     * @param name the name of the class instance.
     */
    static Class<T> forName(String const& name) {
        Class_ANY classAny = Class_ANY_API::forName(name);
        return Class<T>(classAny);
    }
};

template<class T>
class javolution::lang::Class : public javolution::lang::Class_ANY  {
public:
    Class(Type::NullHandle = Type::Null) : Class_ANY() {} // Null.
    explicit Class(Class_ANY const& source) : Class_ANY(source) {}
};

#endif
