/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#ifndef _JAVOLUTION_UTIL_FAST_MAP_HPP
#define _JAVOLUTION_UTIL_FAST_MAP_HPP

#include "javolution/lang/StringBuilder.hpp"
#include "javolution/lang/IllegalStateException.hpp"
#include "javolution/util/Map.hpp"
#include "javolution/util/FastTable.hpp"
#include "javolution/util/AbstractSet.hpp"
#include "javolution/util/NoSuchElementException.hpp"

namespace javolution {
    namespace util {
        template<class K, class V> class FastMap_API;
        template<class K, class V> class FastMapEntry_API;
        template<class K, class V> class EntrySetIterator_API;
        template<class K, class V> class KeySetIterator_API;
        template<class K, class V> class ValuesIterator_API;
        template<class K, class V> class EntrySet_API;
        template<class K, class V> class KeySet_API;
        template<class K, class V> class Values_API;

        // Defines FastMap<K,V> handler alias.
        template<class K, class V> class FastMap : public Type::Handle< FastMap_API<K, V> > {
        public:
            FastMap(Type::NullHandle = Type::Null) : Type::Handle<FastMap_API<K, V> >() {} // Null
            template<class Y > FastMap(Type::Handle<Y> const & r) : Type::Handle< FastMap_API<K, V> > (r) {}
            FastMap(FastMap_API<K, V>* ptr) : Type::Handle< FastMap_API<K, V> > (ptr) {}
        };
    }
}

/**
 * This class represents a hash table based implementation of the Map interface.
 *
 * Note: Unlike standard <code>java.util.FastMap</code>, null keys are not supported.
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/util/FastMap.html">
 *       Java - FastMap</a>
 * @version 1.0
 */
template<class K, class V>
class javolution::util::FastMap_API : public virtual javolution::util::Map_API<K, V> {
    friend class EntrySetIterator_API<K,V>;
    friend class KeySetIterator_API<K,V>;
    friend class ValuesIterator_API<K,V>;

     // Initial capacity (must be a power of 2).
     static const Type::int32 INITIAL_CAPACITY = 16;

     // Emptiness level. Can be 1 (load factor 0.5), 2 (load factor 0.25) or any greater value.
     static const Type::int32 EMPTINESS_LEVEL = 2;

     /**
      * Holds the map keys.
      */
     Type::Array<K> keyArray;

     /**
      * Holds the map values.
      */
     Type::Array<V> valueArray;

     /**
      * Holds maps current size.
      */
     Type::int32 count;

     /**
      * Holds this map mutex.
      */
     Type::Mutex mutex;

public:


     /**
      * Default constructor.
      */
     FastMap_API() {
         this->keyArray = Type::Array<K> (INITIAL_CAPACITY);
         this->valueArray = Type::Array<V> (INITIAL_CAPACITY);
         this->count = 0;
     };

    /**
     * Returns a new empty instance of this class.
     */
    static FastMap<K,V> newInstance() {
        return new FastMap_API<K,V>();
    }

    /**
     * Returns the number of key-value mappings in this map.
     */
    virtual Type::int32 size() const {
        return count;
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     */
    virtual Type::boolean isEmpty() const {
        return count == 0;
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for
     * the specified key.
     *
     * @param key the key to be tested.
     * @throws NullPointerException if the key is null.
     */
    virtual Type::boolean containsKey(K const& key) const {
        return keyArray[indexOfKey(key)] != Type::Null;
    }

    /**
     * Returns <code>true</code> if this map maps one or more keys to the
     * specified value.
     *
     * @param value the value to be tested.
     */
    virtual Type::boolean containsValue(V const& value) const {
        Type::int32 capacity = keyArray.length;
        if (value != Type::Null) { // Search for non null value.
             for (Type::int32 i = 0; i < capacity; i++) {
                 if ((valueArray[i] != Type::Null) && (value->equals(valueArray[i]))) return true;
             }
        } else { // Search for null value.
             for (Type::int32 i = 0; i < capacity; i++) {
                 if ((valueArray[i] == Type::Null) && (keyArray[i] != Type::Null)) return true;
             }
        }
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or <code>Type::Null</code> if this map contains no mapping for the key.
     *
     * @param key the key for which the value is returned.
     * @throws NullPointerException if the key is null.
     */
    virtual V const& get(K const& key) const {
        return valueArray[indexOfKey(key)];
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param key the key to be added.
     * @param value the associated value.
     * @throws NullPointerException if the key is null.
     */
    virtual V put(K const& key, V const& value) {
        Type::int32 i = indexOfKey(key);
        V oldValue = valueArray[i];
        valueArray[i] = value;
        if (keyArray[i] == Type::Null) { // New entry.
            keyArray[i] = key;
            // Check if we need to resize.
            if ((++count << EMPTINESS_LEVEL) > keyArray.length) {
                resize(keyArray.length << 1);
            }
        }
        return oldValue;
    }

    /**
     * Removes the mapping for a key from this map if it is present
     * (optional operation).
     *
     * @param key the key to be removed.
     * @throws NullPointerException if the key is null.
     */
    virtual V remove(K const& key) {
        Type::int32 i = indexOfKey(key);
        V oldValue = valueArray[i];
        if (keyArray[i] != Type::Null) { // Entry exist.
            keyArray[i] = Type::Null; // Remove key.
            valueArray[i] = Type::Null;  // And value.
            // Since we have made a hole, adjacent keys might have to shift.
            for (;;) {
                i = (i + 1) & (keyArray.length - 1); // We use a step of 1 (improve caching through memory locality).
                if (keyArray[i] == Type::Null) break; // Done.
                Type::int32 correctIndex = indexOfKey(keyArray[i]);
                if (correctIndex != i) { // Misplaced.
                    keyArray[correctIndex] = keyArray[i];
                    valueArray[correctIndex] = valueArray[i];
                    keyArray[i] = Type::Null;
                    valueArray[i] = Type::Null;
                }
            }
            // Check if we need to resize.
            if (((--count << (EMPTINESS_LEVEL+1)) <= keyArray.length) && (keyArray.length > INITIAL_CAPACITY)) {
                resize(keyArray.length >> 1);
            }
        }
        return oldValue;
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).
     */
    virtual void putAll(javolution::util::Map<K, V> m) {
        for (Iterator<Entry<K,V> > i = m->entrySet()->iterator(); i->hasNext();) {
            Entry<K,V> entry = i->next();
            put(entry->getKey(), entry->getValue());
        }
    }

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     */
    virtual void clear() {
    	for (int i=0; i < count; i++) {
    		keyArray[i] = Type::Null;
    		valueArray[i] = Type::Null;
    	}
        count = 0;
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    virtual Set< Entry<K, V> > entrySet() const {
        return new EntrySet_API<K,V>(const_cast<FastMap_API<K,V>*>(this));
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.
     */
    virtual Set<K> keySet() const {
        return new KeySet_API<K,V>(const_cast<FastMap_API<K,V>*>(this));
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.
     */
    virtual Collection<V> values() const {
        return new Values_API<K,V>(const_cast<FastMap_API<K,V>*>(this));
    }

    /**
     * Returns the textual representation of this map (entries).
     *
     * @return <code>entrySet()->toString()</code>
     */
    virtual javolution::lang::String toString() const {
        return entrySet()->toString();
    }

    /**
     * Returns the mutex to perform synchronized access to this collection.
     */
    virtual Type::Mutex& getMutex() const {
        return const_cast<FastMap_API<K,V>*>(this)->mutex;
    }

private:

    // Resizes the key and values arrays.
    // The capacity is a power of two such as: (count * 2**EMPTINESS_LEVEL) <=  capacity < (count * 2**(EMPTINESS_LEVEL+1))
    void resize(Type::int32 newCapacity) {
    	Type::Array<K> newKeys = Type::Array<K>(newCapacity);
    	Type::Array<V> newValues = Type::Array<V>(newCapacity);
        Type::int32 capacity = keyArray.length;
        for (Type::int32 i = 0; i < capacity; i++) {
            if (keyArray[i] != Type::Null) {
                Type::int32 newIndex = keyArray[i]->hashCode() & (newCapacity - 1);
                while (newKeys[newIndex] != Type::Null) { // Find empty slot.
                    newIndex = (newIndex + 1) & (newCapacity - 1);
                }
                newKeys[newIndex] = keyArray[i];
                newValues[newIndex] = valueArray[i];
            }
        }
        keyArray = newKeys;
        valueArray = newValues;
    }
    
    // Returns the index of the specified key in the map (points to a null key if key not present).
    Type::int32 indexOfKey(K const& key) const {
        Type::int32 capacity = keyArray.length;
        Type::int32 index = key->hashCode() & (capacity - 1); // NullPointerException is key is null (not supported).
        while (keyArray[index] != Type::Null) {
            if (key->equals(keyArray[index])) return index;
            index = (index + 1) & (capacity - 1);
        }
        return index; // Not found.
    }

};

//////////////////////////////////
// FastMap Entry Implementation //
//////////////////////////////////

template<class K, class V>
class javolution::util::FastMapEntry_API : public virtual javolution::util::Entry_API<K,V> {

    K key;
    V value;

public:

    FastMapEntry_API(K const& thisKey, V const& thisValue) {
        this->key = thisKey;
        this->value = thisValue;
    }

    K const& getKey() const {
        return key;
    }

    V const& getValue() const {
        return value;
    }

    V setValue(V const& thisValue) {
        V oldValue = this->value;
        this->value = thisValue;
        return oldValue;
    }

    Type::boolean equals(javolution::lang::Object obj) const {
        Type::Handle<FastMapEntry_API<K, V> > that = Type::dynamic_handle_cast<FastMapEntry_API<K,V> >(obj);
        if (that == Type::Null) return false;
        return this->equals(that);
    }
    Type::boolean equals(Type::Handle<FastMapEntry_API<K, V> > const& that) const {
        if ((key == Type::Null) && (that->getKey() != Type::Null)) return false;
        if (!key->equals(that->getKey())) return false;
        if ((value == Type::Null) && (that->getValue() != Type::Null)) return false;
        if (!value->equals(that->getValue())) return false;
        return true;
    }

    Type::int32 hashCode() const {
        javolution::lang::Object k = key;
        javolution::lang::Object v = value;
        return ((key != Type::Null) ? key->hashCode() : 0) + ((value != Type::Null) ? value->hashCode() : 0);
    }

    javolution::lang::String toString() const {
        javolution::lang::StringBuilder sb = new javolution::lang::StringBuilder_API();
        sb->append(L"(key: ");
        sb->append(getKey());
        sb->append(L", value: ");
        sb->append(getValue());
        sb->append(L")");
        return sb->toString();
    }

};

///////////////
// Iterators //
///////////////

template<class K, class V>
class javolution::util::EntrySetIterator_API : public virtual javolution::util::Iterator_API<Entry<K,V> > {

     FastMap<K,V> map;
     Type::int32 capacity;
     Type::int32 currentIndex;
     Type::int32 nextIndex;
     Entry<K,V> currentEntry;

public:

     EntrySetIterator_API(FastMap<K,V> const& thisMap) {
         this->map = thisMap;
         this->capacity = map->keyArray.length;
         this->currentIndex = -1;
         this->nextIndex = 0;
         // Set the next index position.
         while ((nextIndex != map->keyArray.length) && (map->keyArray[nextIndex] == Type::Null)) {
             nextIndex++;
         }
     }

     Type::boolean hasNext() const {
        return (nextIndex != capacity);
    }

    Entry<K,V> const& next() {
        if (nextIndex == capacity) throw javolution::util::NoSuchElementException_API::newInstance(L"Method Iterator.next()");
        currentIndex = nextIndex++;
        while ((nextIndex != capacity) && (map->keyArray[nextIndex] == Type::Null)) {
            nextIndex++;
        }
        currentEntry = new FastMapEntry_API<K,V>(
        		map->keyArray[currentIndex], map->valueArray[currentIndex]);
        return currentEntry;
    }

    void remove() {
        if (currentIndex == -1) throw javolution::lang::IllegalStateException_API::newInstance(L"No current key to remove");
        map->remove(map->keyArray[currentIndex]);
        currentIndex = -1;
    }

};

template<class K, class V>
class javolution::util::KeySetIterator_API : public virtual javolution::util::Iterator_API<K> {

	FastMap<K,V> map;
    Type::int32 capacity;
    Type::int32 currentIndex;
    Type::int32 nextIndex;

public:

    KeySetIterator_API(FastMap<K,V> const& thisMap) {
        this->map = thisMap;
        this->capacity = map->keyArray.length;
        this->currentIndex = -1;
        this->nextIndex = 0;
        // Set the next index position.
        while ((nextIndex != capacity) && (map->keyArray[nextIndex] == Type::Null)) {
            nextIndex++;
        }
    }

    Type::boolean hasNext() const {
        return (nextIndex != capacity);
    }

    K const& next() {
        if (nextIndex == capacity) throw javolution::util::NoSuchElementException_API::newInstance(L"Method Iterator.next()");
        currentIndex = nextIndex++;
        while ((nextIndex != capacity) && (map->keyArray[nextIndex] == Type::Null)) {
            nextIndex++;
        }
        return map->keyArray[currentIndex];
    }

    void remove() {
        if (currentIndex == -1) throw javolution::lang::IllegalStateException_API::newInstance(L"No current key to remove");
        map->remove(map->keyArray[currentIndex]);
        currentIndex = -1;
    }

};

template<class K, class V>
class javolution::util::ValuesIterator_API : public virtual javolution::util::Iterator_API<V> {

    FastMap<K,V> map;
    Type::int32 capacity;
    Type::int32 currentIndex;
    Type::int32 nextIndex;

public:

    ValuesIterator_API(FastMap<K,V> const& thisMap) {
        this->map = thisMap;
        this->capacity = map->keyArray.length;
        this->currentIndex = -1;
        this->nextIndex = 0;
        // Set the next index position.
        while ((nextIndex != map->keyArray.length) && (map->keyArray[nextIndex] == Type::Null)) {
            nextIndex++;
        }
    }

    Type::boolean hasNext() const {
        return (nextIndex != capacity);
    }

    V const& next() {
        if (nextIndex == capacity) throw javolution::util::NoSuchElementException_API::newInstance(L"Method Iterator.next()");
        currentIndex = nextIndex++;
        while ((nextIndex != capacity) && (map->keyArray[nextIndex] == Type::Null)) {
            nextIndex++;
        }
        return map->valueArray[currentIndex];
    }

    void remove() {
        if (currentIndex == -1) throw javolution::lang::IllegalStateException_API::newInstance(L"No current key to remove");
        map->remove(map->keyArray[currentIndex]);
        currentIndex = -1;
    }

};

/////////////////
// Collections //
/////////////////

template<class K, class V>
class javolution::util::EntrySet_API : public javolution::util::AbstractSet_API< Entry<K,V> > {

	FastMap<K,V> map;

public:

	EntrySet_API(FastMap<K,V> const& thisMap) {
        this->map = thisMap;
    }

	Iterator< Entry<K,V> > iterator() const {
        return Iterator<Entry<K,V> > (new EntrySetIterator_API<K,V> (map));
    }

	Type::int32 size() const {
        return map->size();
    }

	Type::Mutex& getMutex() const {
        return map->getMutex();
    }

	void clear() { // Optimization.
        map->clear();
    }
};

template<class K, class V>
class javolution::util::KeySet_API : public javolution::util::AbstractSet_API<K> {

    FastMap<K,V> map;

public:

    KeySet_API(FastMap<K,V> const& thisMap) {
        this->map = thisMap;
    }

    Iterator<K> iterator() const {
        return Iterator<K> (new KeySetIterator_API<K,V> (map));
    }

    Type::int32 size() const {
        return map->size();
    }

    Type::Mutex& getMutex() const {
        return map->getMutex();
    }

    void clear() { // Optimization.
        map->clear();
    }

};

template<class K, class V>
class javolution::util::Values_API : public javolution::util::AbstractCollection_API<V> {

    FastMap<K,V> map;

public:

    Values_API(FastMap<K,V> const& thisMap) {
        this->map = thisMap;
    }

    Iterator<V> iterator() const {
        return Iterator<V> (new ValuesIterator_API<K,V> (map));
    }

    Type::int32 size() const {
        return map->size();
    }

    Type::Mutex& getMutex() const {
        return map->getMutex();
    }

    void clear() { // Optimization.
        map->clear();
    }

};

#endif
