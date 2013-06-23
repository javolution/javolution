package javolution.util.function;

import javolution.annotation.RealTime;
import javolution.annotation.Parallelizable;
import javolution.internal.util.comparator.ArrayComparatorImpl;
import javolution.internal.util.comparator.IdentityComparatorImpl;
import javolution.internal.util.comparator.LexicalCaseInsensitiveComparatorImpl;
import javolution.internal.util.comparator.LexicalComparatorImpl;
import javolution.internal.util.comparator.LexicalFastComparatorImpl;
import javolution.internal.util.comparator.StandardComparatorImpl;

import static javolution.annotation.RealTime.Limit.*;

/**
 * <p> A set of useful {@link EqualityComparator comparators} 
 *     for equality and ordering.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
@Parallelizable
@RealTime(limit = UNKNOWN)
public class Comparators {

    /**
     * A standard object comparator (based on the object hashCode and equals 
     * methods).  Comparisons either use the object natural order (which 
     * should be consistent with equals) or an empirical method 
     * (if the object does not implement {@link Comparable}).
     * 
     */
    public static final EqualityComparator<Object> STANDARD = new StandardComparatorImpl<Object>();

    /**
     * A comparator for which instances are only equals to themselves.
     * For comparisons an empirical method consistent with equals ({@code == })
     * is used.
     */
    @RealTime(limit = CONSTANT)
    public static final EqualityComparator<Object> IDENTITY = new IdentityComparatorImpl<Object>();

    /**
     * A content array comparator. If the content of an array is also 
     * an array (multi-dimensional arrays), that same comparator is used 
     * for equality and comparison (recursive). The {@link #STANDARD standard}
     * comparator is used for non-array elements. 
     */
    @RealTime(limit = LINEAR)
    public static final EqualityComparator<Object> ARRAY = new ArrayComparatorImpl();

    /**
     * A lexicographic comparator for any {@link CharSequence}.
     */
    @RealTime(limit = LINEAR)
    public static final EqualityComparator<CharSequence> LEXICAL = new LexicalComparatorImpl();

    /**
     * A case insensitive lexicographic comparator for any {@link CharSequence}.
     */
    @RealTime(limit = LINEAR)
    public static final EqualityComparator<CharSequence> LEXICAL_CASE_INSENSITIVE = new LexicalCaseInsensitiveComparatorImpl();

    /**
     * An optimized lexical comparator for any {@link CharSequence} taking 
     * a sample of few characters instead of the whole character sequence to 
     * calculate the hash code (still equality comparison checks all characters).
     */
    @RealTime(limit = LINEAR)
    public static final EqualityComparator<CharSequence> LEXICAL_FAST = new LexicalFastComparatorImpl();

    /**
     * Utility class (private constructor).
     */
    private Comparators() {}
}