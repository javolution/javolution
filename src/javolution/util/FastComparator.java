package javolution.util;

import javolution.Configuration;
import javolution.lang.Text;
import j2me.io.Serializable;
import j2me.lang.CharSequence;
import j2me.lang.Comparable;
import j2me.util.Comparator;

/**
 * <p> This class represents a comparator to be used for equality as well as 
 *     for ordering; instances of this class provide an hashcode function 
 *     consistent with equal (if two objects {@link #areEqual
 *     are equal}, they have the same {@link #hashCodeOf hashcode}),
 *     equality with <code>null</code> value is supported.</p>
 *     
 * <p> {@link FastComparator} can be employed with {@link FastMap} (custom 
 *     key comparators for identity maps, value retrieval using keys of 
 *     different class that the map's keys, etc) or with {@link FastCollection}
 *     classes.</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.0, February 16, 2004
 */
public abstract class FastComparator implements Comparator, Serializable {

    /**
     * Holds the default object comparator; no rehash is performed.
     * Two objects o1 and o2 are considered {@link #areEqual equal} if and
     * only if <code>o1.equals(o2)</code>. The {@link #compare} method 
     * throws {@link ClassCastException} if the specified objects are not
     * {@link Comparable}. 
     */
    public static final FastComparator DEFAULT = new Default();

    private static class Default extends FastComparator {
        public int hashCodeOf(Object obj) {
            return (obj == null) ? 0 : obj.hashCode();
        }

        public boolean areEqual(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : o1.equals(o2);
        }

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public String toString() {
            return "Default";
        }
    };

    /**
     * Holds the comparator for objects with uneven hash distribution; objects
     * hashcodes are rehashed. Two objects o1 and o2 are considered 
     * {@link #areEqual equal} if and only if <code>o1.equals(o2)</code>.
     * The {@link #compare} method throws {@link ClassCastException} if the
     * specified objects are not {@link Comparable}.
     */
    public static final FastComparator REHASH = new Rehash();

    private static class Rehash extends FastComparator {
        public int hashCodeOf(Object obj) {
            if (obj == null) {
                return 0; 
            }
            // Formula identical <code>java.util.HashMap</code> to ensures
            // similar behavior for ill-conditioned hashcode keys. 
            int h = obj.hashCode();
            h += ~(h << 9);
            h ^= (h >>> 14);
            h += (h << 4);
            return h ^ (h >>> 10);
        }

        public boolean areEqual(Object o1, Object o2) {
            return (o1 == null) ? (o2 == null) : o1.equals(o2);
        }

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public String toString() {
            return "Rehash";
        }
    };

    /**
     * Holds the identity comparator; poorly distributed system hashcodes are
     * rehashed. Two objects o1 and o2 are considered {@link #areEqual equal} 
     * if and only if <code>(o1 == o2)</code>. The {@link #compare} method 
     * throws {@link ClassCastException} if the specified objects are not
     * {@link Comparable}.
     */
    public static final FastComparator IDENTITY = new Identity();

    private static class Identity extends FastComparator {
        private boolean _rehash = !Configuration.isPoorSystemHash();

        public int hashCodeOf(Object obj) {
            int h = System.identityHashCode(obj);
            if (_rehash) {
                h += ~(h << 9);
                h ^= (h >>> 14);
                h += (h << 4);
                return h ^ (h >>> 10);
            }
            return h;
        }

        public boolean areEqual(Object o1, Object o2) {
            return o1 == o2;
        }

        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }

        public String toString() {
            return "Identity";
        }
    };

    /**
     * Holds a lexicographic comparator for any {@link CharSequence} or 
     * {@link String} instances. 
     * Two objects are considered {@link #areEqual equal} if and only if they 
     * represents the same character sequence). The hashcode is calculated
     * using the following formula (same as for <code>java.lang.String</code>):
     * <code>s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]</code>
     */
    public static final FastComparator LEXICAL = new Lexical();

    private static class Lexical extends FastComparator {
        
        public int hashCodeOf(Object obj) {
            if ((obj instanceof String) || (obj instanceof Text)) {
                return obj.hashCode();
            } else if (obj instanceof CharSequence){
                CharSequence chars = (CharSequence) obj;
                int h = 0;
                final int length = chars.length();
                for (int i = 0; i < length;) {
                    h = 31 * h + chars.charAt(i++);
                }
                return h;
            } else { // null value.
                return 0;
            }
        }

        public boolean areEqual(Object o1, Object o2) {
            if ((o1 instanceof String) && (o2 instanceof String)) {
                return o1.equals(o2);

            } else if ((o1 instanceof CharSequence) && (o2 instanceof String)) {
                final CharSequence csq = (CharSequence) o1;
                final String str = (String) o2;
                final int length = str.length();
                if (csq.length() == length) {
                    for (int i=0; i < length;) {
                        if (str.charAt(i) != csq.charAt(i++)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
                
                
            } else if ((o1 instanceof String) && (o2 instanceof CharSequence)) {
                final CharSequence csq = (CharSequence) o2;
                final String str = (String) o1;
                final int length = str.length();
                if (csq.length() == length) {
                    for (int i=0; i < length;) {
                        if (str.charAt(i) != csq.charAt(i++)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
                

            } else if ((o1 instanceof CharSequence) && (o2 instanceof CharSequence)) {
                final CharSequence csq1 = (CharSequence)o1;
                final CharSequence csq2 = (CharSequence)o2;
                final int length = csq1.length();
                if (csq2.length() == length) {
                    for (int i=0; i < length;) {
                        if (csq1.charAt(i) != csq2.charAt(i++)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
                
            } else {  
                return (o1 == null) ? (o2 == null) : o1.equals(o2);
            }
        }
        
        public int compare(Object left, Object right) {
            if (left instanceof String) {
                if (right instanceof String) {
                    return ((String) left).compareTo((String) right);
                } else { // Right must be a CharSequence.
                    String seq1 = (String) left;
                    CharSequence seq2 = (CharSequence) right;
                    int i = 0;
                    int n = Math.min(seq1.length(), seq2.length());
                    while (n-- != 0) {
                        char c1 = seq1.charAt(i);
                        char c2 = seq2.charAt(i++);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                    return seq1.length() - seq2.length();
                }
            } else {
                if (right instanceof String) {
                    return -compare(right, left);
                } else { // Both are CharSequence.
                    CharSequence seq1 = (CharSequence) left;
                    CharSequence seq2 = (CharSequence) right;
                    int i = 0;
                    int n = Math.min(seq1.length(), seq2.length());
                    while (n-- != 0) {
                        char c1 = seq1.charAt(i);
                        char c2 = seq2.charAt(i++);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                    return seq1.length() - seq2.length();
                }
            }
        }

        public String toString() {
            return "Lexical";
        }
    };

    /**
     * Returns the hash code for the specified object (consistent with 
     * {@link #areEqual}). Two objects considered {@link #areEqual equal} have 
     * the same hash code. 
     * 
     * @param  obj the object to return the hashcode for (or <code>null</code>).
     * @return the hashcode for the specified object.
     */
    public abstract int hashCodeOf(Object obj);

    /**
     * Indicates if the specified objects can be considered equal.
     * 
     * @param o1 the first object (or <code>null</code>).
     * @param o2 the second object (or <code>null</code>).
     * @return <code>true</code> if both objects are considered equal;
     *         <code>false</code> otherwise. 
     */
    public abstract boolean areEqual(Object o1, Object o2);

    /**
     * Compares the specified objects for order. Returns a negative integer, 
     * zero, or a positive integer as the first argument is less than, equal to,
     * or greater than the second.
     * 
     * @param o1 the first object.
     * @param o2 the second object.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    public abstract int compare(Object o1, Object o2);

}