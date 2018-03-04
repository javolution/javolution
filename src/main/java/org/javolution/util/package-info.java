/**
 * High-performance collection classes with {@link javolution.lang.Realtime worst case execution time behavior} 
 * documented.
 * 
 * Whereas Java current evolution leads to more and more classes being parts of the standard library; Javolution 
 * approach is quite the opposite. It aims to  provide only the quintessential classes from which all others can
 * be derived.
 * <img alt="architecture image" src="doc-files/architecture.png"> 
 * 
 * All collections classes support numerous views which can be chained:
 * 
 *  - {@link AbstractCollection#concat concat(Collection)} - View resulting of the concatenation of two collections.
 *  - {@link AbstractCollection#parallel parallel()} - View allowing parallel processing of {@link Parallel} operations.
 *  - {@link AbstractCollection#sequential sequential()} - View disallowing parallel processing of {@link Parallel} operations.
 *  - {@link AbstractCollection#unmodifiable unmodifiable()} - View which does not allow for modifications.
 *  - {@link AbstractCollection#shared shared()} - Thread-safe view based on <a href= "http://en.wikipedia.org/wiki/Readers%E2%80%93writer_lock">
 *                      readers-writer locks</a>.
 *  - {@link AbstractCollection#atomic atomic()} - Thread-safe view for which all reads are mutex-free and collection updates 
 *                      (e.g. {@link AbstractCollection#addAll addAll}, {@link AbstractCollection#removeIf removeIf}, ...) are atomic.
 *  - {@link AbstractCollection#filter filter(Predicate)} - View exposing only the elements matching the specified filter and 
 *                                         preventing elements not matching the specified filter from being added.
 *  - {@link AbstractCollection#map map(Function)} - View exposing elements through the specified mapping function.
 *  - {@link AbstractCollection#sorted(Comparator) sorted(Comparator)} - View exposing elements sorted according to the specified comparator.
 *  - {@link AbstractCollection#reversed reversed()} - View exposing elements in the reverse iterative order.
 *  - {@link AbstractCollection#distinct distinct()} - View exposing each element only once.
 *  - {@link AbstractCollection#linked linked()} - View exposing each element based on its {@link #add insertion} order.
 *  - {@link AbstractCollection#equality(Equality) equality(Equality)} - View using the specified comparator to test for element equality 
 *                                  (e.g. {@link AbstractCollection#contains}, {@link AbstractCollection#remove}, {@link AbstractCollection#distinct}, ...)
 * 
 * For all these views, the chaining order <b>does matter!</b>
 * 
 * ```java
 * FastTable<String> names = new FastTable<String>().with("Sim Ilicuir", "Pat Ibulair");
 *      
 * names.sorted().reversed(); // Reversed sorting order.
 * names.reversed().sorted(); // Standard sorting order.
 * 
 * names.filter(s -> s.startsWith("X")).parallel().clear(); // Parallel removal of names starting with "X"
 * names.parallel().filter(s -> s.startsWith("X")).clear(); // Sequential removal of names starting with "X"
 * 
 * List<String> atomic = names.sorted().atomic(); 
 * List<String> nonAtomic = names.atomic().sorted();
 * 
 * List<String> threadSafe = names.linked().shared();  
 * List<String> threadUnsafe = names.shared().linked(); 
 * ``` 
*  It should be noted that {@link AbstractCollection#unmodifiable unmodifiable} views *are not immutable*; 
 * {@link Immutable constant/immutable} collections (or maps) can only be obtained through the freeze method.
 * 
 * ```java
 * FastSet.Immutable<String> winners 
 *     = new FastSet<String>().with("John Deuff", "Otto Graf", "Sim Kamil").freeze();
 * FastMap.Immutable<String, Integer> wordToInt 
 *     = new FastMap<String, Integer>().with("one", 1).with("two", 2).with("three", 3).freeze();
 * ``` 
  * 
 * Views are similar to [Java 8 streams](http://lambdadoc.net/api/java/util/stream/package-summary.html)
 * except that views are themselves collections and actions on the view *will impact* 
 * the original collection. Collection views are nothing "new" since they already existed in the original 
 * java.util collection classes (e.g. `List.subList(...), Map.keySet(), Map.values()`). Javolution extends to 
 * this concept and allows views to be chained in order to address the issue of class proliferation.
 * 
 * ```java
 * names.subTable(0, n).clear(); // Removes the n first names (see java.util.List.subList).
 * names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 * names.filter(s -> s.length > 16).clear(); // Removes all the persons with long names.
 * names.sorted().reversed().forEach(str -> System.out.println(str)); // Prints names in reverse alphabetical order.
 * tasks.parallel().forEach(task -> task.run()); // Execute concurrently the tasks and wait for their completion.
 * ``` 
 * 
 * Views can of course be used to perform "stream" oriented filter-map-reduce operations with the same benefits:
 * Parallelism support, excellent memory characteristics (no caching, cost nothing to create), etc.
 * 
 * ```java
 * String anyFound = names.filter(s -> s.length > 16).findAny(); // Sequential search (returns the first found).
 * String anyFound = names.filter(s -> s.length > 16).parallel().findAny(); // Parallel search.
 * Collection<String> allFound = names.filter(s -> s.length > 16).collect(); // Sequential reduction.
 * Collection<String> allFound = names.filter(s -> s.length > 16).parallel().collect(); // Parallel reduction.
 * 
 * String maxName = names.parallel().maxBy((x,y) -> x.length - y.length); // Finds the name of maximum length in parallel.
 * int sumLength = names.map(s -> s.length).parallel().reduce((x,y)-> x + y); // Calculates the sum in parallel.
 * 
 * // Class.getEnclosingMethod (JDK) using Javolution's views and Java 8.
 * Method matching = new FastTable<Method>().with(enclosingInfo.getEnclosingClass().getDeclaredMethods())
 *     .filter(m -> Objects.equals(m.getName(), enclosingInfo.getName())
 *     .filter(m -> Arrays.equals(m.getParameterTypes(), parameterClasses))
 *     .filter(m -> Objects.equals(m.getReturnType(), returnType)).findAny(); 
 * if (matching == null) throw new InternalError("Enclosing method not found");
 * return matching;
 * ``` 
 */
package org.javolution.util;

import java.util.Comparator;

import org.javolution.annotations.Parallel;
import org.javolution.util.function.Equality;
