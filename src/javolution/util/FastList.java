/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.IOException;

import j2me.io.ObjectInputStream;
import j2me.io.ObjectOutputStream;
import j2me.io.Serializable;
import j2me.lang.IllegalStateException;
import j2me.util.Collection;
import j2me.util.Iterator;
import j2me.util.List;
import j2me.util.ListIterator;
import java.util.NoSuchElementException;
import javolution.lang.Reusable;
import javolution.realtime.ObjectFactory;
import javolution.realtime.Realtime;

/**
 * <p> This class represents a linked list with real-time behavior; 
 *     smooth capacity increase and no memory allocation as long as the
 *     list size does not exceed its capacity.</p>
 * 
 * <p> All of the operations perform as could be expected for a doubly-linked
 *     list ({@link #addLast insertion}/{@link #removeLast() deletion}
 *     at the end of the list are nonetheless the fastest). 
 *     Operations that index into the list will traverse the list from
 *     the begining or the end whichever is closer to the specified index. 
 *     Random access operations can be significantly accelerated by 
 *     {@link #subList splitting} the list into smaller ones.</p> 
 * 
 * <p> {@link FastList} (as for any {@link FastCollection} sub-class) supports
 *     thread-safe, fast iterations without using iterators.<pre>
 *     FastList&lt;String&gt; list = new FastList&lt;String&gt;();
 *     for (FastList.Node&lt;String&gt; n = list.headNode(), end = list.tailNode(); (n = n.getNextNode()) != end;) {
 *         String value = n.getValue(); // No typecast necessary.    
 *     }</pre></p>
 *     
 * <p> {@link FastList} are fully {@link Reusable reusable}, they maintain 
 *     internal pools of {@link Node nodes} objects. When a node is removed
 *     from its list, it is automatically restored to its pool.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.2, April 2, 2005
 */
public class FastList/*<E>*/extends FastCollection/*<E>*/implements Reusable,
        List/*<E>*/, Serializable {

    /**
     * Holds the main list factory.
     */
    private static final Factory LIST_FACTORY = new Factory() {

        public Object create() {
            return new FastList();
        }

        public void cleanup(Object obj) {
            ((FastList) obj).reset();
        }
    };

    /**
     * Holds the parent list (if any).
     */
    private transient FastList/*<E>*/_parent;

    /**
     * Holds the node marking the beginning of the list (not included).
     */
    private transient Node/*<E>*/_head = new Node();

    /**
     * Holds the node marking the end of the list (not included).
     */
    private transient Node/*<E>*/_tail = new Node();

    /**
     * Holds the current size.
     */
    private transient int _size;

    /**
     * Creates a fast list of small initial capacity.
     */
    public FastList() {
        this(0);
    }

    /**
     * Creates a fast list of specified initial capacity.
     * Unless the list size exceeds the specified capacity no memory 
     * allocation is ever performed.
     * 
     * @param capacity the initial capacity.
     */
    public FastList(int capacity) {
        _head._next = _tail;
        _tail._previous = _head;
        Node/*<E>*/previous = _tail;
        for (int i = 0; i < capacity; i += 4) { //Four nodes at a time.
            Node/*<E>*/newNode = (Node/*<E>*/) Node.FACTORY.newObject();
            newNode._previous = previous;
            previous._next = newNode;
            previous = newNode._next._next._next;
        }
    }

    /**
     * Creates a fast list containing the specified values, in the order they
     * are returned by the collection's iterator.
     *
     * @param values the values to be placed into this fast list.
     */
    public FastList(Collection/*<? extends E>*/values) {
        this(values.size());
        addAll(values);
    }

    /**
     * Constructor for sub-lists.
     * 
     * @param isSublist indicates if sub-list (must be true).
     */
    private FastList(boolean isSublist) {
    }

    /**
     * Returns a fast list allocated from the stack when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new, preallocated or recycled list instance.
     */
    public static FastList newInstance() {
        return (FastList) LIST_FACTORY.object();
    }

    /**
     * Appends the specified value to the end of this list
     * (equivalent to {@link #addLast}).
     *
     * @param value the value to be appended to this list.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     */
    public final boolean add(Object/*E*/value) {
        addLast(value);
        return true;
    }

    /**
     * Compares the specified object with this list for equality.  Returns
     * <code>true</code> if and only if both lists contain the same values
     * in the same order.
     *
     * @param obj the object to be compared for equality with this list.
     * @return <code>true</code> if the specified object is equal to this list;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        final FastComparator comp = this.getValueComparator();
        if (obj == this)
            return true;
        if (obj instanceof FastList)
            return equals((FastList) obj);
        if (obj instanceof List) {
            List/*<?>*/list = (List) obj;
            if (_size != list.size())
                return false;
            Node n1 = this._head;
            Iterator/*<?>*/i2 = list.iterator();
            for (int i = _size; i-- != 0;) {
                n1 = n1._next;
                Object o1 = n1._value;
                Object o2 = i2.next();
                if (!comp.areEqual(o1, o2))
                    return false;
            }
            return true;
        }
        return false;
    }

    private boolean equals(FastList that) {
        if (this._size != that._size)
            return false;
        final FastComparator comp = this.getValueComparator();
        for (Node n1 = _head._next, n2 = that._head._next, end = _tail; n1 != end;) {
            if (!comp.areEqual(n1._value, n2._value))
                return false;
            n1 = n1._next;
            n2 = n2._next;
        }
        return true;
    }

    /**
     * Returns the hash code value for this list.  The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  h = 1;
     *  Iterator i = list.iterator();
     *  while (i.hasNext()) {
     *      Object obj = i.next();
     *      h = 31 * h +  this.getValueComparator().hashCodeOf(obj);
     *  }
     * </pre>
     *
     * @return the hash code value for this list.
     */
    public int hashCode() {
        final FastComparator comp = this.getValueComparator();
        int h = 1;
        for (Node n = _head, end = _tail; (n = n._next) != end;) {
            h = 31 * h + comp.hashCodeOf(n._value);
        }
        return h;
    }

    /**
     * Returns the value at the specified position in this list.
     *
     * @param index index of value to return.
     * @return the value at the specified position in this list.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final Object/*E*/get(int index) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        return nodeAt(index)._value;
    }

    /**
     * Replaces the value at the specified position in this list with the
     * specified value.
     *
     * @param index index of value to replace.
     * @param value value to be stored at the specified position.
     * @return the value previously at the specified position.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final Object/*E*/set(int index, Object/*E*/value) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final Node/*<E>*/node = nodeAt(index);
        Object/*E*/previousValue = node._value;
        node._value = value;
        return previousValue;
    }

    /**
     * Inserts the specified value at the specified position in this list.
     * Shifts the value currently at that position
     * (if any) and any subsequent values to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified value is to be inserted.
     * @param value value to be inserted.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > size())</code>
     */
    public final void add(int index, Object/*E*/value) {
        if ((index < 0) || (index > _size))
            throw new IndexOutOfBoundsException("index: " + index);
        addBefore(nodeAt(index), value);
    }

    /**
     * Inserts all of the values in the specified collection into this
     * list at the specified position. Shifts the value currently at that
     * position (if any) and any subsequent values to the right 
     * (increases their indices). 
     *
     * @param index index at which to insert first value from the specified
     *        collection.
     * @param values the values to be inserted into this list.
     * @return <code>true</code> if this list changed as a result of the call;
     *         <code>false</code> otherwise.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index > size())</code>
     */
    public final boolean addAll(int index, Collection/*<? extends E>*/values) {
        if ((index < 0) || (index > _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final Node indexNode = nodeAt(index);
        if (values instanceof FastList/*<?>*/) {
            FastList/*<? extends E>*/list = (FastList/*<? extends E>*/) values;
            for (Node/*<? extends E>*/n = list._head, end = list._tail; (n = n._next) != end;) {
                addBefore(indexNode, n._value);
            }
        } else {
            Iterator/*<? extends E>*/i = values.iterator();
            while (i.hasNext()) {
                addBefore(indexNode, i.next());
            }
        }
        return values.size() != 0;
    }

    /**
     * Removes the value at the specified position in this list.
     * Shifts any subsequent values to the left (subtracts one
     * from their indices). Returns the value that was removed from the
     * list.
     *
     * @param index the index of the value to removed.
     * @return the value previously at the specified position.
     * @throws IndexOutOfBoundsException if <code>(index < 0) || 
     *         (index >= size())</code>
     */
    public final Object/*E*/remove(int index) {
        if ((index < 0) || (index >= _size))
            throw new IndexOutOfBoundsException("index: " + index);
        final Node/*<E>*/node = nodeAt(index);
        Object/*E*/previousValue = node._value;
        delete(node);
        return previousValue;
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * value, or -1 if this list does not contain this value.
     *
     * @param value the value to search for.
     * @return the index in this list of the first occurrence of the specified
     *         value, or -1 if this list does not contain this value.
     */
    public final int indexOf(Object value) {
        final FastComparator comp = this.getValueComparator();
        int index = 0;
        for (Node n = _head, end = _tail; (n = n._next) != end; index++) {
            if (comp.areEqual(value, n._value))
                return index;
        }
        return -1;
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * value, or -1 if this list does not contain this value.
     *
     * @param value the value to search for.
     * @return the index in this list of the last occurrence of the specified
     *         value, or -1 if this list does not contain this value.
     */
    public final int lastIndexOf(Object value) {
        final FastComparator comp = this.getValueComparator();
        int index = size() - 1;
        for (Node n = _tail, end = _head; (n = n._previous) != end; index--) {
            if (comp.areEqual(value, n._value)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Returns a list iterator over the values in this list in proper 
     * sequence.
     *
     * @return a list iterator of the values in this list (in proper
     *         sequence).
     * @deprecated Applications should use direct {@link Node} iterations
     *             (faster, thread-safe and no object creation).
     */
    public final ListIterator/*<E>*/listIterator() {
        return listIterator(0);
    }

    /**
     * Returns a list iterator of the values in this list (in proper sequence).
     * The specified index indicates the first value that would be returned by
     * an initial call to the <code>next</code> method.  An initial call to
     * the <code>previous</code> method would return the value with the
     * specified index minus one.
     *
     * @param index index of first value to be returned from the
     *        list iterator (by a call to the <code>next</code> method).
     * @return a list iterator of the values in this list
     *         starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *         &lt; 0 || index &gt; size()).
     * @deprecated Applications should use direct {@link Node} iterations
     *             (faster, thread-safe and no object creation).
     */
    public final ListIterator/*<E>*/listIterator(int index) {
        if ((index >= 0) && (index <= _size)) {
            FastListIterator i = new FastListIterator();
            i._length = this._size;
            i._nextNode = nodeAt(index);
            i._nextIndex = index;
            return i;
        } else {
            throw new IndexOutOfBoundsException("index: " + index
                    + " for list of size: " + _size);
        }
    }

    /**
     * Returns a view of the portion of this list between the specified
     * indexes (instance of {@link FastList} allocated from the "stack" when
     * executing in a {@link javolution.realtime.PoolContext PoolContext}).
     * If the specified indexes are equal, the returned list is empty. 
     * The returned list is backed by this list, so non-structural changes in
     * the returned list are reflected in this list, and vice-versa. 
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays). Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of values from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for <code>indexOf</code> and
     * <code>lastIndexOf</code>, and all of the algorithms in the
     * <code>Collections</code> class can be applied to a subList.
     *
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list (structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results).
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     * 
     * @throws IndexOutOfBoundsException if <code>(fromIndex &lt; 0 ||
     *          toIndex &gt; size || fromIndex &gt; toIndex)</code>
     */
    public final List/*<E>*/subList(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex > _size) || (fromIndex > toIndex))
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                    + ", toIndex: " + toIndex + " for list of size: " + _size);
        FastList/*<E>*/subList = new FastList/*<E>*/(true);
        subList._parent = this;
        subList._head = nodeAt(fromIndex)._previous;
        subList._tail = nodeAt(toIndex);
        subList._size = toIndex - fromIndex;
        return subList;
    }

    /**
     * Returns the first value of this list.
     *
     * @return this list's first value.
     * @throws NoSuchElementException if this list is empty.
     */
    public final Object/*E*/getFirst() {
        final Node/*<E>*/ node = _head._next;
        if (node == _tail)
            throw new NoSuchElementException();
        return node._value;
    }

    /**
     * Returns the last value of this list.
     *
     * @return this list's last value.
     * @throws NoSuchElementException if this list is empty.
     */
    public final Object/*E*/getLast() {
        final Node/*<E>*/ node = _tail._previous;
        if (node == _head)
            throw new NoSuchElementException();
        return node._value;
    }

    /**
     * Inserts the specified value at the beginning of this list.
     * 
     * @param value the value to be inserted.
     */
    public final void addFirst(Object/*E*/value) {
        addBefore(_head._next, value);
    }

    /**
     * Appends the specified value to the end of this list <i>(fast)</i>.
     * 
     * @param value the value to be inserted.
     */
    public final void addLast(Object/*E*/value) { // Optimized.
        _size++;
        if (_parent == null) { // Main list.
            Node newTail = _tail._next;
            if (newTail == null) {
                newTail = _tail._next = (Node) Node.FACTORY.newObject();
                newTail._previous = _tail;
            }
            _tail._value = value;
            _tail = newTail;
        } else {
            _parent.addBefore(_tail, value);
        }
    }

    /**
     * Removes and returns the first value of this list.
     *
     * @return this list's first value before this call.
     * @throws NoSuchElementException if this list is empty.
     */
    public final Object/*E*/removeFirst() {
        final Node/*<E>*/ first = _head._next;
        if (first == _tail)
            throw new NoSuchElementException();
		Object/*E*/ previousValue = first._value;
        delete(first);
        return previousValue;
    }

    /**
     * Removes and returns the last value of this list <i>(fast)</i>.
     *
     * @return this list's last value before this call.
     * @throws NoSuchElementException if this list is empty.
     */
    public final Object/*E*/removeLast() {
        if (_size == 0)
            throw new NoSuchElementException();
        _size--;
        final Node/*<E>*/ last = _tail._previous;
		final Object/*E*/ previousValue = last._value;
        if (_parent == null) {
            _tail = last;
            last._value = null;
        } else {
            _parent.delete(last);
        }
        return previousValue;
    }

    ///////////////////////
    // Nodes operations. //
    ///////////////////////

    /**
     * Returns the head node of this list; it is the node such as 
     * <code>headNode().getNextNode()</code> holds the first list value.
     * 
     * @return the head node.
     */
    public final Node/*<E>*/headNode() {
        return _head;
    }

    /**
     * Returns the tail node of this list; it is the node such as
     * <code>tailNode().getPreviousNode()</code> holds the last list value.
     * 
     * @return the tail record.
     */
    public final Node/*<E>*/tailNode() {
        return _tail;
    }

    /**
     * Inserts the specified value before the specified Node.
     * 
     * @param next the Node before which this value is inserted.
     * @param value the value to be inserted.   
     */
    public final void addBefore(Node/*<E>*/ next, Object/*E*/value) {
        _size++;
        if (_parent == null) { // Main list.
            Node newNode = _tail._next;
            if (newNode == null) { // Increases capacity.
                newNode = _tail._next = (Node) Node.FACTORY.newObject();
                newNode._previous = _tail;
            }
            // Detaches newNode.
            final Node tailNext = _tail._next = newNode._next;
            if (tailNext != null) {
                tailNext._previous = _tail;
            }
            // Inserts before next.
            final Node previous = next._previous;
            previous._next = newNode;
            next._previous = newNode;
            newNode._next = next;
            newNode._previous = previous;

            newNode._value = value;
        } else {
            _parent.addBefore(next, value);
        }
    }

    /**
     * Returns the node at the specified index. This method returns
     * the {@link #headNode} node when <code>index &lt; 0</code> or 
     * the {@link #tailNode} node when <code>index &gt;= size()</code>.
     * 
     * @param index the index of the Node to return.
     */
    private final Node/*<E>*/nodeAt(int index) {
        if (index <= (_size >> 1)) { // Forward search.
            Node/*<E>*/node = _head;
            for (int i = index; i-- >= 0;) {
                node = node._next;
            }
            return node;
        } else { // Backward search.
            Node/*<E>*/node = _tail;
            for (int i = _size - index; i-- > 0;) {
                node = node._previous;
            }
            return node;
        }
    }

    // Implements FastCollection abstract method.
    public final Record headRecord() {
        return _head;
    }

    // Implements FastCollection abstract method.
    public final Record tailRecord() {
        return _tail;
    }

    // Implements FastCollection abstract method.
    public final Object/*E*/valueOf(Record record) {
        return ((Node/*<E>*/) record)._value;
    }

    // Implements FastCollection abstract method.
    public final void delete(Record record) {
        Node/*<E>*/node = (Node/*<E>*/) record;
        _size--;
        if (_parent == null) { // Main list.
            node._value = null;
            // Detaches.
            node._previous._next = node._next;
            node._next._previous = node._previous;
            // Inserts after _tail.
            final Node/*<E>*/next = _tail._next;
            node._previous = _tail;
            node._next = next;
            _tail._next = node;
            if (next != null) {
                next._previous = node;
            }
        } else {
            _parent.delete(node);
        }
    }

    ///////////////////////
    // Contract Methods. //
    ///////////////////////

    // Implements abstract method.
    public final int size() {
        return _size;
    }

    // Overrides (optimization).
    public final void clear() {
        if (_parent == null) {
            for (Node/*<E>*/n = _head, end = _tail; (n = n._next) != end;) {
                n._value = null;
            }
            _tail = _head._next;
            _size = 0;
        } else {
            while (_head._next != _tail) {
                _parent.delete(_head._next);
            }
            _size = 0;
        }
    }

    // Implements Reusable.
    public void reset() {
        this.clear();
        this.setValueComparator(FastComparator.DEFAULT);
    }

    // Overrides (external references which might not be on the heap).
    public boolean move(ObjectSpace os) {
        if (super.move(os)) {
            if (_parent == null) { // Main List
                for (Node n = _head, end = _tail; (n = n._next) != end;) {
                    if (n._value instanceof Realtime) {
                        ((Realtime) n._value).move(os);
                    }
                }
            } else {
                _parent.move(os);
            }
            return true;
        }
        return false;
    }

    // Requires special handling during de-serialization process.
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        _head = new Node();
        _tail = new Node();
        _head._next = _tail;
        _tail._previous = _head;
        final int size = stream.readInt();
        setValueComparator((FastComparator) stream.readObject());
        for (int i = size; i-- != 0;) {
            addLast((Object/*E*/) stream.readObject());
        }
    }

    // Requires special handling during serialization process.
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeInt(_size);
        stream.writeObject(getValueComparator());
        Node node = _head;
        for (int i = _size; i-- != 0;) {
            node = node._next;
            stream.writeObject(node._value);
        }
    }

    /**
     * This class represents a {@link FastList} node; it allows for direct 
     * iteration over the list {@link #getValue values}.
     */
    public static final class Node/*<E>*/implements Record/*<E>*/ {

        /**
         * Holds the node factory (to allow for pre-allocation).
         */
        private static ObjectFactory FACTORY = new ObjectFactory() {
            // Creates 4 nodes at a time.
            public Object create() {
                Node n0 = new Node();

                Node n1 = new Node();
                n1._previous = n0;
                n0._next = n1;

                Node n2 = new Node();
                n2._previous = n1;
                n1._next = n2;

                Node n3 = new Node();
                n3._previous = n2;
                n2._next = n3;

                return n0;
            }
        };

        /**
         * Holds the next node.
         */
        private Node/*<E>*/_next;

        /**
         * Holds the previous node.
         */
        private Node/*<E>*/_previous;

        /**
         * Holds the node value.
         */
        private Object/*E*/_value;

        /**
         * Default constructor (allows sub-classing).
         */
        private Node() {
        }

        /**
         * Returns the value for this node.
         * 
         * @return the node value.
         */
        public final Object/*E*/getValue() {
            return _value;
        }

        /**
         * Returns the node after this one.
         * 
         * @return the next node.
         */
        public final Node/*<E>*/getNextNode() {
            return _next;
        }

        /**
         * Returns the node before this one.
         * 
         * @return the previous node.
         */
        public final Node/*<E>*/getPreviousNode() {
            return _previous;
        }

        // Implements Record interface.
        public final Record/*<E>*/getNextRecord() {
            return _next;
        }

        // Implements Record interface.
        public final Record/*<E>*/getPreviousRecord() {
            return _previous;
        }
    }

    /**
     * This inner class implements a fast list iterator.
     */
    private final class FastListIterator implements ListIterator/*<E>*/{

        private Node/*<E>*/_nextNode;

        private Node/*<E>*/_currentNode;

        private int _length;

        private int _nextIndex;

        public boolean hasNext() {
            return (_nextIndex != _length);
        }

        public Object/*E*/next() {
            if (_nextIndex == _length)
                throw new NoSuchElementException();
            _nextIndex++;
            _currentNode = _nextNode;
            _nextNode = _nextNode._next;
            return _currentNode._value;
        }

        public int nextIndex() {
            return _nextIndex;
        }

        public boolean hasPrevious() {
            return _nextIndex != 0;
        }

        public Object/*E*/previous() {
            if (_nextIndex == 0)
                throw new NoSuchElementException();
            _nextIndex--;
            _currentNode = _nextNode = _nextNode._previous;
            return _currentNode._value;
        }

        public int previousIndex() {
            return _nextIndex - 1;
        }

        public void add(Object/*E*/o) {
            FastList.this.addBefore(_nextNode, o);
            _currentNode = null;
            _length++;
            _nextIndex++;
        }

        public void set(Object/*E*/o) {
            if (_currentNode != null) {
                _currentNode._value = o;
            } else {
                throw new IllegalStateException();
            }
        }

        public void remove() {
            if (_currentNode != null) {
                if (_nextNode == _currentNode) { // previous() has been called.
                    _nextNode = _nextNode._next;
                } else {
                    _nextIndex--;
                }
                FastList.this.delete(_currentNode);
                _currentNode = null;
                _length--;
            } else {
                throw new IllegalStateException();
            }
        }
    }

}