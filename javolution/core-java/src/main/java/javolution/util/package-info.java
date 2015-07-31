/**
<p> High-performance collection classes with {@link javolution.lang.Realtime 
    worst case execution time behavior} documented.</p>
<p> Whereas Java current evolution leads to more and more classes being parts of 
    the standard library; Javolution approach is quite the opposite. It aims to
    provide only the quintessential classes from which all others can be derived.
    </p>
    <img alt="architecture image" src="doc-files/architecture.png"> 

<h2><a name="FAQ">FAQ:</a></h2>
<ol>
    <li><b>Does Javolution provide immutable collections similar to 
     the ones provided by Scala or .NET ?</b>
    <p> Using <b>J</b>avolution you may return an immutable 
        view (const reference) over any object which cannot be modified including collections or maps.
{@code
public class UnitSystem {
    final FastSet<Unit> units;
    public UnitSystem(Unit... units) {
       this.units = FastSet.of(units);
    }
    Immutable<Set<Unit>> getUnits() { // Immutable view.
        return units.immutable();
    }
}
...
UnitSystem MKSA = new UnitSystem(M, K, S, A);
}</p>
    </li>
</ol>    
    
 */
package javolution.util;

