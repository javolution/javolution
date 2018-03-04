## Javolution
#### The Java(TM) Solution for Real-Time and Embedded Systems
##### Because real-time programming requires a time-predictable standard library.


> "The ability to simplify means to eliminate the unnecessary so that the necessary may speak."
>                                                           - Hans Hofmann, Introduction to the Bootstrap, 1993

### Javolution - A Java Revolution?

Javolution real-time goals are simple: To make your application faster and more time predictable!

- **High-Performance** - Hardware accelerated computing (GPUs) with ComputeContext.
- **Minimalistic** - Collection classes, supporting custom views, closure-based iterations, map-reduce paradigm, parallel computations, etc.
- **Optimized** - To reduce the worst case execution time documented through annotations.
- **Innovative** - Fractal-based structures to maintain high-performance regardless of the size of the data.
- **Multi-Cores Ready** - Most parallelizable classes (including collections) are either mutex-free (atomic) or using extremely short locking time (shared).
- **OSGi Compliant** - Run as a bundle or as a standard library. OSGi contexts allow cross cutting concerns (concurrency, logging, security, ...) to be addressed at run-time through OSGi published services without polluting the application code (Separation of Concerns).
- **Interoperable** - Struct and Union base classes for direct interfacing with native applications. Development of the Javolution C++ library to mirror its Java counterpart and makes it easy to port any Java application to C++ for native compilation (maven based) or to write Java-Like code directly in C++ (more at Javolution C++ Overview)).
- **Simple** - You don't need to know the hundreds of new Java 8 util.* classes, most can be built from scratch by chaining Javolution collections or maps. No need to worry about configuration, immutability or code bloating !
- **Free** - Permission to use, copy, modify, and distribute this software is freely granted, provided that copyright notices are preserved (BSD License).

The Top-10 Reasons why you should be using Javolution

- Javolution classes are simple to use, even simpler than most JDK classes. You don't need to guess the capacity of a TextBuilder, FastTable or a FastMap, their size expand gently without ever incurring expensive resize/copy or rehash operations (unlike StringBuilder, ArrayList or HashMap).
- Developers may achieve true separation of concerns (e.g. logging, configuration) through Context Programming or by using classes such as Configurable.
- Javolution classes are fast, very fast (e.g. Text insertion/deletion in O[Log(n)] instead of O[n] for standard StringBuffer/StringBuilder).
- All Javolution classes are hard real-time compliant with documented real-time behavior.
- Javolution makes it easy for concurrent algorithms to take advantage of multi-processors systems.
- Javolution's real-time collection classes (map, table and set) can be used in place of most standard collection classes and provide numerous additional capabilities.
- Any Java class can be serialized/deserialized in XML format in any form you may want, also no need to implement Serializable or for the platform to support serialization
- Javolution provides Struct and Union classes for direct interoperability with C/C++ applications.
- Javolution is fully integrated with OSGi but still can be used as a standard Java library.
- Javolution can be either a Pure Java Solution or a Pure Native Solution (C++ mirror), small (less than 400 KBytes jar) and fully produced through maven (Java and C++).

### Usage
The simplest way to use Javolution is through Maven with the following dependency in your POM.xml

```
      <dependency>
          <groupId>org.javolution</groupId>
          <artifactId>javolution-core-java</artifactId>
          <version>7.0.0</version>
      </dependency>
```

The native dynamic library (.dll or .so) is generated from the sources using the Maven Native Plugin
Released artifacts are available from the maven central repository, all snapshots (with sources/javadoc) are deployed to the java.net repository
The standard Java distribution is an OSGi bundle which can also be used as standalone Java library. The C++ distribution includes a port of standard Java classes, Javolution classes, OSGi and JUnit. Below is the table of correspondance between the Java packages and Javolution C++ namespaces.

### Links

- Website: http://javolution.org
- JavaDoc: http://javolution.org/apidocs/index.html
