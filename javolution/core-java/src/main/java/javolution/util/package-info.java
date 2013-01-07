/**
<p> High-performance collection classes.</p>

<p> Although this package provides very few collection classes, they are substitutes for
    most of <code>java.util.*</code> classes (for example, <code>java.util.IdentityHashMap</code> would be 
    a {@link javolution.util.FastMap FastMap} with an 
    {@link javolution.util.FastComparator#IDENTITY identity} key comparator).</p>

<h2><a name="FAQ">FAQ:</a></h2>
<ol>
    <li><b>ArrayList may throw ConcurrentModificationException,
           but <b>J</b>avolution FastTable does not, why?</b>
    <p> FastTable (or any <b>J</b>avolution collection/map) <b>do support concurrent modifications</b>
        as long as the collections/maps is {@link javolution.util.FastCollection FastCollection#shared shared}.
        You can iterates using closure or use concurrently shared collections/maps even if 
        new elements/entries are added/deleted (shared fast map implementation
        synchronizes on sub-maps with hash code dispatching).</p>
    
    <p><i> Disallowing concurrent modifications (standard java util) has proven to be a performance 
    killer for many (forcing users to work with copies of their whole collections). Furthermore the additional checks required
    directly impact performance.</i></p>
    </li>
    
    <li><b>Do you have a test case showing any scenario of concurrent modification where 
          ArrayList "fails" and FastTable doesn't?</b>
    <p> Let's say that you have a collection of "Units", and you want to provide users 
        with a read-only view of these units. The following code will fail miserably:[code]
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
    <p> Now with FastTable the following is completely safe even when new units are added:[code]
    public class Unit {
        static Collection<Unit> INSTANCES = new FastTable<Unit>().shared();
        public static Collection<Unit> getInstances() { // Const. view.
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
       <p> Yes, and they are a lot faster in most cases. Shared fast maps are
           basically standard fast map with synchronization on sub-maps (hash code dispatching).
           It will block only when you read/write the same sub-map. 
           But because the access time is very short (unlike when using read/write locks) the 
           chances of conflicts are slim.</p>                      
    </li>
 </ol>
 */
package javolution.util;
