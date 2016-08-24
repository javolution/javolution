/**
<p> Basic functions for lambda expressions and method references.</p>
    Most often, functions do not have a state and can be called concurrently, 
    as indicated by the annotation {@link javolution.lang.Parallelizable Parallelizable}.</p>
    
<p> Functions may take an arbitrary number of arguments through the use of the 
    {@link javolution.lang.Binary Binary}/ {@link javolution.lang.Ternary Ternary} 
    types or no argument at all using the standard {@link java.lang.Void} class.  
[code]
// Function indicating if two persons are married.
Predicate<Binary<Person, Person>> areMarried = ...;
Binary<Person, Person> johnAndJane = ...;
boolean areJohnAndJaneMarried = areMarried.test(johnAndJane);

// Function throwing an error.
Supplier<Void> throwError = () -> throw new Error();
[/code]</p>
 */
package org.javolution.util.function;

