/**
<p> Provides basic function types for lambda expressions and method references.</p>
    All functions are hierarchically organized (e.g. {@link Predicate} and 
    {@link Supplier} are derived from {@link Function}). Functions may take an
    arbitrary number of arguments using the {@link MultiVariable} class or no
    argument at all using the standard {@link Void} class.  
    [code]
    // Function adding n integers to a list and returning void.
    Function<MultiVariable<Integer, List<Integer>>, Void> fill = new Function<MultiVariable<Integer, List<Integer>>, Void>() {
        public Void evaluate(MultiVariable<Integer, List<Integer>> param) {
            List<Integer> list = param.getRight();
            for (int i = 0; i < param.getLeft(); i++) {
                list.add(i);
           }
           return null;
        }
    };
    ...
    FastTable<Integer> list = new FastTable<Integer>();
    fill.evaluate(new MultiVariable(100, list));
    ...
    [/code] 
    In future version, the classes defined in this package may derive
    from <code>java.util.function</code> classes without breaking backward 
    compatibility of course (using JDK 8 interface default method implementations). 
 */
package javolution.util.function;

