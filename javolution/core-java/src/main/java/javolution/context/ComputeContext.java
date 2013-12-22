/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import javolution.osgi.internal.OSGiServices;

/**
 * <p> A context for executing and chaining calculations in parallel 
 *     on computing devices such as CPUs/GPUs. ComputeContext keeps its 
 *     {@link Buffer buffers} in device memory unless a buffer is 
 *     {@link Buffer#export exported} (to the heap). Device operations 
 *     are asynchronous and performed in parallel whenever possible.
 *     Synchronization with the host (java) is performed only when buffers
 *     are read/exported.
 * [code]
 * VectorFloat64 sumXYZ;
 * VectorFloat64 productXYZ;
 * 
 * ComputeContext ctx = ComputeContext.enter();
 * try {
 *     VectorFloat64 x = new VectorFloat64(1.0, 2.0, 3.0);
 *     VectorFloat64 y = new VectorFloat64(1.0, 2.0, 3.0);
 *     VectorFloat64 z = new VectorFloat64(1.0, 2.0, 3.0);
 *     
 *     sumXYZ = x.plus(y).plus(z).export();        // Both sum and product calculations   
 *     productXYZ = x.times(y).times(z).export();  // are performed in parallel.
 *      
 * } finally { 
 *     ctx.exit(); // Release non-exported resources (e.g. buffers). 
 * }
 * 
 * class VectorFloat64 implements Vector<Float64> { 
 *     private final Buffer buffer;
 *     public VectorFloat64(double... elements) { 
 *         buffer = ComputeContext.newBuffer(DoubleBuffer.wrap(elements));
 *     }
 *     private VectorFloat64(int length) {
 *         buffer = ComputeContext.newBuffer(length * 8L); // Size in bytes.
 *     }
 *     VectorFloat64 plus(VectorFloat64 that) {
 *         if (this.length() != that.length()) throw new DimensionMismatch();
 *         VectorFloat34 result  = new VectorFloat64(this.length()); 
 *         Kernel sum = ComputeContext.newKernel(VectorMath64.class, "sum");   
 *         sum.setArguments(this.buffer.readOnly(), that.buffer.readOnly(), result.buffer);
 *         sum.setGlobalWorkSize(length()); // Number of work-items.
 *         sum.execute(); // Executes in parallel with others kernels. 
 *         return result; 
 *     }
 *     public VectorFloat64 export() { 
 *         buffer.export();
 *         return this;
 *     }
 *     public int length() { return (int) buffer.getByteCount() / 8; }
 * }
 * class VectorMath64 implements Program { // All programs published as OSGi services are 
 *                                         // automatically loaded/compiled by Javolution.
 *     public String toOpenCL() {
 *         return  "__kernel void sum(__global const double *a, __global const double *b, __global double *c) {" +
 *                 "    int gid = get_global_id(0);" +
 *                 "    c[gid] = a[gid] + b[gid];" + "}";
 * }
 * [/code]</p>
 * 
 * <p> For complex data structures shared between OpenCL and Java, 
 *     the use of {@link javolution.io.Struct} is recommended.</p>
 *     
 * <p> <b>Thread-Safety:</b> As for any {@link AbstractContext context}, 
 *     compute contexts are inherently thread-safe (each thread entering 
 *     a context has its own context instance); but nesting a 
 *     {@link ConcurrentContext} within a {@link ComputeContext}
 *      (already intrinsically parallel) is ill-advised 
 *     (the reverse is fine though).</p>         
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.1, December 22, 2013
 * @see <a href="http://en.wikipedia.org/wiki/OpenCL">Wikipedia: OpenCL</a>
 */
public abstract class ComputeContext extends AbstractContext {

    /**
     * A program which can be loaded/compiled on a device. 
     * Any OSGi published instance of this class is automatically 
     * loaded and compiled by <a href="http://javolution.org">Javolution</a>.
     */
    public interface Program {
    
        /**
         * Returns the OpenCL image of this program.
         */
        String toOpenCL();
      
    }
  
    /**
     * A function which can be executed on a device.
     */
    public interface Kernel {

        /**
         * Sets the arguments of this kernel. 
         */
        void setArguments(Object... args);
      
        /**
         * Sets the number of global work-items for each dimension. 
         */
        void setGlobalWorkSize(int... gws);
      
        /**
         * Sets the number of work-items which makes up a work-group for 
         * each dimension. 
         */
        void setLocalWorkSize(int... lws);
      
        /**
         * Executes this kernel possibly in parallel with other kernels
         * executions. This kernel can be reused with new sets of arguments 
         * immediately after this call.  
         */
        void execute();
    }
    
    /**
     * A high speed memory buffer which can be created on a device. 
     */
    public interface Buffer {
    	
     	/**
    	 * Returns the size in bytes of this buffer.
    	 */
    	long getByteCount();
    	
    	/**
    	 * Moves this buffer to host memory.
    	 */
    	void export();
    	
      	/**
    	 * Returns an unmodifiable view over this buffer.
    	 */
    	Buffer readOnly();
    	
    	/**
    	 * Returns this buffer as a {@link ByteBuffer}.
    	 */
    	ByteBuffer asByteBuffer();
    	
    	/**
    	 * Returns this buffer as a {@link CharBuffer}.
    	 */
    	CharBuffer asCharBuffer();
    	
    	/**
    	 * Returns this buffer as a {@link ShortBuffer}.
    	 */
    	ShortBuffer asShortBuffer();
    	
    	/**
    	 * Returns this buffer as a {@link IntBuffer}.
    	 */
    	IntBuffer asIntBuffer();
    	
    	/**
    	 * Returns this buffer as a {@link LongBuffer}.
    	 */
    	LongBuffer asLongBuffer();
    	
    	/**
    	 * Returns this buffer as a {@link FloatBuffer}.
    	 */
    	FloatBuffer asFloatBuffer();
    	
    	/**
    	 * Returns this buffer as a {@link DoubleBuffer}.
    	 */
    	DoubleBuffer asDoubleBuffer();     	
    }

    /**
     * Default constructor.
     */
    protected ComputeContext() {}

    /**
     * Enters the scope of a local computing context; all the resources 
     * (programs, buffers) allocated while in this context scope are 
     * released upon {@link AbstractContext#exit()}.
     */
    public static ComputeContext enter() {
        ComputeContext ctx = currentComputeContext();
        return (ComputeContext) ctx.enterInner();
    }

    /**
     * Explicitly loads the specified program.
     */
    public static void load(Program program) {
    	currentComputeContext().createProgram(program);
    }
    
    /**
     * Explicitly unloads the specified program.
     */
    public static void unload(Program program) {
    	currentComputeContext().releaseProgram(program);
    }
    
    /**
     * Returns a memory buffer having the specified capacity.
     */
    public static Buffer newBuffer(long byteCount) {
    	return currentComputeContext().createBuffer(byteCount);
    }
    
    /**
     * Returns a memory buffer having the specified initial data.
     * Once created the buffer returned as no further link to the specified
     * NIO buffer.
     */
    public static Buffer newBuffer(java.nio.Buffer init) {
    	return currentComputeContext().createBuffer(init);
    }
    
    /**
     * Returns a kernel from the current context having the specified name.
     * If the program has not yet been loaded, a new instance is allocated
     * using the class default constructor and then loaded.
     */
    public static Kernel newKernel(Class<? extends Program> program, String kernelName) {
    	return currentComputeContext().createKernel(program, kernelName);
    }
    
    /**
     * Creates and builds the specified program (if not already done).
     */
    protected abstract void createProgram(Program program);
    
    /**
     * Releases resources associated to the specified program.
     */
    protected abstract void releaseProgram(Program program);
    
    /**
     * Creates a memory buffer having the specified capacity in this context. 
     */
    protected abstract Buffer createBuffer(long byteCount);
    
    /**
     * Creates a buffer having the specified initial data in this context. 
     */
    protected abstract Buffer createBuffer(java.nio.Buffer init);
    
    /**
     * Creates the kernel from the specified program having the specified name.
     * If the program has not yet been loaded, a new instance is allocated 
     * using the class default constructor and then loaded in this context.
     */
    protected abstract Kernel createKernel(Class<? extends Program> program, String kernelName);
    
    /**
     * Returns the current computational context. 
     */
    private static ComputeContext currentComputeContext() {
        ComputeContext ctx = current(ComputeContext.class);
        if (ctx != null)
            return ctx;
        return OSGiServices.getComputeContext();
    }
    
}
