package javolution.util;

import java.io.Serializable;
import java.util.Comparator;

import javolution.annotation.StackSafe;
import javolution.internal.util.comparator.DefaultComparatorImpl;
import javolution.internal.util.comparator.IdentityComparatorImpl;
import javolution.internal.util.comparator.LexicalComparatorImpl;
import javolution.internal.util.comparator.StringComparatorImpl;
import javolution.util.service.ComparatorService;

/**
 * <p> A comparator to be used for equality as well as for ordering.
 *     Instances of this class provide a hashcode function 
 *     consistent with equal (if two objects {@link #areEqual
 *     are equal}, they have the same {@link #hashCodeOf hashcode}),
 *     equality with <code>null</code> values is supported.</p>
 *     
 * <p> {@link FastComparator} can be employed with {@link FastMap} (e.g. 
 *     custom key comparators for identity maps, value retrieval using keys
 *     of a different class that the map keys) or with {@link FastCollection}
 *     classes.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe
public class FastComparator<T> implements Comparator<T>, Serializable {

    /**
     * Holds the default object comparator.
     * Two instances o1 and o2 are considered {@link #areEqual equal} 
     * if and only if <code>(o1 == o2)</code>. 
     * The {@link #compare} method throws {@link ClassCastException} if the 
     * specified objects are not {@link Comparable}.
     */
    public static final FastComparator<Object> DEFAULT 
        = new FastComparator<Object>(new DefaultComparatorImpl<Object>());

    /**
     * Holds the identity object comparator.
     * Two instances o1 and o2 are considered {@link #areEqual equal} 
     * if and only if <code>(o1 == o2)</code>. 
     * The {@link #compare} method throws {@link ClassCastException} if the 
     * specified objects are not {@link Comparable}.
     */
    public static final FastComparator<Object> IDENTITY 
        = new FastComparator<Object>(new IdentityComparatorImpl<Object>());

    /**
     * Holds a lexicographic comparator for any {@link CharSequence}.
     * Hashcodes are calculated by taking a sample of few characters instead of 
     * the whole character sequence.
     */
    public static final FastComparator<CharSequence> LEXICAL 
        = new FastComparator<CharSequence>(new LexicalComparatorImpl());
    
    /**
     * Holds an optimized comparator for <code>java.lang.String</code>
     * instances.
     */
    public static final FastComparator<String> STRING 
        = new FastComparator<String>(new StringComparatorImpl());
        
    /**
     * Holds the service implementation.
     */
    private final ComparatorService<T> service;
        
    /**
     * Creates a comparator implemented by the specified service.
     */
    public FastComparator(ComparatorService<T> service) {
        this.service = service;
    }
       
    /**
     * Returns the default comparator for instances of the specified class.
     * Two instances o1 and o2 are considered {@link #areEqual equal} if and
     * only if <code>o1.equals(o2)</code>. The {@link #compare} method 
     * throws {@link ClassCastException} if the specified objects are not
     * {@link Comparable}. 
     */
    public static <T> FastComparator<T> defaultFor(Class<T> type) {
        return new FastComparator<T>(new DefaultComparatorImpl<T>());
    }
       

    /**
     * Returns the identity comparator for instances of the specified class.
     * Two instances o1 and o2 are considered {@link #areEqual equal} if and
     * only if <code>o1.equals(o2)</code>. The {@link #compare} method 
     * throws {@link ClassCastException} if the specified objects are not
     * {@link Comparable}. 
     */
    public static <T> FastComparator<T> identityFor(Class<T> type) {
        return new FastComparator<T>(new IdentityComparatorImpl<T>());
    }

    /**
     * Returns the comparator service implementation.
     */
    public ComparatorService<T> getService() {
        return service;
    }
       
    /**
     * See {@link ComparatorService#hashCodeOf(Object)}.
     */
    public int hashCodeOf(T obj) {
        return service.hashCodeOf(obj);
    }

    /**
     * See {@link ComparatorService#areEqual(Object, Object)}.
     */
    public boolean areEqual(T o1, T o2) {
        return service.areEqual(o1, o2);
    }

    /**
     * See {@link ComparatorService#compare(Object, Object)}.
     */
    public int compare(T o1, T o2) {
        return service.compare(o1, o2);
    }
    
    private static final long serialVersionUID = 953664997000330872L;    
}