/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_LANG_OBJECT_HPP
#define _JAVOLUTION_LANG_OBJECT_HPP

#include "JAVOLUTION.hpp"

// Declares the namespace here to avoid unnecessary indentation later.
namespace javolution {
    namespace lang {
        class Object_API;
        typedef Type::Handle<Object_API> Object;
        class Class_ANY_API; // Forward reference.
        typedef Type::Handle<Class_ANY_API> Class_ANY;
        class JAVOLUTION_String; // Forward reference.
        typedef JAVOLUTION_String String;
    }
}
// Methods required by Type::Handle.
namespace Type {
    void handle_add_ref(javolution::lang::Object_API* that);
    void handle_release(javolution::lang::Object_API* that);
}

/**
 * <p> This class represents the root of the class hierarchy for any Java-Like
 *     API. For such API, classes <b>and</b> interfaces should derive directly
 *     or indirectly from <code>Object_API</code>.</p>
 *
 * <p> Many classes from this JAVOLUTION have been translated from Open Source
 *     Java code (e.g. JAVOLUTION Java, Javolution, OSGi Java Spec, JUnit, etc.)
 *     Therefore for consistency and maintainability we follow the same
 *     <a href="http://geosoft.no/development/javastyle.html">Java Style</a>.
 *     Java objects are represented using Type::Handle<XXX_API> where XXX is
 *     the corresponding Java class. Garbage collection is done through
 *     reference counting (Type::Handle are intrusive pointers).
 *     The general pattern for class/interface definition is as follow (foo.hpp):
 *
 *     <pre><code>
 *
 *     #include "javolution/lang/Object.hpp"
 *     #include "com/bar/Bar.hpp"
 *     namespace com {                              // Namespace declaration to avoid indenting later.
 *         namespace bar {
 *            class Foo_API;
 *            typedef Type::Handle<Foo_API> Foo;     // Foo is the handle of Foo_API
 *        }
 *     }
 *     class com::bar::Foo_API : public virtual javolution::lang::Object_API {
 *         Bar _bar;                       // Private member.
 *     public:
 *         Foo_API(Bar bar) : _bar(bar) {} // Constructor (e.g. Foo foo = new Foo_API(bar);)
 *         virtual void g() { ... };       // Member method implemented in header.
 *         JAVOLUTION_DLL virtual void f();       // Member method implemented in body.
 *     }
 *
 *     </code></pre></p>
 *
 * <p> JAVOLUTION handles, like standard C++ pointers, are initialized to null.
 *     The constant <code>Type::Null</code> can be used for resetting or
 *     testing of handles. Accessing members from a null handle results
 *     in a <code>NullPointerException</code> being thrown.</p>
 *
 * <p> JAVOLUTION handles can be implicitly created from raw pointers.<pre><code>
 *
 *     <pre><code>
 *
 *     // Allows chaining, e.g. sb->append("Duration: ")->append(t)->append("ms")
 *     StringBuilder StringBuilder_API::append(...) {
 *         ...
 *         return this; // Implicit conversion from StringBuilder_API* to StringBuilder.
 *     }
 *
 *     </code></pre>
 *
 *     To facilitate object casting Type::static_handle_cast and
 *     Type::dynamic_handle_cast are provided.
 *
 *     <pre><code>
 *
 *     Type::boolean equals(Object obj) const {      // String_API::equals(Object)
 *        String that = Type::dynamic_handle_cast<String_API>(obj);
 *        if (that == Type::Null) return false;
 *        return equals(that);
 *     }
 *     Type::boolean equals(String const& that) const {
 *        return _wchars == that->_wchars;
 *     }
 *
 *     </code></pre>
 *
 *     Special precaution must be taken in case of cycles (object referencing
 *     themselves directly or indirectly). Self reference to oneself should
 *     be set directly (no reference count increment) to allow for object
 *     automatic deletion when there is no external reference to it.
 *
 *     <pre><code>
 *
 *     class ServiceTracker_API : public virtual ServiceTrackerCustomizer_API {
 *          ServiceTrackerCustomizer _customizer;
 *          ServiceTracker_API(ServiceTrackerCustomizer customizer) {'
 *              _customizer = customizer;
 *              if (customizer == Type::Null) {
 *                  _customizer.set(this);  // The set method does not increment the reference count.
 *              }
 *          }
 *     }
 *
 *     </code></pre>
 *
 *     </p>
 *
 * <p> Any object can be output directly (standard or wide stream).
 *
 *     <pre><code>
 *
 *     List<String> list = new FastTable_API<String>();
 *     list->add("first");
 *     list->add("second");
 *     list->add("third");
 *     list->add(Type::Null);
 *     std::cout << list << std::endl; // Displays list->toString() content.
 *
 *     >> [first, second, third, null]
 *
 *     </code</pre>>
 *     </p>
 *
 * <p> Concatenations between string literals and objects are supported,
 *     e.g. <code>String str = L"Hello " + user;</code>.</p>
 *
 * <p> Object handles should be stored and manipulated by value.
 *     'const&' are authorized for direct getter/setter and constructors.
 *     All other usage is dangerous and should be documented.<pre><code>
 *
 *         Temperature const& getTemperature() const { // Ok. Direct member access.
 *             return memberTemperature;
 *         }
 *
 *         void setTemperature(Temperature const& t) { // Ok. Direct member setting.
 *              memberTemperature = t; // Self assignment supported (e.g. setTemperature(getTemperature))
 *         }
 *
 *         Type::boolean equals(Object that) const; // Ok. This method accepts any handles referencing
 *                                                 // an Object_API subclass (implicit constructor)
 *
 *         Temperature const& getTemperature() const { // Wrong. Not a direct member access.
 *             return Temperature_API::valueOf(20, CELSIUS);  // Stack allocated instance.
 *         }
 *
 *         void setTemperature(Temperature const& t) { // Wrong. Not a direct member setting.
 *             memberTemperature = Type::Null;
 *             ...
 *             memberTemperature = t; // setTemperature(getTemperature()) would crash.
 *         }
 *
 *         Type::boolean equals(Object const& that) const; // Wrong. This method does not override Object_API::equals(Object)
 *                                                        // There is no class hierarchy between handles.
 *
 *     </code></pre>
 *
 *     JAVOLUTION handles have the same size as regular pointers, using handles by reference
 *     instead of by value is interesting only for very simple/short methods (e.g. getter/setter).
 * </p>
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Object.html">Java - Object</a>
 * @see  <a href="http://en.wikipedia.org/wiki/Comparison_of_Java_and_C%2B%2B">Comparison of Java and C++</a>
 * @version 1.0
 */
class javolution::lang::Object_API  {

	/**
	 * Holds reference count.
	 */
	Type::atomic_count _refCount;

	/**
	 * Holds memory cache for small objects.
	 */
	JAVOLUTION_DLL static JAVOLUTION::MemoryCache _memoryCache;

public:

    /**
     * Default constructor.
     */
    Object_API() : _refCount(0) {
    };

    /**
     * Returns the runtime class of this object (as a generic object type).
     * The default implementation returns a class having for name the 
     * RTTI (typeinfo) name.
     *
     * @return a class instance identifying this object.
     */
    JAVOLUTION_DLL virtual Class_ANY getClass() const;

    /**
     * Returns a string representation of the object.
     *
     * @return the textual representation of this object.
     */
    JAVOLUTION_DLL virtual String toString() const;

    /**
     * Indicates whether some other object is "equal to" this one.
     * The default implementation returns <code>true</code> only
     * if this object and that object are the same.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the argument;
     *         <code>false</code> otherwise.
     */
    virtual Type::boolean equals(Object obj) const {
    	return this == obj.get();
    }

    /**
     * Returns a well distributed hash code value for this object.
     *
     * @return this object hash code.
     */
    JAVOLUTION_DLL virtual Type::int32 hashCode() const;

    /**
     * Returns a reference to the mutex associated to this object (if any).
     * The default implementation raises a <code>javolution::lang::UnsupportedOperationException</code>.
     * This method should be overridden by classes supporting synchronization
     * (e.g. all collection classes).
     *
     * @return a unique mutex for this object.
     */
    JAVOLUTION_DLL virtual Type::Mutex& getMutex() const;

    /**
     * Ensures sub-classes destructors are called (C++).
     * Since members should never be references or pointers, this method
     * should rarely be overridden.
     */
    virtual ~Object_API() { 
    };

    /**
     * Returns the memory cache used by Object_API and its sub-classes.
     */
    static JAVOLUTION::MemoryCache& getMemoryCache() {
    	return _memoryCache;
    }

    /**
     * Custom allocator.
     */
    inline void* operator new (size_t size) {
     	return _memoryCache.allocate(size);
    }

    /**
     * Custom deallocator.
     */
    inline void operator delete (void* mem) {
     	_memoryCache.deallocate(mem);
    }


private:

    friend void Type::handle_add_ref(Object_API* that);
    friend void Type::handle_release(Object_API* that);
};

inline void Type::handle_add_ref(javolution::lang::Object_API* that) {
	 ++(that->_refCount);
}
inline void Type::handle_release(javolution::lang::Object_API* that) {
  	 if (--(that->_refCount) == 0) delete that;
}

// Stream operators.
JAVOLUTION_DLL std::ostream& operator<<(std::ostream& out, javolution::lang::Object_API const& that);
JAVOLUTION_DLL std::wostream& operator<<(std::wostream& wout, javolution::lang::Object_API const& that);

// Concatenation operators.
JAVOLUTION_DLL std::string operator+(std::string const& left, javolution::lang::Object_API const& right);
JAVOLUTION_DLL std::string operator+(javolution::lang::Object_API const& left, std::string const& right);
JAVOLUTION_DLL std::wstring operator+(std::wstring const& left, javolution::lang::Object_API const& right);
JAVOLUTION_DLL std::wstring operator+(javolution::lang::Object_API const& left, std::wstring const& right);


///////////////////
// Array Support //
///////////////////

namespace Type {
    template<class E> class Array_API;
}
template<class E> class Type::Array_API: public virtual javolution::lang::Object_API {
	friend class Type::Array<E>;
	E* value;
public:
	Array_API(Type::int32 length) : value(new E[length]) {}
	virtual ~Array_API() {
		delete[] value;
	}
};

template<class E> class Type::Array: public Type::Handle<Type::Array_API<E> > {
public:
	Type::int32 length;
	Array(Type::NullHandle = Type::Null) : Type::Handle<Array_API<E> >(), length(0) {} // Null
	Array(Type::int32 len) : Type::Handle<Array_API<E> >(new Array_API<E> (len)), length(len) {}
	E& operator[](Type::int32 index) const {
            if ((index < 0) || (index >= length))
			Type::NullHandle::throwArrayIndexOutOfBoundsException(index, length);
            if (this->get() == 0) Type::NullHandle::throwNullPointerException("Null Array.");
            return this->get()->value[index];
	}
};

#endif
