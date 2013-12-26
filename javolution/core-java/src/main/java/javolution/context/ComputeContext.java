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

import javolution.lang.Configurable;
import javolution.osgi.internal.OSGiServices;

/**
 * <p> A context for executing and chaining calculations in parallel 
 *     on computing devices such as CPUs/GPUs. ComputeContext keeps its 
 *     {@link Buffer buffers} in device memory unless a buffer is 
 *     {@link Buffer#export exported} (to the heap). Device operations 
 *     are asynchronous and performed in parallel whenever possible
 *     (based on inputs parameters dependencies).
 *     Synchronization with host (java) is performed when buffers
 *     are read/exported (typically to retrieve the calculations results).
 * [code]
 * VectorFloat64 result;
 * ComputeContext ctx = ComputeContext.enter();
 * try {
 *     VectorFloat64 x = new VectorFloat64(1.0, 2.0, 3.0);
 *     VectorFloat64 y = new VectorFloat64(1.0, 2.0, 3.0);
 *     VectorFloat64 z = new VectorFloat64(1.0, 2.0, 3.0);
 *     
 *     VectorFloat64 sumXYZ = x.plus(y).plus(z);        // Both sum and product calculations   
 *     VectorFloat64 productXYZ = x.times(y).times(z);  // are performed in parallel.
 *     
 *     result = sumXYZ.plus(productXYZ).export(); // Moves result to host (blocking).
 * } finally { 
 *     ctx.exit(); // Release non-exported resources (e.g. device buffers). 
 * }
 * 
 * class VectorFloat64 implements Vector<Float64> { 
 *     private final ComputeContext.Buffer buffer;
 *     public VectorFloat64(double... elements) { 
 *         buffer = ComputeContext.newBuffer(DoubleBuffer.wrap(elements));
 *     }
 *     private VectorFloat64(int length) {
 *         buffer = ComputeContext.newBuffer(length * 8L); // Size in bytes.
 *     }
 *     VectorFloat64 plus(VectorFloat64 that) {
 *         if (this.length() != that.length()) throw new DimensionMismatch();
 *         VectorFloat34 result  = new VectorFloat64(this.length()); 
 *         ComputeContext.Kernel sum = ComputeContext.newKernel(VectorMath64.class, "sum");   
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
 * <p> By {@link ComputeContext#DOUBLE_PRECISION_REQUIRED default}, the best 
 *     GPU device with support for double precision floating-point is 
 *     selected. If your GPU does not have such support, the CPU device 
 *     is chosen. For complex data structures shared between OpenCL and Java, 
 *     {@link javolution.io.Struct} is recommended.</p>
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
     * Indicates if support for double precision floating-point is required
     * (default {@code true}). Running with the option {@code 
     * -Djavolution.context.ComputeContext#DOUBLE_PRECISION_REQUIRED=false}
     * disables the requirement to select a device supporting 
     * double precision.
     */
    public static final Configurable<Boolean> DOUBLE_PRECISION_REQUIRED 
           = new Configurable<Boolean>() {
        @Override
        protected Boolean getDefault() {
            return true;
        }
    };
    
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
    	 * 
    	 * @throws UnsupportedOperationException if 
    	 *         {@link ComputeContext#DOUBLE_PRECISION_REQUIRED} is 
    	 *         configured to {@code false}
    	 */
    	DoubleBuffer asDoubleBuffer();     	
    }

    /**
     * Default constructor.
     */
    protected ComputeContext() {}

    /**
     * Enters the scope of a local computing context; all the resources 
     * (kernels, buffers) allocated while in this context scope are 
     * released upon {@link AbstractContext#exit() exit}.
     */
    public static ComputeContext enter() {
        ComputeContext ctx = currentComputeContext();
        return (ComputeContext) ctx.enterInner();
    }

    /**
     * Explicitly loads the specified program.
     */
    public static void load(Program program) {
    	currentComputeContext().loadAndBuild(program);
    }
    
    /**
     * Explicitly unloads the specified program.
     */
    public static void unload(Program program) {
    	currentComputeContext().unloadAndFree(program);
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
     * Loads and builds the specified program.
     */
    protected abstract void loadAndBuild(Program program);
    
    /**
     * Unloads and free the specified program.
     */
    protected abstract void unloadAndFree(Program program);
    
    /**
     * Creates a memory buffer having the specified capacity in this context. 
     */
    protected abstract Buffer createBuffer(long byteCount);
    
    /**
     * Creates a buffer having the specified initial data in this context. 
     */
    protected abstract Buffer createBuffer(java.nio.Buffer init);
    
    /**
     * Creates the kernel from the specified program having the specified name
     * in this context. If the specified program has not yet been loaded 
     * in this context or a parent context, a new instance is allocated 
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
