package javolution.util.function;

import javolution.lang.Parallelizable;
import javolution.lang.Realtime;
import javolution.util.internal.comparator.ArrayComparatorImpl;
import javolution.util.internal.comparator.IdentityComparatorImpl;
import javolution.util.internal.comparator.LexicalCaseInsensitiveComparatorImpl;
import javolution.util.internal.comparator.LexicalComparatorImpl;
import javolution.util.internal.comparator.LexicalFastComparatorImpl;
import javolution.util.internal.comparator.StandardComparatorImpl;

import static javolution.lang.Realtime.Limit.*;

/**
 * <p> A set of useful {@link EqualityComparator comparators} 
 *     for equality and ordering.</p>
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public class Comparators {

    /**
     * A standard object comparator (based on the object hashCode and equals 
     * methods).  Comparisons either use the object natural order (which 
     * should be consistent with equals) or an empirical method 
     * (if the object does not implement {@link Comparable}).
     * 
     */
    @Parallelizable
    @Realtime(limit = UNKNOWN)
    public static final EqualityComparator<Object> STANDARD = new StandardComparatorImpl<Object>();

    /**
     * A comparator for which instances are only equals to themselves.
     * For comparisons an empirical method consistent with equals ({@code == })
     * is used.
     */
    @Parallelizable
    @Realtime(limit = CONSTANT)
    public static final EqualityComparator<Object> IDENTITY = new IdentityComparatorImpl<Object>();

    /**
     * A content array comparator. If the content of an array is also 
     * an array (multi-dimensional arrays), that same comparator is used 
     * for equality and comparison (recursive). The {@link #STANDARD standard}
     * comparator is used for non-array elements. 
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final EqualityComparator<Object> ARRAY = new ArrayComparatorImpl();

    /**
     * A lexicographic comparator for any {@link CharSequence}.
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final EqualityComparator<CharSequence> LEXICAL = new LexicalComparatorImpl();

    /**
     * A case insensitive lexicographic comparator for any {@link CharSequence}.
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final EqualityComparator<CharSequence> LEXICAL_CASE_INSENSITIVE = new LexicalCaseInsensitiveComparatorImpl();

    /**
     * An optimized lexical comparator for any {@link CharSequence} taking 
     * a sample of few characters instead of the whole character sequence to 
     * calculate the hash code (still equality comparison checks all characters).
     */
    @Parallelizable
    @Realtime(limit = LINEAR)
    public static final EqualityComparator<CharSequence> LEXICAL_FAST = new LexicalFastComparatorImpl();

    /**
     * Utility class (private constructor).
     */
    private Comparators() {}
}