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
import j2me.util.NoSuchElementException;

import javolution.lang.Reusable;
import javolution.realtime.ObjectFactory;
import javolution.realtime.Realtime;

/**
 * <p> This class represents a linked-list with real-time behavior
 *     (no copy/resize ever performed).</p>
 * 
 * <p> All of the operations perform as could be expected for a doubly-linked
 *     list ({@link #addLast insertion}/{@link #removeLast() deletion}
 *     at the end of the list are nonetheless the fastest). 
 *     Operations that index into the list will traverse the list from
 *     the begining or the end whichever is closer to the specified index. 
 *     Random access operations can be significantly accelerated by 
 *     {@link #subList splitting} the list into smaller ones.</p> 
 * 
 * <p> {@link FastList} instances have (non thread-safe) {@link #fastIterator}
 *     and support custom {@link #setElementComparator element comparators}.
 *     </p>
 *     
 * <p> To be fully {@link Reusable reusable}, {@link FastList} maintains an 
 *     internal pool of <code>Node</code> objects. When a node is removed
 *     from the list, it is automatically restored to the pool.</p>
 *     
 * <p> {@link FastList} are fully {@link Reusable} and can also be part of 
 *     higher level {@link Reusable} components (the list maximum size 
 *     determinates the maximum number of node allocations performed).</p>
 * 
 * <p> This implementation is not synchronized. Multiple threads accessing
 *     or modifying the collection must be synchronized externally.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2005
 */
public class FastList extends FastCollection implements Reusable, List,
        Serializable {

    /**
     * Holds the list factory.
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
     * Holds the sub-list factory.
     */
    private static final Factory SUBLIST_FACTORY = new Factory() {

        public Object create() {
            return new FastList(null);
        }

        // No need to cleanup, 
        // external objects references cleared by parent list.
    };

    /**
     * Holds the parent list (if any).
     */
    private transient FastList _parent;

    /**
     * Holds the node marking the beginning of the list (not included).
     */
    private transient Node _head;

    /**
     * Holds the node marking the end of the list (not included).
     */
    private transient Node _tail;

    /**
     * Holds the current size.
     */
    private transient int _size;

    /**
     * Creates a {@link FastList} on the heap.
     */
    public FastList() {
        _head = new Node();
        _tail = new Node();
        _head._next = _tail;
        _tail._previous = _head;
        _fastIterator = new FastListIterator();
    }

    /**
     * Constructor for sub-lists.
     * 
     * @param parent list.
     */
    private FastList(FastList parent) {
        _fastIterator = new FastListIterator();
    }

    /**
     * Returns a {@link FastList} allocated from the stack when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new, preallocated or recycled list instance.
     */
    public static FastList newInstance() {
        return (FastList) LIST_FACTORY.object();
    }

    /**
     * Appends the specified element to the end of this list
     * (equivalent to {@link #addLast}).
     *
     * @param element the element to be appended to this list.
     * @return <code>true</code> (as per the general contract of the
     *         <code>Collection.add</code> method).
     */
    public boolean add(Object element) {
        addLast(element);
        return true;
    }

    /**
     * Compares the specified object with this list for equality.  Returns
     * <code>true</code> if and only if both lists contain the same elements
     * in the same order.
     *
     * @param obj the object to be compared for equality with this list.
     * @return <code>true</code> if the specified object is equal to this list;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        final FastComparator comp = this.getElementComparator();
        if (obj == this) {
            return true;
        } else if (obj instanceof FastList) {
            FastList list = (FastList) obj;
            if (this._size == list._size) {
                Node n1 = this._head;
                Node n2 = list._head;
                for (int i = _size; i-- != 0;) {
                    n1 = n1._next;
                    Object o1 = n1._element;
                    n2 = n2._next;
                    Object o2 = n2._element;
                    if (!comp.areEqual(o1, o2)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else if (obj instanceof List) {
            List list = (List) obj;
            if (this._size == list.size()) {
                Node n1 = this._head;
                Iterator i2 = list.iterator();
                for (int i = _size; i-- != 0;) {
                    n1 = n1._next;
                    Object o1 = n1._element;
                    Object o2 = i2.next();
                    if (!comp.areEqual(o1, o2)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else { // Not a List.
            return false;
        }
    }

    /**
     * Returns the hash code value for this list.  The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  h = 1;
     *  Iterator i = list.iterator();
     *  while (i.hasNext()) {
     *      Object obj = i.next();
     *      h = 31 * h +  this.getElementComparator().hashCodeOf(obj);
     *  }
     * </pre>
     *
     * @return the hash code value for this list.
     */
    public int hashCode() {
        final FastComparator comp = this.getElementComparator();
        int h = 1;
        Node node = _head;
        for (int i = _size; i-- != 0;) {
            node = node._next;
            Object element = node._element;
            h = 31 * h + comp.hashCodeOf(element);
        }
        return h;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *        &lt; 0 || index &gt;= size()).
     */
    public Object get(int index) {
        if ((index >= 0) && (index < _size)) {
            return nodeAt(index)._element;
        } else {
            throw new IndexOutOfBoundsException("index: " + index
                    + " for list of size: " + _size);
        }
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range
     *        (index &lt; 0 || index &gt;= size()).
     */
    public Object set(int index, Object element) {
        if ((index >= 0) && (index < _size)) {
            Node node = nodeAt(index);
            Object previousElement = node._element;
            node._element = element;
            return previousElement;
        } else {
            throw new IndexOutOfBoundsException("index: " + index
                    + " for list of size: " + _size);
        }
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws IndexOutOfBoundsException if the index is out of range
     *        (index &lt; 0 || index &gt; size()).
     */
    public void add(int index, Object element) {
        if ((index >= 0) && (index <= _size)) {
            addBefore(nodeAt(index), element);
        } else {
            throw new IndexOutOfBoundsException("index: " + index
                    + " for list of size: " + _size);
        }
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right 
     * (increases their indices). 
     *
     * @param index index at which to insert first element from the specified
     *        collection.
     * @param c elements to be inserted into this list.
     * @return <code>true</code> if this list changed as a result of the call;
     *         <code>false</code> otherwise.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *        &lt; 0 || index &gt; size()).
     */
    public boolean addAll(int index, Collection c) {
        List subList = this.subList(0, index);
        boolean modified = false;
        Iterator i = c.iterator();
        while (i.hasNext()) {
            subList.add(i.next());
            modified = true;
        }
        return modified;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one
     * from their indices). Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to removed.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *            &lt; 0 || index &gt;= size()).
     */
    public Object remove(int index) {
        if ((index >= 0) && (index < _size)) {
            Node node = nodeAt(index);
            Object previousElement = node._element;
            removeNode(node);
            return previousElement;
        } else {
            throw new IndexOutOfBoundsException("index: " + index
                    + " for list of size: " + _size);
        }
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * @param element the element to search for.
     * @return the index in this list of the first occurrence of the specified
     *         element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object element) {
        final FastComparator comp = this.getElementComparator();
        Node node = _head;
        for (int i = _size; i-- != 0;) {
            node = node._next;
            if (comp.areEqual(element, node._element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * @param element the element to search for.
     * @return the index in this list of the last occurrence of the specified
     *         element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object element) {
        final FastComparator comp = this.getElementComparator();
        Node node = _tail;
        for (int i = _size; i-- != 0;) {
            node = node._previous;
            if (comp.areEqual(element, node._element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a list iterator over the elements in this list in proper 
     * sequence (allocated from the "stack" when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a list iterator of the elements in this list (in proper
     *         sequence).
     */
    public ListIterator listIterator() {
        FastListIterator i = (FastListIterator) FastListIterator.FACTORY
                .object();
        i._list = this;
        i._nextNode = _head._next;
        i._currentNode = null;
        i._nextIndex = 0;
        i._length = this._size;
        return i;
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list
     * (allocated from the "stack" when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     * The specified index indicates the first element that would be returned by
     * an initial call to the <code>next</code> method.  An initial call to
     * the <code>previous</code> method would return the element with the
     * specified index minus one.
     *
     * @param index index of first element to be returned from the
     *        list iterator (by a call to the <code>next</code> method).
     * @return a list iterator of the elements in this list
     *         starting at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *         &lt; 0 || index &gt; size()).
     */
    public ListIterator listIterator(int index) {
        if ((index >= 0) && (index < _size)) {
            FastListIterator i = (FastListIterator) listIterator();
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
     * removes a range of elements from a list:
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
    public List subList(int fromIndex, int toIndex) {
        if ((fromIndex >= 0) && (toIndex <= _size) && (fromIndex <= toIndex)) {
            FastList subList = (FastList) SUBLIST_FACTORY.object();
            subList._parent = this;
            subList._head = nodeAt(fromIndex)._previous;
            subList._tail = nodeAt(toIndex);
            subList._size = toIndex - fromIndex;
            return subList;
        } else {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex
                    + ", toIndex: " + toIndex + " for list of size: " + _size);
        }
    }

    /**
     * Returns the first element of this list.
     *
     * @return this list's first element.
     * @throws NoSuchElementException if this list is empty.
     */
    public Object getFirst() {
        if (_size != 0) {
            return _head._next._element;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the last element of this list.
     *
     * @return this list's last element.
     * @throws NoSuchElementException if this list is empty.
     */
    public Object getLast() {
        if (_size != 0) {
            return _tail._previous._element;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Inserts the specified element at the beginning of this list.
     * 
     * @param element the element to be inserted.
     */
    public void addFirst(Object element) {
        addBefore(_head._next, element);
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param element the element to be inserted.
     */
    public void addLast(Object element) {
        if (_parent == null) { // Main list.
            final Node newTail = _tail._next;
            if (newTail != null) { // Enough capacity. 
                _size++;
                _tail._element = element;
                _tail = newTail;
            } else {
                addBefore(_tail, element);
            }
        } else {
            _size++;
            _parent.addBefore(_tail, element);
        }
    }

    /**
     * Removes and returns the first element of this list.
     *
     * @return this list's first element before this call.
     * @throws NoSuchElementException if this list is empty.
     */
    public Object removeFirst() {
        if (_size != 0) {
            Object previousElement = _head._next._element;
            removeNode(_head._next);
            return previousElement;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Removes and returns the last element of this list.
     *
     * @return this list's last element before this call.
     * @throws NoSuchElementException if this list is empty.
     */
    public Object removeLast() {
        if (_size != 0) {
            _size--;
            _tail = _tail._previous;
            Object previousElement = _tail._element;
            _tail._element = null;
            return previousElement;
        } else {
            throw new NoSuchElementException();
        }
    }

    ///////////////////////
    // Contract Methods. //
    ///////////////////////

    // Implements abstract method.    
    public final Iterator fastIterator() {
        _fastIterator._list = this;
        _fastIterator._nextNode = _head._next;
        _fastIterator._currentNode = null;
        _fastIterator._nextIndex = 0;
        _fastIterator._length = this._size;
        return _fastIterator;
    }

    private transient FastListIterator _fastIterator;

    // Implements abstract method.
    public int size() {
        return _size;
    }

    // Implements abstract method.
    public Iterator iterator() {
        return listIterator();
    }

    // Overrides (optimization).
    public void clear() {
        Node node = _head;
        for (int i = _size; i-- != 0;) {
            node = node._next;
            node._element = null;
        }
        _tail = _head._next;
        _size = 0;
    }

    // Overrides (optimization).
    public boolean contains(Object element) {
        return indexOf(element) >= 0;
    }

    // Overrides (optimization).
    public boolean remove(Object element) {
        final FastComparator comp = this.getElementComparator();
        Node node = _head;
        for (int i = _size; i-- != 0;) {
            node = node._next;
            if (comp.areEqual(element, node._element)) {
                removeNode(node);
                return true;
            }
        }
        return false;
    }

    // Overrides (optimization).
    public boolean addAll(Collection c) {
        if (c instanceof FastList) {
            FastList list = (FastList) c;
            Node node = list._head;
            for (int i = list._size; i-- != 0;) {
                node = node._next;
                add(node._element);
            }
            return list._size != 0 ? true : false;
        } else {
            return super.addAll(c);
        }
    }

    // Implements Reusable.
    public void reset() {
        this.clear();
        this.setElementComparator(FastComparator.DEFAULT);
    }

    // Overrides (external references which might not be on the heap).
    public boolean move(ObjectSpace os) {
        if (super.move(os)) {
            if (_parent == null) { // Main List
                Node node = _head;
                for (int i = _size; i-- != 0;) {
                    node = node._next;
                    if (node._element instanceof Realtime) {
                        ((Realtime) node._element).move(os);
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
        stream.defaultReadObject();
        _head = new Node();
        _tail = new Node();
        _head._next = _tail;
        _tail._previous = _head;
        _fastIterator = new FastListIterator();
        final int size = stream.readInt();
        for (int i = size; i-- != 0;) {
            addLast(stream.readObject());
        }
    }

    // Requires special handling during serialization process.
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(_size);
        Node node = _head;
        for (int i = _size; i-- != 0;) {
            node = node._next;
            stream.writeObject(node._element);
        }
    }

    ////////////////////////
    // Utilities Methods. //
    ////////////////////////

    /**
     * Returns the node at the specified index.
     * 
     * @param index the index in the range [-1 .. size()]
     */
    private Node nodeAt(int index) {
        if (index <= (_size >> 1)) { // Forward search.
            Node node = _head;
            for (int i = index; i-- >= 0;) {
                node = node._next;
            }
            return node;
        } else { // Backward search.
            Node node = _tail;
            for (int i = _size - index; i-- > 0;) {
                node = node._previous;
            }
            return node;
        }
    }

    /**
     * Inserts the specified element before the specified node.
     * 
     * @param next the node before which this element is inserted.
     * @param element the element to insert.   
     */
    private void addBefore(Node next, Object element) {
        _size++;
        Node newNode;
        final Node newTail = _tail._next;
        if (newTail != null) { // Detaches tail.
            Node previous = newTail._previous = _tail._previous;
            previous._next = newTail;
            newNode = _tail;
            _tail = newTail;
        } else { // No node after tail allocates one from heap.
            newNode = (Node) Node.FACTORY.heapPool().next();
        }
        final Node previous = next._previous;
        newNode._element = element;
        newNode._previous = previous;
        newNode._next = next;
        next._previous = newNode;
        previous._next = newNode;
    }

    /**
     * Removes the specified node from this list (and inserts it before tail).
     * 
     * @param the node to remove.
     */
    private void removeNode(Node node) {
        _size--;

        // Detaches.
        node._next._previous = node._previous;
        node._previous._next = node._next;

        // Attaches before tail.
        node._previous = _tail._previous;
        node._next = _tail;
        node._previous._next = node;
        _tail._previous = node; // node._next._previous = node;

        _tail = node;
    }

    /**
     * This inner class represents a list node.
     */
    private static final class Node {

        private static final ObjectFactory FACTORY = new ObjectFactory() {
            public Object create() {
                return new Node();
            }
        };

        private Node _previous;

        private Node _next;

        private Object _element;

    }

    /**
     * This inner class implements a fast list iterator.
     */
    private static final class FastListIterator implements ListIterator {

        private static final ObjectFactory FACTORY = new ObjectFactory() {
            public Object create() {
                return new FastListIterator();
            }
        };

        private FastList _list;

        private Node _nextNode;

        private Node _currentNode;

        private int _length;

        private int _nextIndex;

        public boolean hasNext() {
            return (_nextIndex != _length);
        }

        public Object next() {
            if (_nextIndex++ != _length) {
                _currentNode = _nextNode;
                _nextNode = _nextNode._next;
                return _currentNode._element;
            } else {
                _nextIndex--;
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return _nextIndex;
        }

        public boolean hasPrevious() {
            return _nextIndex != 0;
        }

        public Object previous() {
            if (_nextIndex-- != 0) {
                _currentNode = _nextNode._previous;
                _nextNode = _currentNode;
                return _currentNode._element;
            } else {
                _nextIndex++;
                throw new NoSuchElementException();
            }
        }

        public int previousIndex() {
            return _nextIndex - 1;
        }

        public void add(Object o) {
            _list.addBefore(_nextNode, o);
            _length++;
            _currentNode = null;
            _nextIndex++;
        }

        public void set(Object o) {
            if (_currentNode != null) {
                _currentNode._element = o;
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
                _list.removeNode(_currentNode);
                _currentNode = null;
                _length--;
            } else {
                throw new IllegalStateException();
            }
        }
    }
}