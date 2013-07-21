/**
<p> High-performance collection classes with {@link javolution.lang.RealTime 
    guaranteed worst case execution time}.</p>
<p> Whereas Java current evolution leads to more and more classes being parts of 
    the standard library; Javolution approach is quite the opposite. It aims to
    provide only the quintessential classes from which all others can be derived.
    </p>
    <img src="doc-files/architecture.png" /> 

<h2><a name="FAQ">FAQ:</a></h2>
<ol>
    <li><b>Does <b>J</b>avolution provide immutable collections similar to 
     the ones provided by Scala or .NET ?</b>
    <p> Using <b>J</b>avolution you may return an {@link javolution.lang.Immutable Immutable} 
        reference (const reference) over any object which cannot be modified including collections or maps.
[code]
public class UnitSystem {
    Set<Unit> units;
    public UnitSystem(Immutable<Set<Unit>> units) {
       this.units = units.value(); // Defensive copy unnecessary (immutable)
    }
}
...
Immutable<Set<Unit>> unitsMKSA = new FastSet<Unit>().addAll(M, K, S, A).toImmutable();
UnitSystem MKSA = new UnitSystem(unitsMKSA);
[/code]</p>
    </li>
</ol>    
    
 */
package javolution.util;

