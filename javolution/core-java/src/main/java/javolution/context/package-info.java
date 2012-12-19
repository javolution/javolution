/**
<p> Provides real-time {@link javolution.context.Context} to facilitate 
    separation of concerns and achieve higher level of performance and 
    code predictability.</p>

<h2><a name="OVERVIEW">Separation of Concerns</a></h2>

   <p> Separation of concerns is an important design principle greatly misunderstood. 
       Most developers think it is limited to modularity and encapsulation or it
       requires special programming tools (e.g. Aspect programming).</p>
       
   <p> Separation of concerns is very powerful and easier than it looks. 
       Basically, it could be summarized as the "pass the buck principle".
       If you don't know what to do with some information, just give it to someone
       else who might know.</p>
       
   <p> A frequent example is the catching of exceptions too early (with some logging processing) 
       instead of throwing a checked exception. Unfortunately, they are still plenty of cases
       where the separation of concerns is not as good as it could be. For example logging!
       Why low-level code need to know which logging facility is being used 
       (e.g. standard logging, Log4J library or anything else)? 
       Furthermore, why logging should have to be based upon the class hierarchy
       (standard logging)?</p>
   
   <p> Separation of concerns can be addressed through "Aspect Programming", 
       but there is a rather simpler solution <b>"Context Programming"</b>!</p>
       
   <p> It does not require any particular tool, it basically says that every threads 
       has a context which can be customized by someone else (the one who knows what to do).
       Then, your code looks a lot cleaner and is way more flexible as you don't have
       to worry about logging, security, performance etc. in your low level methods. 
       For example:[code]
       void myMethod() {
           ...
           LogContext.info("Don't know where this is going to be logged to");
           ...
       }[/code]
       </p>
       
   <p> Used properly <b>J</b>avolution's {@link javolution.context.Context contexts}
       greatly facilitate the separation of concerns. Contexts are  
       complemented by others classes such as for example the 
       {@link javolution.lang.Configurable Configurable} class to reduce 
       dependency between configuration and application code.</p>
     
 <h2><a name="PREDEFINED">Predefined Contexts:</a></h2>
  <p> This package provides few predefined contexts:<ul>
      <li>{@link javolution.context.LocalContext LocalContext} - To define locally 
           scoped environment settings.</li>
      <li>{@link javolution.context.ConcurrentContext ConcurrentContext} - To take advantage of concurrent 
          algorithms on multi-processors systems.</li>
      <li>{@link javolution.context.AllocatorContext AllocatorContext} - To control 
           object allocation, e.g. {@link javolution.context.StackContext StackContext}
           to allocate on the stack (or RTSJ ScopedMemory).</li>
      <li>{@link javolution.context.LogContext LogContext} - For performant logging capabilities
           using either {@link org.osgi.service.log.LogService} when running OSGi or 
          {@link javolution.util.StandardLog StandardLog} when running outside of OSGi.</li>     
      <li>{@link javolution.context.SecurityContext SecurityContext} - To address application-level security 
          concerns.</li>
      </ul>
  </p>

 */
package javolution.context;
