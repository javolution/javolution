/**
<p> Run-time {@link javolution.context.AbstractContext contexts} to facilitate 
    separation of concerns and achieve higher level of performance and flexibility.</p>

## Separation of Concerns

Separation of concerns is an important design principle greatly misunderstood. 
Most developers think it is limited to modularity and encapsulation or it
requires special programming tools (e.g. Aspect programming).
       
Separation of concerns is very powerful and easier than it looks. 
Basically, it could be summarised as the "pass the buck principle".
If you don't know what to do with some information, just give it to someone
else who might know.
       
A frequent example is the catching of exceptions too early (with some logging processing) 
instead of throwing a checked exception. Unfortunately, they are still plenty of cases
where the separation of concerns is not as good as it could be. For example logging!
Why low-level code need to know which logging facility to use 
(e.g. standard logging, Log4J library, OSGi LogService or anything else)? 
Why logging should have to be based upon the class hierarchy (standard logging)? 
Why can't we attach some relevant information (such as user id, session number, etc.) 
to the logging content ? 
   
Separation of concerns can be addressed through "Aspect Programming", 
but there is a rather simpler solution <b>"Context Programming"</b>!</p>
       
It does not require any particular tool, it basically says that every threads 
has a {@link javolution.context.AbstractContext context} which can be customized by someone else
(the one who knows what to do, when running OSGi it is typically a separate bundle).
Then, your code looks a lot cleaner and is way more flexible as you don't have
to worry about logging, security, performance etc. in your low level methods. 

```java
void myMethod() {
    ...
    LogContext.info("Don't know where this is going to be logged to");
    ...
}
```       

Used properly <i><b>J</b>avolution</i>'s {@link javolution.context.AbstractContext contexts}
greatly facilitate the separation of concerns. Contexts are fully 
integrated with OSGi (they are published services). Applications 
may dynamically plug-in the "right context" using OSGi mechanisms. 
For example, the {@link javolution.context.SecurityContext SecurityContext} might only be known at runtime.
     
## Predefined Contexts

`Javolution` provides several useful runtime contexts:

- {@link javolution.context.ComputeContext ComputeContext} - To take advantage of computing devices (GPUs)
  and to reduce heap allocations.
- {@link javolution.context.ConcurrentContext ConcurrentContext} - To take advantage of concurrent algorithms
  on multi-cores systems.
- {@link javolution.context.LogContext LogContext} - To log events according to the runtime environment 
  (e.g. {@link org.osgi.service.log.LogService} when running OSGi).     
- {@link javolution.context.LocalContext LocalContext} - To define locally  scoped environment settings.
- {@link javolution.context.ComputeContext ComputeContext} - To accelerate computations using GPUs device (if present).
- {@link javolution.context.SecurityContext SecurityContext} - To address application-level security concerns.
- {@link javolution.context.StorageContext StorageContext} - To store/retrieve your persistent data/dataset.
- {@link javolution.context.FormatContext FormatContext} - For plugable objects parsing/formatting.
   Such as  {@link javolution.text.TextContext TextContext} for plain text, 
   or {@link javolution.xml.XMLContext XMLContext} for XML serialization/deserialization.
- {@link javolution.context.StorageContext StorageContext} - To store/retrieve your persistent data/dataset.
- ...add your own !

  

## FAQ

1. Thanks for providing GPUs accelerated {@link javolution.context.ComputeContext ComputeContext} but why 
   is it recommended to enter/exit a context scope in order to use it ?
> ComputeContext allocates buffers in the device memory (CPU/GPU), these buffers are released either through 
> garbage collection or immediately when exiting the context scope. Freeing the resources immediately ensures
> a more predictable time-behaviour and less of a chance to run out of device memory.

```java
public static void main(String... args) {
    while (true) {
         ComputeContext ctx = ComputeContext.enter();
         try {
             readInput();
             calculateOutput();
             refreshDisplay();
         } finally {
             ctx.exit(); // All temporary buffers are released.
         }    
    }
}}
```

2. In my application I create new threads on-the-fly and I would like them to inherit the current context environment. 
   How can I do that?
> Context is automatically inherited when performing concurrent executions using 
> {@link javolution.context.ConcurrentContext ConcurrentContext}. If you create new threads yourself 
> you can easily setup their context as shown below.

```java
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
}
```

3. Is it possible to configure the context of all running threads (global configuration) ?
> Yes by publishing an OSGi implementation of the customized context
> (published context services are the default contexts of all running threads).
> Otherwise, you can only configure a context that you have entered (for obvious safety reasons).</p>

*/
package org.javolution.context;

