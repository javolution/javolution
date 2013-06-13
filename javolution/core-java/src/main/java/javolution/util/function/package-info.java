/**
<p> Provides basic function types for lambda expressions and method references.</p>
    Usually functions do not have a state and can be called concurrently, 
    if not this should be indicated by implementing the interface {@link Sequential}.
    Functions may take an arbitrary number of arguments through the use of {@link MultiVariable multi-variables}
    or no argument at all using the standard {@link Void} class.  
    [code]
    // Function adding n integers to a list and returning void.
    Function<MultiVariable<Integer, List<Integer>>, Void> fill = new Function<>() {
        public Void apply(MultiVariable<Integer, List<Integer>> params) {
            List<Integer> list = params.getRight();
            for (int i = 0; i < params.getLeft(); i++) {
                list.add(i);
           }
           return null;
        }
    };
    FastTable<Integer> list = new FastTable<Integer>();
    fill.evaluate(new MultiVariable(100, list)); // Populates with numbers [0 .. 100[
    [/code] 
    In future versions, classes in in this package may derive
    from classes in <code>java.util.function</code> classes. 
 */
package javolution.util.function;

