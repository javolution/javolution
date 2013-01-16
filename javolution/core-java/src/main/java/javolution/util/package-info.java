/**
<p> High-performance collection classes based on OSGi principles (pluggable 
    and run-time adaptable implementations for optimum performance).</p>
    * 
<p> Whereas Java current evolution leads to more and more classes being parts of 
    the standard library; Javolution approach is quite the opposite. It aims to
    provide only the quintessential classes from which all others can be derived.
    Here are few examples of fast collection usages:</p>
[code]
// FastTable examples (all views can be chained).
FastTable<Session> sessions = new FastTable().shared(); // Table which can be concurrently accessed/modified.
FastTable<Item> items = new FastTable().sorted().noDuplicate(); // Table of sorted items with no duplicate (see {@link FastSortedSet}).
     // Sorted tables have faster {@link javolution.util.FastTable#indexOf indexOf}, {@link javolution.util.FastTable#contains contains} and {@link javolution.util.FastTable#remove(java.lang.Object) remove} methods. 
FastTable<CharSequence> names ...
names.unmodifiable(); // Unmodifiable view.
names.usingComparator(FastComparator.LEXICAL).reverse().sort(); // Sorts the names in reverse alphabetical order.
names.shared().subList(0, mames.size() / 2); // Provides a concurrently modifiable view of the first half of the table.
names.shared().subList(mames.size() / 2, names.size()); // Provides a concurrently modifiable view of the second half of the table.
[/code]</p>

<h2><a name="FAQ">FAQ:</a></h2>
<ol>
    <li><b>ArrayList may throw ConcurrentModificationException,
           but <b>J</b>avolution FastTable does not, why?</b>
    <p> FastTable (or any <b>J</b>avolution collection/map) <b>do support concurrent modifications</b>
        as long as the collections/maps is {@link javolution.util.FastCollection FastCollection#shared shared}.
        You can iterates using closure even if new elements/entries are added/deleted.</p>
    
    <p><i> Disallowing concurrent modifications (standard java util) has proven to be a performance 
    killer for many (forcing users to work with copies of their whole collections). Furthermore the additional checks required
    directly impact performance.</i></p>
    </li>
    
    <li><b>Do you have a test case showing any scenario of concurrent modification where 
          ArrayList "fails" and FastTable doesn't?</b>
    <p> Let's say that you have a collection of "Units", and you want to provide users 
        with a read-only view of these units. The following code will fail miserably:
[code]
public class Unit {
    static ArrayList<Unit> INSTANCES = new ArrayList<unit>();
    public static Collection<Unit> getInstances() {
        return Collections.unmodifiableCollection(INSTANCES);
    }
}[/code]
        Why? Because, it the user iterates on the read-only list of units while a new unit is added
        to the collection (by another thread) a <code>ConcurrentModificationException</code> is 
        automatically raised. In other words, it is almost impossible to provide a "read-only" view
        of non-fixed size collections with the current java.util classes (e.g. you will have to replace
        the whole collection each time a new unit is added).</p>
    <p> Now with {@link javolution.util.FastTable FastTable} the following is completely safe even when new units are added:
[code]
public class Unit {
    static final FastTable<Unit> INSTANCES = new FastTable<Unit>().shared();
    public static FastTable<Unit> getInstances() { // Const. view.
        return INSTANCES.unmodifiable();
    }
}[/code]</p>
    </li>
        
    <li><b>Do checks for concurrent modifications make your code safer?</b>
    <p> Not really. The current checks for concurrent modifications do not "guarantee" that concurrent 
    modifications will not occur! You can imagine two threads one updating a collection
    and the other one iterating the collection. As long as the update is not performed 
    while the other thread is iterating, everything is fine (no ConcurrentModificationException)!
    But, if for a reason or another the timing changes (e.g. in the user environment) and 
    iterations are performed at the wrong time then your application crashes...
    Not a good thing and very high probability for this to happen!</p>
    </li>

    <li><b> Are {@link javolution.util.FastMap#shared shared maps} valid substitutes for
           <code>ConcurrentHashMap</code>?</b>
       <p> Yes, and they are a lot faster in most cases. Current shared map implementation 
           synchronizes on sub-maps based on hash code dispatching.
           It will block only when you read/write the same sub-map.
           But because the access time is very short (unlike when using read/write locks) the 
           chances of conflicts are slim. The number of sub-maps is based on 
           {@link javolution.context.ConcurrentContext#CONCURRENCY CONCURRENCY)} configurable value.</p>                      
    </li>
 </ol>
 */
package javolution.util;
