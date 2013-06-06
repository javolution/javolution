package javolution.util;

import javolution.annotation.StackSafe;
import javolution.annotation.ThreadSafe;
import javolution.internal.util.comparator.IdentityComparatorImpl;
import javolution.internal.util.comparator.LexicalCaseInsensitiveComparatorImpl;
import javolution.internal.util.comparator.LexicalComparatorImpl;
import javolution.internal.util.comparator.StandardComparatorImpl;
import javolution.internal.util.comparator.StringComparatorImpl;
import javolution.util.service.ComparatorService;

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
     * The standard object comparator.
     */
    public static final ComparatorService<Object> STANDARD 
        = new StandardComparatorImpl<Object>();

    /**
     * Holds an identity comparator for any object.
     */
    public static final ComparatorService<Object> IDENTITY 
        = new IdentityComparatorImpl<Object>();

    /**
     * Holds a lexicographic comparator for any {@link CharSequence}.
     * Hash codes are calculated by taking a sample of few characters instead of 
     * the whole character sequence.
     */
    public static final ComparatorService<CharSequence> LEXICAL 
        = new LexicalComparatorImpl();
    
    /**
     * Holds a case insensitive lexicographic comparator for any {@link CharSequence}.
     * Hash codes are calculated by taking a sample of few characters instead of 
     * the whole character sequence.
     */
    public static final ComparatorService<CharSequence> LEXICAL_CASE_INSENSITIVE 
        = new LexicalCaseInsensitiveComparatorImpl();
    
    /**
     * Holds an optimized comparator for <code>java.lang.String</code>
     * instances taking a sample of few characters instead of 
     * the whole character sequence to calculate the hash code.
     */
    public static final ComparatorService<String> STRING 
        = new StringComparatorImpl();
        
    /**
     * Utility class (private constructor).
     */
    private Comparators() {
    }
}