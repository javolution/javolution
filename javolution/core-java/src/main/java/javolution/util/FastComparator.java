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
 *     {@link FastComparator#IDENTITY} for identity maps, value retrieval 
 *     using keys of a different class that the map keys) or with 
 *     {@link FastCollection} classes.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe(initialization=false)
public abstract class FastComparator<T> implements ComparatorService<T>, Comparator<T>, Serializable {

    /**
     * Holds a standard comparator for any object.
     */
    public static final FastComparator<Object> STANDARD 
        = new DefaultComparatorImpl<Object>();

    /**
     * Holds an identity comparator for any object.
     */
    public static final FastComparator<Object> IDENTITY 
        = new IdentityComparatorImpl<Object>();

    /**
     * Holds a lexicographic comparator for any {@link CharSequence}.
     * Hashcodes are calculated by taking a sample of few characters instead of 
     * the whole character sequence.
     */
    public static final FastComparator<CharSequence> LEXICAL 
        = new LexicalComparatorImpl();
    
    /**
     * Holds an optimized comparator for <code>java.lang.String</code>
     * instances.
     */
    public static final FastComparator<String> STRING 
        = new StringComparatorImpl();
        
    /**
     * Default constructor.
     */
    protected FastComparator() {
    }

    private static final long serialVersionUID = -7225629198872713338L;
}