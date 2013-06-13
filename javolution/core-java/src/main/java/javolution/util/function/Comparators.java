package javolution.util.function;

import javolution.annotation.RealTime;
import javolution.annotation.StackSafe;
import javolution.annotation.ThreadSafe;
import javolution.annotation.RealTime.Limit;
import javolution.internal.util.comparator.ArrayComparatorImpl;
import javolution.internal.util.comparator.IdentityComparatorImpl;
import javolution.internal.util.comparator.LexicalCaseInsensitiveComparatorImpl;
import javolution.internal.util.comparator.LexicalComparatorImpl;
import javolution.internal.util.comparator.LexicalFastComparatorImpl;
import javolution.internal.util.comparator.StandardComparatorImpl;

/**
 * <p> A collection of {@link StackSafe stack-safe} and 
 *     {@link ThreadSafe thread-safe} (stateless) comparators.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@StackSafe
@ThreadSafe
public class Comparators  {

    /**
     * A standard object comparator (based on the object hashCode and equals 
     * methods).  Comparisons either use the object natural order or an 
     * empirical method (if the object does not implement {@link Comparable}).
     */
    @RealTime(Limit.UNKNOWN)
    public static final FullComparator<Object> STANDARD 
        = new StandardComparatorImpl<Object>();

    /**
     * A standard comparator for which instances are only equals to themselves.
     */
    @RealTime(Limit.CONSTANT)
    public static final FullComparator<Object> IDENTITY 
        = new IdentityComparatorImpl<Object>();

    /**
     * A standard content array comparator. If the content of an array is also 
     * an array (multi-dimensional arrays), that same comparator is used 
     * for equality and comparison (recursive).
     */
    @RealTime(Limit.LINEAR)
    public static final FullComparator<Object> ARRAY 
        = new ArrayComparatorImpl();

    /**
     * A lexicographic comparator for any {@link CharSequence}.
     */
    @RealTime(Limit.LINEAR)
    public static final FullComparator<CharSequence> LEXICAL 
        = new LexicalComparatorImpl();
    
    /**
     * A case insensitive lexicographic comparator for any {@link CharSequence}.
     */
    @RealTime(Limit.LINEAR)
    public static final FullComparator<CharSequence> LEXICAL_CASE_INSENSITIVE 
        = new LexicalCaseInsensitiveComparatorImpl();
    
    /**
     * An optimized lexical comparator for any {@link CharSequence} taking 
     * a sample of few characters instead of the whole character sequence to 
     * calculate the hash code (still equality comparison checks all characters).
     */
    @RealTime(Limit.LINEAR)
    public static final FullComparator<CharSequence> LEXICAL_FAST 
        = new LexicalFastComparatorImpl();
        
    /**
     * Utility class (private constructor).
     */
    private Comparators() {
    }
}