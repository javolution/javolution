/**
<p> Provides basic function types for lambda expressions and method references.</p>
    Usually functions do not have a state and can be called concurrently, 
    this should be indicated using the annotation {@link javolution.annotation.Parallelizable Parallelizable}.</p>
    
<p> Functions may take an arbitrary number of arguments through the use of {@link MultiVariable multi-variables}
    or no argument at all using the standard {@link Void} class.  
    [code]
    // Function populating a list of integer and returning void.
    Function<MultiVariable<List<Integer>, Integer>, Void> fill = new Function<>() {
        public Void apply(MultiVariable<List<Integer>, Integer> params) {
            List<Integer> list = params.getLeft();
            for (int i = 0, n = params.getRight(); i < n; i++) {
                list.add(i);
           }
           return null;
        }
    };
    FastTable<Integer> list = new FastTable<Integer>();
    fill.apply(new MultiVariable(list, 100)); // Populates with numbers [0 .. 100[
    [/code] 
    </p>
 */
package javolution.util.function;

