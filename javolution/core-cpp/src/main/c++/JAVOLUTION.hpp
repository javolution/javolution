/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_HPP
#define	_JAVOLUTION_HPP

#include <ostream>
#include <string>
#include <iostream> // Not required but makes std::wcout, std::wcerr and std::wclog directly visible.
#include "boost/detail/atomic_count.hpp"
#include "boost/detail/lightweight_mutex.hpp"
#include "boost/cstdint.hpp"

#if defined(_WIN32) || defined(_WIN64) || defined __CYGWIN__
#define _WINDOWS
#define _DEPRECATED(text) __declspec(deprecated(#text))
#pragma warning(disable: 4290) //  Visual C++ does not implement checked exceptions (throw declaration ignored).
#endif

#if defined(__sun)
#define _SOLARIS
#define _DEPRECATED(text)
#endif

#if defined(__linux)
#define _LINUX
#define _DEPRECATED(text) __attribute__ ((deprecated(#text)))
#endif

// Generic helper definitions for shared library support, ref. http://gcc.gnu.org/wiki/Visibility
// For gcc compilations the option  -fvisibility=hidden and -fvisibility-inlines-hidden
// should be used to export only symbols explicitly marked as *_DLL
#if defined _WINDOWS
#define HELPER_DLL_IMPORT __declspec(dllimport)
#define HELPER_DLL_EXPORT __declspec(dllexport)
#else
#if __GNUC__ >= 4
#define HELPER_DLL_IMPORT __attribute__ ((visibility("default")))
#define HELPER_DLL_EXPORT __attribute__ ((visibility("default")))
#else
#define HELPER_DLL_IMPORT
#define HELPER_DLL_EXPORT
#endif
#endif

// Now we use the generic helper definitions above to define JAVOLUTION_DLL (components using the JAVOLUTION can do the same).
#ifndef JAVOLUTION_DLL // Only if there is no user override (e.g. static build).
#ifdef JAVOLUTION_DLL_EXPORT // The JAVOLUTION DLL is being built.
#define JAVOLUTION_DLL HELPER_DLL_EXPORT
#else // The JAVOLUTION DLL is being used.
#define JAVOLUTION_DLL HELPER_DLL_IMPORT
#endif
#endif


// Define a very useful macro holding the current line location.
// This macro can be used for logging, exception raising or assertion.
#define _STR(x) # x
#define JAVOLUTION_STR(x) _STR(x)
#define _WIDEN(x) L ## x
#define JAVOLUTION_WIDEN(x) _WIDEN(x)
#define JAVOLUTION_LINE_INFO L"Line " JAVOLUTION_WIDEN(JAVOLUTION_STR(__LINE__)) L" in file " JAVOLUTION_WIDEN(__FILE__)

namespace Type {

    // Deiines portable primitive types.
    typedef wchar_t wchar;
    typedef bool boolean;
    typedef boost::int8_t int8;
    typedef boost::int16_t int16;
    typedef boost::int32_t int32;
    typedef boost::int64_t int64;
    typedef float float32;
    typedef double float64;

#ifdef NOTHREAD
    typedef long atomic_count;
#else
    typedef boost::detail::atomic_count atomic_count;
#endif

    // Defines handles types.

    class NullHandle {
    public:
        JAVOLUTION_DLL static void throwNullPointerException(const char* msg);
        JAVOLUTION_DLL static void throwArrayIndexOutOfBoundsException(Type::int32 index, Type::int32 length);
    };
    template<class T> class Handle;
    template<class E> class Array;

    JAVOLUTION_DLL extern NullHandle Null;
}


// Defines basic handle (intrusive pointer).
//

template<class T> class Type::Handle {
private:
    typedef Handle this_type;
    T* ptr;
public:
    typedef T element_type;

    Handle(Type::NullHandle = Type::Null) : ptr(0) {
    }

    Handle(Handle const& that) : ptr(that.ptr) {
        if (ptr != 0) handle_add_ref(ptr);
    }

    Handle(T* that) : ptr(that) {
        if (ptr != 0) handle_add_ref(ptr);
    }

    template<class U> Handle(Handle<U> const& that) : ptr(that.get()) {
        if (ptr != 0) handle_add_ref(ptr);
    }

    Handle& operator=(Handle const& that) {
        this_type(that).swap(*this);
        return *this;
    }

    Handle& operator=(T* that) {
        this_type(that).swap(*this);
        return *this;
    }

    Handle& operator=(NullHandle const&) {
        if (ptr != 0) handle_release(ptr);
        ptr = 0;
        return *this;
    }

    template<class U> Handle& operator=(Handle<U> const& that) {
        this_type(that).swap(*this);
        return *this;
    }

    ~Handle() {
        if (ptr != 0) handle_release(ptr);
    }

    T* get() const {
        return ptr;
    }

    void set(T* that) { // Sets the pointer without incrementing 'that' or decrementing the old pointer.
        ptr = that; // This method is typically used to prevent cycles (e.g. inner class referencing its parent).
    }

    T& operator*() const {
        return *ptr;
    }

    T* operator->() const {
        if (ptr == 0) Type::NullHandle::throwNullPointerException("Null Handle.");
        return ptr;
    }
private:

    void swap(Handle& that) {
        T* tmp = ptr;
        ptr = that.ptr;
        that.ptr = tmp;
    }
};

template<class T, class U> inline bool operator==(Type::Handle<T> const& a, Type::Handle<U> const & b) {
    return a.get() == b.get();
}

template<class T, class U> inline bool operator!=(Type::Handle<T> const& a, Type::Handle<U> const & b) {
    return a.get() != b.get();
}

template<class T> inline bool operator==(Type::Handle<T> const& a, Type::NullHandle const&) {
    return a.get() == 0;
}

template<class T> inline bool operator!=(Type::Handle<T> const& a, Type::NullHandle const&) {
    return a.get() != 0;
}

template<class T> inline bool operator==(Type::NullHandle const&, Type::Handle<T> const& b) {
    return b.get() == 0;
}

template<class T> inline bool operator!=(Type::NullHandle const&, Type::Handle<T> const& b) {
    return b.get() != 0;
}

template<class T> std::ostream& operator<<(std::ostream& os, Type::Handle<T> const& that) {
    return (that.get() != 0) ? os << *(that.get()) : os << "null";
}

template<class T> std::wostream& operator<<(std::wostream& wos, Type::Handle<T> const& that) {
    return (that.get() != 0) ? wos << *(that.get()) : wos << "null";
}

template<class T> std::string operator+(std::string const& left, Type::Handle<T> const& right) {
    return (right.get() != 0) ? left + *(right.get()) : left + "null";
}

template<class T> std::string operator+(Type::Handle<T> const& left, std::string const& right) {
    return (left.get() != 0) ? *(left.get()) + right : "null" + right;
}

template<class T> std::wstring operator+(std::wstring const& left, Type::Handle<T> const& right) {
    return (right.get() != 0) ? left + *(right.get()) : left + L"null";
}

template<class T> std::wstring operator+(Type::Handle<T> const& left, std::wstring const& right) {
    return (left.get() != 0) ? *(left.get()) + right : L"null" + right;
}

// Defines static/dynamic cast template.
namespace Type {

    // Static cast, e.g. String str = Type::static_handle_cast<String_API>(myObject);

    template<class T, class U> T* static_handle_cast(Handle<U> const& that) {
        return static_cast<T*> (that.get());
    }

    // Dynamic cast, e.g. String str = Type::dynamic_handle_cast<String_API>(myObject)

    template<class T, class U> T* dynamic_handle_cast(Handle<U> const& that) {
        return dynamic_cast<T*> (that.get());
    }
}

// Define the synchronized keyword implemented using boost mutexes.
// See http://www.codeproject.com/KB/threads/cppsyncstm.aspx
// The synchronized parameter should be a Javolution handle having the getMutex()
// method implemented (e.g. collection classes).
// The synchronized macro is exception-safe, since it unlocks its mutex upon destruction.
namespace Type {

#ifdef _WINDOWS // Use boost lightweight mutex implementation.
    typedef boost::detail::lightweight_mutex Mutex;

    class ScopedLock : public boost::detail::lightweight_mutex::scoped_lock {
    public:

        explicit ScopedLock(Mutex& m) : boost::detail::lightweight_mutex::scoped_lock(m) {
            isLocked = true;
        }
        bool isLocked;
    };
#else  // We cannot use boost lightweight mutex because non-recursive.

    class ScopedLock;

    class Mutex {
        friend class ScopedLock;
    public:
        JAVOLUTION_DLL Mutex();
        JAVOLUTION_DLL ~Mutex();
    private:
        pthread_mutex_t mutex;
        pthread_mutexattr_t attr;
    };

    class ScopedLock {
    public:
        JAVOLUTION_DLL explicit ScopedLock(Mutex& m);
        JAVOLUTION_DLL ~ScopedLock();
        bool isLocked;
    private:
        pthread_mutex_t& mutex;
    };
#endif
}
#define synchronized(M)  for(Type::ScopedLock _lock(M->getMutex()); _lock.isLocked; _lock.isLocked=false)

// Simple lock free memory cache.
//
namespace JAVOLUTION {

    class MemoryCache {

        struct Block { // Define memory block of 32 bytes.
            Type::int64 data0;
            Type::int64 data1;
            Type::int64 data2;
            Type::int64 data3;
        };
        static const Type::int32 LOG2_SIZE = 20; // 1024 * 1024 (32 MBytes).
        static const Type::int32 SIZE = 1 << LOG2_SIZE;
        static const Type::int32 MASK = SIZE - 1;
        static const size_t BLOCK_SIZE = sizeof (Block);
        static const Type::int32 MAX_CPU = 32;
        void** _queue; // Circular queue.
        void* _cacheMin;
        void* _cacheMax;
        Type::atomic_count _newCount; // Number of blocks allocated minus one.
        Type::atomic_count _deleteCount; // Number of blocks deallocated minus one.
        Type::int32 _debugMaxUsage;
        Type::boolean _isEnabled;
    public:

        MemoryCache() : _newCount(-1), _deleteCount(-1), _debugMaxUsage(0), _isEnabled(false) {
        }

        inline Type::int32 freeCount() {
            return SIZE - useCount();
        }

        inline Type::int32 useCount() {
            return _newCount - _deleteCount;
        }
        JAVOLUTION_DLL void enable(Type::boolean isEnabled); // Enable/disable the memory cache.

        inline void* allocate(size_t size) {
            if (!_isEnabled || (size > BLOCK_SIZE) || (freeCount() < MAX_CPU))
                return ::operator new(size);
            if (useCount() > _debugMaxUsage) _debugMaxUsage = useCount();
            return _queue[++_newCount & MASK];
        }

        inline void deallocate(void* mem) {
            if ((mem < _cacheMin) || (mem > _cacheMax))
                return ::operator delete(mem);
            _queue[++_deleteCount & MASK] = mem;
        }
    };
}

#endif	/* _JAVOLUTION_HPP */
