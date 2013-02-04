/**
<p> Provides execution {@link javolution.context.AbstractContext contexts} to facilitate 
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
       Why low-level code need to know which logging facility to use 
       (e.g. standard logging, Log4J library; OSGi LogService or anything else)? 
       Why logging should have to be based upon the class hierarchy (standard logging)? 
       Why can't we attach some relevant information to my logging content (e.g. user id, session #, etc.)? </p>
   
   <p> Separation of concerns can be addressed through "Aspect Programming", 
       but there is a rather simpler solution <b>"Context Programming"</b>!</p>
       
   <p> It does not require any particular tool, it basically says that every threads 
       has a {@link javolution.context.AbstractContext context} which can be customized by someone else
      (the one who knows what to do, when running OSGi it is typically a separate bundle).
       Then, your code looks a lot cleaner and is way more flexible as you don't have
       to worry about logging, security, performance etc. in your low level methods. 
       For example:
[code]
void myMethod() {
    ...
    LogContext.info("Don't know where this is going to be logged to");
    ...
}[/code]</p>
       
   <p> Used properly <b>J</b>avolution's {@link javolution.context.AbstractContext contexts}
       greatly facilitate the separation of concerns. Contexts are fully 
       integrated with OSGi (they are published services). Applications 
       may dynamically plug-in the right context for their environment (e.g. 
       {@link javolution.context.SecurityContext SecurityContext}).</p>
     
 <h2><a name="PREDEFINED">Predefined Contexts:</a></h2>
  <p> Here is the list of a few predefined contexts provided by this library.<ul>
      <li>{@link javolution.context.LocalContext LocalContext} - To define locally 
           scoped environment settings.</li>
      <li>{@link javolution.context.ConcurrentContext ConcurrentContext} - To take advantage of concurrent 
          algorithms on multi-processors systems.</li>
      <li>{@link javolution.context.AllocatorContext AllocatorContext} - To control 
           object allocation, e.g. {@link javolution.context.StackContext StackContext}
           to allocate on the stack (or RTSJ ScopedMemory).</li>
      <li>{@link javolution.context.LogContext LogContext} - For performant logging capabilities
           using {@link org.osgi.service.log.LogService} when running OSGi.</li>     
      <li>{@link javolution.context.SecurityContext SecurityContext} - To address application-level security 
          concerns.</li>
      <li>{@link javolution.context.FormatContext FormatContext} - For objects serialization/deserialization, 
          e.g. {@link javolution.text.TextContext TextContext},  {@link javolution.xml.XMLContext XMLContext}.</li>
      <li>...add your own</li>
      </ul>
  </p>

<h2><a name="FAQ">FAQ:</a></h2>
<ol>
    <a name="FAQ-1"></a>
    <li><b>In my application I create new threads on-the-fly and I would like them to inherit 
           the current context stack. How can I do that?</b>
    <p> This can only be done if you enter a concurrent context and set up 
        your thread as indicated below. Then the child thread inherits the context
        stack of the parent thread (invariant regardless of what the parent thread does).
[code]
ConcurrentContext ctx = ConcurrentContext.enter();
try {
    MyThread myThread = new MyThread();
    myThread.parentContext = ctx;
    myThread.start(); // Autonomous thread inheriting an invariant view of the context stack.
} finally {
    ctx.exit(); 
}
...
class MyThread extends Thread {
    ConcurrentContext parentContext;
    public void run() {
        parentContext.setCurrent(); 
        ...
    }
}[/code]</p>

    </li>
    <a name="FAQ-2"></a>
    <li><b>To configure a context I need to enter a context and the configuration 
          will impact only the current thread and possibly threads spawned from that 
          thread (see above). Is it possible to perform 
          global configurations impacting all running threads?</b>
    <p> The right answer to that would be to publish an OSGi implementation 
        of your customized context to be used by all. Another possiblity is at start up; before 
        entering any context to configure the root contexts as shown below.
        It should be noted that with OSGi, this approach is not recommended 
        since the global configuration will be lost if there is a new context 
        implementation published (unlike local configurations).
[code]
class Main { 
    static abstract class TextContextConfigurator extends TextContext {
        public static void configure() {
            TextContext.current().setFormat(Complex.class, polar); // Use polar representation for all complex numbers
        }                                                          // (global setting).
    }
    public static void main(String [] args) {
        TextContextConfigurator.configure();
        ...
    }
}[/code] </p>
    </li> 
    <a name="FAQ-3"></a>
    <li><b>I am writing an application using third party libraries. 
          I cannot avoid GC unless I get the source and patch it to Javolution.
          Can I still make my application real-time using {@link javolution.context.StackContext StackContext}?</b>
    <p> You cannot get determinism using "any" library (including Java standard library) 
    regardless of the garbage collector issue. Array resizing, lazy initialization, map rehashing (...)
    would all introduce unexpected  delay, this is why Javolution comes with its own 
    real-time collections implementation (fractal-based). Furthermore, {@link javolution.context.StackContext StackContext}
    to be efficient will require a RTSJ runtime (ScopedMemory support).</p>
    </li> 
    
 </ol>

*/
package javolution.context;
