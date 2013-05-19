package javolution.util;

import java.io.Serializable;

import javolution.annotation.StackSafe;
import javolution.annotation.ThreadSafe;
import javolution.internal.util.comparator.StandardComparatorImpl;
import javolution.internal.util.comparator.IdentityComparatorImpl;
import javolution.internal.util.comparator.LexicalComparatorImpl;
import javolution.internal.util.comparator.StringComparatorImpl;
import javolution.util.service.ComparatorService;

/**
 * <p> Common comparators instances.</p>
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
    public static final Comparators<Object> IDENTITY 
        = new IdentityComparatorImpl<Object>();

    /**
     * Holds a lexicographic comparator for any {@link CharSequence}.
     * Hashcodes are calculated by taking a sample of few characters instead of 
     * the whole character sequence.
     */
    public static final Comparators<CharSequence> LEXICAL 
        = new LexicalComparatorImpl();
    
    /**
     * Holds an optimized comparator for <code>java.lang.String</code>
     * instances.
     */
    public static final Comparators<String> STRING 
        = new StringComparatorImpl();
        
    /**
     * Default constructor.
     */
    protected Comparators() {
    }

    private static final long serialVersionUID = -7225629198872713338L;
}