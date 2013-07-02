/**
<p> High-performance collection classes based on the OSGi principles (pluggable 
    and run-time adaptable implementations for optimum performance).</p>
    <img src="doc-files/architecture.png" /> 
<p> Whereas Java current evolution leads to more and more classes being parts of 
    the standard library; Javolution approach is quite the opposite. It aims to
    provide only the quintessential classes from which all others can be derived.
    </p>

<h2><a name="FAQ">FAQ:</a></h2>
<ol>
    <li><b>Why <b>J</b>avolution does not provide immutable collections similar to 
     the ones provided by Scala or .NET ?</b>
    <p> In standard collection frameworks there are some benefit in terms 
        of algorithmic complexity for using immutable collections. For example,
        the {@code List.Add} method in .NET has a WCET of {@code O(n)} (due to resizing) 
        versus {@code O(Log(n))} for the {@code ImmutableList.Add} version. 
        It is not the case with <b>J</b>avolution classes (incremental resize).<p>   
    <p> Nonetheless, for semantic reasons it is always possible to return an {@link Immutable} reference 
        over any {@link javolution.util.FastCollection FastCollection FastCollection}
        using the {@link javolution.util.FastCollection#toImmutable() toImmutable()} method.
[code]
public class UnitSystem {
    Set<Unit> units;
    public UnitSystem(Immutable<Set<Unit>> units) {
       this.units = units.value(); // No need to copy (semantic contract)
    }
}
...
FastSet<Unit> unitsMKSA = new FastSet<Unit>(M, K, S, A);
UnitSystem MKSA = new UnitSystem(unitsMKSA.toImmutable());
[/code]</p>
    </li>
</ol>    
    
 */
package javolution.util;

