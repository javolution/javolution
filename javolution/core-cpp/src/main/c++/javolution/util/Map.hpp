/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_MAP_HPP
#define _JAVOLUTION_UTIL_MAP_HPP

#include "javolution/lang/Object.hpp"
#include "javolution/util/Collection.hpp"
#include "javolution/util/Set.hpp"

namespace javolution {
    namespace util {

        // Defines the Entry<K,V> handler alias.
        template<class T, class V>
        class Entry_API;
        template<class K, class V> class Entry : public Type::Handle< Entry_API<K, V> > {
        public:
            Entry(Type::NullHandle = Type::Null) : Type::Handle< Entry_API<K,V> >() {} // Null
            template<class Y > Entry(Type::Handle<Y> const & r) : Type::Handle< Entry_API<K,V> > (r) {}
            Entry(Entry_API<K, V>* ptr) : Type::Handle<Entry_API<K, V> > (ptr) {}
        };

        // Defines the Map<K,V> handler alias.
        template<class T, class V>
        class Map_API;
        template<class K, class V> class Map : public Type::Handle<Map_API<K, V> > {
        public:
            Map(Type::NullHandle = Type::Null) : Type::Handle<Map_API<K, V> >() {} // Null
            template<class Y > Map(Type::Handle<Y> const & r) : Type::Handle<Map_API<K, V> > (r) {}
            Map(Map_API<K, V>* ptr) : Type::Handle<Map_API<K, V> > (ptr) {}
        };
    }
}

/**
 * This class represents an object that maps keys to values.
 * A map cannot contain duplicate keys; each key can map to at most one value.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/Map.html">
 *       Java - Map</a>
 * @version 1.0
 */
template <class K, class V> class javolution::util::Map_API : public virtual javolution::lang::Object_API {
public:

    /**
     * Returns the number of key-value mappings in this map.
     */
    virtual Type::int32 size() const = 0;

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     */
    virtual Type::boolean isEmpty() const = 0;

    /**
     * Returns <code>true</code> if this map contains a mapping for
     * the specified key.
     */
    virtual Type::boolean containsKey(K const& key) const = 0;

    /**
     * Returns <code>true</code> if this map maps one or more keys to the
     * specified value.
     */
    virtual Type::boolean containsValue(V const& value) const = 0;

    /**
     * Returns the value to which the specified key is mapped,
     * or <code>Type::Null</code> if this map contains no mapping for the key.
     */
    virtual V const& get(K const& key) const = 0;

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     */
    virtual V put(K const& key, V const& value) = 0;

    /**
     * Removes the mapping for a key from this map if it is present
     * (optional operation).
     */
    virtual V remove(K const& key) = 0;

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).
     */
    virtual void putAll(javolution::util::Map<K, V> m) = 0;

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     */
    virtual void clear() = 0;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    virtual Set<K> keySet() const = 0;

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.
     */
    virtual Collection<V> values() const = 0;

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    virtual Set<Entry<K,V> > entrySet() const = 0;

};

/**
 * This class represents a map entry.
 */
template <class K, class V> class javolution::util::Entry_API : public virtual javolution::lang::Object_API {
public:
    /**
     * Returns the key corresponding to this entry.
     */
    virtual K const& getKey() const = 0;

    /**
     * Returns the value corresponding to this entry.
     */
    virtual V const& getValue() const = 0;

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation).
     */
    virtual V setValue(V const& value) = 0;

    /**
     * Compares the specified object with this entry for equality.
     */
    virtual Type::boolean equals(javolution::lang::Object o) const = 0;

    /**
     * Returns the hash code value for this map entry.  The hash code
     * of a map entry <code>e</code> is defined to be: <pre>
     *     (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
     *     (e.getValue()==null ? 0 : e.getValue().hashCode())
     * </pre>
     */
    virtual Type::int32 hashCode() const = 0;
};

#endif
