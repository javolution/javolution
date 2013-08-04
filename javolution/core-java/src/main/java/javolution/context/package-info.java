/**
<p> Run-time {@link javolution.context.AbstractContext contexts} to facilitate 
    separation of concerns and achieve higher level of performance and flexibility.</p>

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
       Why low-level code need to know which logging facility to use 
       (e.g. standard logging, Log4J library, OSGi LogService or anything else)? 
       Why logging should have to be based upon the class hierarchy (standard logging)? 
       Why can't we attach some relevant information (such as user id, session number, etc.) 
       to the logging content ? </p>
   
   <p> Separation of concerns can be addressed through "Aspect Programming", 
       but there is a rather simpler solution <b>"Context Programming"</b>!</p>
       
   <p> It does not require any particular tool, it basically says that every threads 
       has a {@link javolution.context.AbstractContext context} which can be customized by someone else
      (the one who knows what to do, when running OSGi it is typically a separate bundle).
       Then, your code looks a lot cleaner and is way more flexible as you don't have
       to worry about logging, security, performance etc. in your low level methods. 
[code]
void myMethod() {
    ...
    LogContext.info("Don't know where this is going to be logged to");
    ...
}[/code]</p>
       
   <p> Used properly <i><b>J</b>avolution</i>'s {@link javolution.context.AbstractContext contexts}
       greatly facilitate the separation of concerns. Contexts are fully 
       integrated with OSGi (they are published services). Applications 
       may dynamically plug-in the "right context" using OSGi mechanisms. 
       For example, the {@link javolution.context.SecurityContext SecurityContext}
       might only be known at runtime.</p>
     
 <h2><a name="PREDEFINED">Predefined Contexts:</a></h2>
  <p> <i><b>J</b>avolution</i> provides several useful runtime contexts:<ul>
      <li>{@link javolution.context.ConcurrentContext ConcurrentContext} 
      - To take advantage of concurrent algorithms on multi-cores systems.</li>
      <li>{@link javolution.context.LogContext LogContext} 
      - To log events according to the runtime environment (e.g. {@link org.osgi.service.log.LogService} when running OSGi).</li>     
      <li>{@link javolution.context.LocalContext LocalContext} 
      - To define locally  scoped environment settings.</li>
      <li>{@link javolution.context.SecurityContext SecurityContext} 
      - To address application-level security concerns.</li>
      <li>{@link javolution.context.FormatContext FormatContext} 
      - For plugable objects parsing/formatting. Such as  {@link javolution.text.TextContext TextContext} for plain text, 
        or {@link javolution.xml.XMLContext XMLContext} for XML serialization/deserialization.</li>
      <li>...add your own !</li>
      </ul>
  </p>

<h2><a name="FAQ">FAQ:</a></h2>
<ol>
    <a name="FAQ-1"></a>
    <li><b>In my application I create new threads on-the-fly and I would like them to inherit 
           the current context environment. How can I do that?</b>
    <p> Context is automatically inherited when performing concurrent executions using 
        {@link javolution.context.ConcurrentContext ConcurrentContext}. If you create
        new threads yourself you can easily setup their context as shown below.</p>
[code]
//Spawns a new thread inheriting the context of the current thread.
MyThread myThread = new MyThread();
myThread.inherited = AbstractContext.current(); 
myThread.start(); 
 ...
class MyThread extends Thread {
    AbstractContext inherited;
    public void run() {
        AbstractContext.inherit(inherited); // Sets current context. 
        ...
    }
}[/code]
<p></p>
    </li>
    
    <a name="FAQ-2"></a>
    <li><b>Is it possible to configure the context of all running threads (global configuration) ?</b>
    <p> Yes by publishing an OSGi implementation of the customized context
        (published context services are the default contexts of all running threads).
        Otherwise, you can only configure a context that you have entered (for obvious safety reasons).</p>
<p></p>
    </li> 
 </ol>

*/
package javolution.context;

