/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javolution.realtime.ObjectPool;
import javolution.realtime.Realtime;
import javolution.realtime.RealtimeObject;
import javolution.xml.XmlElement;
import javolution.xml.XmlFormat;

/**
 * <p> This class represents a linked-list with real-time behavior 
 *     (nodes allocated from the stack and recycled to the stack when executing
 *     in a {@link javolution.realtime.PoolContext PoolContext}).</p>
 * 
 * <p> All of the operations perform as could be expected for a doubly-linked
 *     list. Operations that index into the list will traverse the list from
 *     the begining or the end whichever is closer to the specified index. 
 *     Random access operations can be significantly accelerated by 
 *     {@link #subList splitting} the list into smaller ones.</p> 
 * 
 * <p> Instances of this class can be used to implement dynamically sized
 *     real-time objects (no expensive resizing operations) or  
 *     for throw-away collections {@link #newInstance allocated} from
 *     the stack. Iterators upon instances of this class are real-time
 *     compliant as well.</p>
 * 
 * <p> Note: {@link FastList} allocates/recycles its nodes from/to the pool 
 *     context the list belongs to (e.g. the heap if the list belongs to the 
 *     heap context, the current pool for local lists).</p>
 * 
 * <p> This implementation is not synchronized. Multiple threads accessing
 *     or modifying the collection must be synchronized externally.</p>
 * 
 * @author <a href="mailto:artem@bizlink.ru">Artem Aleksandrovich Kozarezov</a>
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, October 4, 2004
 */
public class FastList extends FastCollection implements List, Serializable {

    /**
     * Overrides {@link XmlFormat#COLLECTION_XML} format in order to use 
     * the {@link #newInstance} factory method instead of the default 
     * constructor during the deserialization of {@link FastList} instances.
     */
    protected static final XmlFormat FAST_LIST_XML = new XmlFormat(
            FastList.class) {
        
        public void format(Object obj, XmlElement xml) {
            xml.addAll((FastList) obj);
        }

        public Object parse(XmlElement xml) {
            FastList fl = (xml.objectClass() == FastList.class) ?
                    FastList.newInstance() : (FastList) xml.object();
            fl.addAll(xml);
            return fl;
        }
    };

    /**
     * Holds the nodes' pool for this list (or <code>null</code> if 
     * list allocated on the heap).
     */
    private transient ObjectPool _nodes;

    /**
     * Holds the parent list (for sub-lists).
     */
    private FastList _parent;

    /**
     * Holds the node marking the beginning of the list (not included).
     */
    private Node _start;

    /**
     * Holds the node marking the end of the list (not included).
     */
    private Node _end;

    /**
     * Holds the current size.
     */
    private int _size;

    /**
     * Creates a {@link FastList} on the heap. All internal nodes will be  
     * allocated on the heap as well.
     */
    public FastList() {
        _start = new Node();
        _end = new Node();
        _start._next = _end;
        _end._previous = _start;
    }

    /**
     * Constructor for sub-lists.
     * 
     * @param start the beginning of the sub-list marker.
     * @param end the end of the sub-list marker.
     */
    private FastList(Node start, Node end) {
        _start = start;
        _end = end;
    }

    /**
     * Returns a {@link FastList} allocated from the stack when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}). All internal nodes 
     * will be allocated from the same context as this list.
     *
     * @return a new or recycled list instance.
     */
    public static FastList newInstance() {
        FastList fastList = (FastList) LIST_FACTORY.object();
        ObjectPool nodePool = NODE_FACTORY.currentPool();
        fastList._nodes = (nodePool == NODE_FACTORY.heap()) ? null : nodePool;
        return fastList;
    }

    // Implements abstract method.    
    public final Iterator fastIterator() {
        _fastIterator._list = this;
        _fastIterator._nextNode = _start._next;
        _fastIterator._currentNode = null;
        _fastIterator._nextIndex = 0;
        _fastIterator._length = this._size;
        return _fastIterator;
    }
    private final FastListIterator _fastIterator = new FastListIterator();

    // Implements abstract method.
    public int size() {
        return _size;
    }

    // Implements abstract method.
    public Iterator iterator() {
        return listIterator();
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
        addBefore(_end, element);
        return true;
    }

    // Overrides (optimization).
    public void clear() {
        while (!isEmpty()) {
            removeLast();
        }
    }

    /**
     * Compares the specified object with this list for equality.  Returns
     * <code>true</code> if and only if both lists contain the same elements
     * in the same order.
     *
     * @param o the object to be compared for equality with this list.
     * @return <code>true</code> if the specified object is equal to this list;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof List)) {
            return false;
        } else {
            FastListIterator i1 = (FastListIterator) iterator();
            Iterator i2 = ((List) o).iterator();
            while (i1.hasNext() && i2.hasNext()) {
                final Object o1 = i1.next();
                final Object o2 = i2.next();
                if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                    return false;
                }
            }
            return !(i1.hasNext() || i2.hasNext());
        }
    }

    /**
     * Returns the hash code value for this list.  The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  hashCode = 1;
     *  Iterator i = list.iterator();
     *  while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     *  }
     * </pre>
     *
     * @return the hash code value for this list.
     */
    public int hashCode() {
        int h = 1;
        Node node = _start._next;
        while (node != _end) {
            Object element = node._element;
            h = 31 * h + (element == null ? 0 : element.hashCode());
            node = node._next;
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
        Node node = _start._next;
        if (element != null) {
            for (int i = 0; i < _size; i++) {
                if (element.equals(node._element)) {
                    return i;
                }
                node = node._next;
            }
        } else { // Searches for null
            for (int i = 0; i < _size; i++) {
                if (node._element == null) {
                    return i;
                }
                node = node._next;
            }
        } // Not found.
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
        Node node = _end._previous;
        if (element != null) {
            for (int i = _size - 1; i >= 0; i--) {
                if (element.equals(node._element)) {
                    return i;
                }
                node = node._previous;
            }
        } else { // Searches for null
            for (int i = _size - 1; i >= 0; i--) {
                if (node._element == null) {
                    return i;
                }
                node = node._previous;
            }
        } // Not found.
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
        FastListIterator i = (FastListIterator) ITERATOR_FACTORY.object();
        i._list = this;
        i._nextNode = _start._next;
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
            subList._start = nodeAt(fromIndex)._previous;
            subList._end = nodeAt(toIndex);
            subList._size = toIndex - fromIndex;
            // subList._nodes is never used, nodes allocated from parent list.
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
            return _start._next._element;
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
            return _end._previous._element;
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
        addBefore(_start._next, element);
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param element the element to be inserted.
     */
    public void addLast(Object element) {
        addBefore(_end, element);
    }

    /**
     * Removes and returns the first element of this list.
     *
     * @return this list's first element before this call.
     * @throws NoSuchElementException if this list is empty.
     */
    public Object removeFirst() {
        if (_size != 0) {
            Object previousElement = _start._next._element;
            removeNode(_start._next);
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
            Object previousElement = _end._previous._element;
            removeNode(_end._previous);
            return previousElement;
        } else {
            throw new NoSuchElementException();
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
        if (_parent == null) { // Main list.
            final Node node = newNode();
            final Node previous = next._previous;
            node._element = element;
            node._next = next;
            node._previous = previous;
            previous._next = node;
            next._previous = node;
        } else { // Sub-List.
            _parent.addBefore(next, element);
        }
    }

    private Node newNode() {
        if (_nodes != null) {
            if (_nodes.isLocal()) {
                return (Node) _nodes.next();
            } else {
                synchronized (_nodes) {
                    return (Node) _nodes.next();
                }
            }
        } else {
            return new Node();
        }
    }

    /**
     * Removes the specified node from this list.
     * 
     * @param the node to remove.
     */
    private void removeNode(Node node) {
        _size--;
        if (_parent == null) { // Main list.
            node._previous._next = node._next;
            node._next._previous = node._previous;
            node._element = null;
            recycleNode(node);
        } else { // Sub-List.
            _parent.removeNode(node);
        }
    }

    private void recycleNode(Node node) {
        if (_nodes != null) {
            if (_nodes.isLocal()) {
                _nodes.recycle(node);
            } else {
                synchronized (_nodes) {
                    _nodes.recycle(node);
                }
            }
        } // Else heap allocated, gc does the recycling.
    }

    /**
     * Returns the node at the specified index.
     * 
     * @param index the index in the range [-1 .. size()]
     */
    private Node nodeAt(int index) {
        if (index <= (_size >> 1)) { // Forward search.
            Node node = _start;
            for (int i = index; i-- >= 0; node = node._next) {
            }
            return node;
        } else { // Backward search.
            Node node = _end;
            for (int i = _size - index; i-- > 0; node = node._previous) {
            }
            return node;
        }
    }

    // Overrides.
    public void move(ContextSpace cs) {
        super.move(cs);
        if (_parent == null) { // Main List
            for (Node node = _start; node != _end; node = node._next) {
                node.move(cs);
            }
            _end.move(cs);
            if (cs == ContextSpace.OUTER) {
                _nodes = _nodes.getOuter();
            } else if (cs == ContextSpace.HEAP) {
                _nodes = null;
            }
        } else {
            _parent.move(cs);
        }
    }

    /**
     * This inner class implements a fast list node.
     */
    private static final class Node extends RealtimeObject implements
            Serializable {
        private Object _element;

        private Node _next;

        private Node _previous;

        // Overrides.
        public void move(ContextSpace cs) {
            super.move(cs);
            if (_element instanceof Realtime) {
                ((Realtime) _element).move(cs);
            }
        }

        private static final long serialVersionUID = -9081362293021515482L;
    }

    /**
     * This inner class implements a fast list iterator.
     */
    private static final class FastListIterator extends RealtimeObject
            implements ListIterator {

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

    ///////////////
    // FACTORIES //
    ///////////////

    private static final Factory LIST_FACTORY = new Factory() {

        public Object create() {
            return new FastList();
        }

        public void cleanup(Object obj) {
            // Clears external objects references.
            final FastList fastList = (FastList) obj;
            final Node start = fastList._start;
            final Node end = fastList._end;
            for (Node node = start; node != end;) {
                node = node._next;
                node._element = null;
            }
            // Resets to empty list.
            start._next = end;
            end._previous = start;
            fastList._size = 0;
        }
    };

    private static final Factory SUBLIST_FACTORY = new Factory() {

        public Object create() {
            return new FastList(null, null);
        }

        // No need to cleanup, 
        // external objects references cleared by parent list.
    };

    private static final Factory NODE_FACTORY = new Factory() {

        public Object create() {
            return new Node();
        }
        // No need to cleanup, 
        // external objects references cleared by the list itself.

    };

    private static final Factory ITERATOR_FACTORY = new Factory() {

        public Object create() {
            return new FastListIterator();
        }
        // No need to cleanup, 
        // external objects references cleared by the list itself.

    };

    private static final long serialVersionUID = 3978424741055181621L;
}