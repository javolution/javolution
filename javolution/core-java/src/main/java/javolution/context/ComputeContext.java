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
 * {@code
 * FloatVector result;
 * ComputeContext ctx = ComputeContext.enter();
 * try {
 *     FloatVector x = new FloatVector(1.0, 2.0, 3.0);
 *     FloatVector y = new FloatVector(1.0, 2.0, 3.0);
 *     FloatVector z = new FloatVector(1.0, 2.0, 3.0);
 *     
 *     FloatVector sumXYZ = x.plus(y).plus(z);        // Both sum and product calculations   
 *     FloatVector productXYZ = x.times(y).times(z);  // are performed in parallel.
 *     
 *     result = sumXYZ.plus(productXYZ);
 *     result.export(); // Moves result to global memory (heap).
 * } finally { 
 *     ctx.exit(); // Release all local resources (e.g. device buffers). 
 * }
 * 
 * class FloatVector implements ComputeContext.Local { 
 *     private final ComputeContext.Buffer buffer;
 *     public FloatVector(double... elements) { 
 *         buffer = ComputeContext.newBuffer(DoubleBuffer.wrap(elements));
 *     }
 *     private FloatVector(int length) {
 *         buffer = ComputeContext.newBuffer(length * 8L); // Size in bytes.
 *     }
 *     FloatVector plus(FloatVector that) {
 *         if (this.length() != that.length()) throw new DimensionMismatch();
 *         FloatVector result  = new FloatVector(this.length()); 
 *         ComputeContext.Kernel sum = ComputeContext.newKernel(VectorFP64.class, "sum");   
 *         sum.setArguments(this.buffer.readOnly(), that.buffer.readOnly(), result.buffer);
 *         sum.setGlobalWorkSize(length()); // Number of work-items.
 *         sum.execute(); // Executes in parallel with others kernels. 
 *         return result; 
 *     }
 *     {@literal@}Override
 *     public void export() { // Moves to global memory. 
 *         buffer.export();
 *     }
 *     public int length() { return (int) buffer.getByteCount() / 8; }
 * }
 * class VectorFP64 implements Program { // All programs published as OSGi services are 
 *                                       // automatically loaded/compiled by Javolution.
 *     public String toOpenCL() {
 *         return  "__kernel void sum(__global const double *a, __global const double *b, __global double *c) {" +
 *                 "    int gid = get_global_id(0);" +
 *                 "    c[gid] = a[gid] + b[gid];" + "}";
 * }
 * }}</p>
 * 
 * <p> By default, the best GPU device with support for {@link 
 *     ComputeContext#DOUBLE_PRECISION_REQUIRED double precision 
 *     floating-point} is selected. If your GPU does not have such support, 
 *     the CPU device is chosen. For complex data structures shared between 
 *     OpenCL and Java, {@link javolution.io.Struct} is recommended.</p>
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
	 * A high speed memory buffer which can be created on a device. 
	 */
	public interface Buffer extends Local {

		/**
		 * @return this buffer as a {@link ByteBuffer}.
		 */
		ByteBuffer asByteBuffer();

		/**
		 * @return this buffer as a {@link CharBuffer}.
		 */
		CharBuffer asCharBuffer();

		/**
		 * @return this buffer as a {@link DoubleBuffer}.
		 * @throws UnsupportedOperationException if 
		 *         {@link ComputeContext#DOUBLE_PRECISION_REQUIRED} is 
		 *         configured to {@code false}
		 */
		DoubleBuffer asDoubleBuffer();

		/**
		 * @return this buffer as a {@link FloatBuffer}.
		 */
		FloatBuffer asFloatBuffer();

		/**
		 * @return this buffer as a {@link IntBuffer}.
		 */
		IntBuffer asIntBuffer();

		/**
		 * @return this buffer as a {@link LongBuffer}.
		 */
		LongBuffer asLongBuffer();

		/**
		 * @return this buffer as a {@link ShortBuffer}.
		 */
		ShortBuffer asShortBuffer();

		/**
		 * @return the size in bytes of this buffer.
		 */
		long getByteCount();

		/**
		 * @return an unmodifiable view over this buffer.
		 */
		Buffer readOnly();
	}

	/**
	 * A function which can be executed on a device.
	 */
	public interface Kernel {

		/**
		 * Executes this kernel possibly in parallel with other kernels
		 * executions. This kernel can be reused with new sets of arguments 
		 * immediately after this call.  
		 */
		void execute();

		/**
		 * Sets the arguments of this kernel. 
		 * @param args List of kernel arguments
		 */
		void setArguments(Object... args);

		/**
		 * @param gws Sets the number of global work-items for each dimension.  
		 */
		void setGlobalWorkSize(int... gws);

		/**
		 * @param lws Sets the number of work-items which makes up a work-group for 
		 * each dimension. 
		 */
		void setLocalWorkSize(int... lws);
	}

	/**
	 * An object which may be partially allocated in local device memory.
	 * Local memory is used to enable coalesced accesses, to share data between
	 * work items in a work group, and to reduce accesses to lower bandwidth 
	 * global memory.
	 */
	public interface Local {

		/**
		 * Exports this object to global memory (heap). 
		 */
		void export();

	}

	/**
	 * A program which can be loaded/compiled on a device. 
	 * Any OSGi published instance of this class is automatically 
	 * loaded and compiled by <a href="http://javolution.org">Javolution</a>.
	 */
	public interface Program {

		/**
		 * @return the OpenCL image of this program.
		 */
		String toOpenCL();

	}

	/**
	 * Indicates if support for double precision floating-point is required
	 * (default {@code true}). Running with the option {@code 
	 * -Djavolution.context.ComputeContext#DOUBLE_PRECISION_REQUIRED=false}
	 * disables the requirement to select a device supporting 
	 * double precision.
	 */
	public static final Configurable<Boolean> DOUBLE_PRECISION_REQUIRED = new Configurable<Boolean>() {
		@Override
		protected Boolean getDefault() {
			return true;
		}
	};

	/**
	 * Returns the current computational context. 
	 */
	private static ComputeContext currentComputeContext() {
		ComputeContext ctx = current(ComputeContext.class);
		if (ctx != null)
			return ctx;
		return OSGiServices.getComputeContext();
	}

	/**
	 * Enters the scope of a local computing context; all the resources 
	 * (kernels, buffers) allocated while in this context scope are 
	 * released upon {@link AbstractContext#exit() exit}.
	 * @return Reference to the entered ComputeContext
	 */
	public static ComputeContext enter() {
		ComputeContext ctx = currentComputeContext();
		return (ComputeContext) ctx.enterInner();
	}

	/**
	 * Explicitly loads the specified program.
	 * @param program to load
	 */
	public static void load(Program program) {
		currentComputeContext().loadAndBuild(program);
	}

	/**
	 * Returns a memory buffer having the specified initial data.
	 * Once created the buffer returned as no further link to the specified
	 * NIO buffer.
	 * @param init data buffer
	 * @return Buffer with the specified initial state
	 */
	public static Buffer newBuffer(java.nio.Buffer init) {
		return currentComputeContext().createBuffer(init);
	}

	/**
	 * Returns a memory buffer having the specified capacity.
	 * @param byteCount count
	 * @return New buffer
	 */
	public static Buffer newBuffer(long byteCount) {
		return currentComputeContext().createBuffer(byteCount);
	}

	/**
	 * Returns a kernel from the current context having the specified name.
	 * If the program has not yet been loaded, a new instance is allocated
	 * using the class default constructor and then loaded.
	 * @param program to create a kernel of
	 * @param kernelName name of the kernel
	 * @return Kernel created from the specified program
	 */
	public static Kernel newKernel(Class<? extends Program> program,
			String kernelName) {
		return currentComputeContext().createKernel(program, kernelName);
	}

	/**
	 * Explicitly unloads the specified program.
	 * @param program to unload
	 */
	public static void unload(Program program) {
		currentComputeContext().unloadAndFree(program);
	}

	/**
	 * Default constructor.
	 */
	protected ComputeContext() {
	}

	/**
	 * Creates a buffer having the specified initial data in this context.
	 * @param init Initial Data
	 * @return Buffer with the specified initial data 
	 */
	protected abstract Buffer createBuffer(java.nio.Buffer init);

	/**
	 * Creates a memory buffer having the specified capacity in this context.
	 * @param byteCount count
	 * @return Buffer with the specified capacity 
	 */
	protected abstract Buffer createBuffer(long byteCount);

	/**
	 * Creates the kernel from the specified program having the specified name
	 * in this context. If the specified program has not yet been loaded 
	 * in this context or a parent context, a new instance is allocated 
	 * using the class default constructor and then loaded in this context.
	 * @param program Program to make a kernel of
	 * @param kernelName Name of the kernel
	 * @return Kernel created from the specified program
	 */
	protected abstract Kernel createKernel(Class<? extends Program> program,
			String kernelName);

	/**
	 * Loads and builds the specified program.
	 * @param program to load and build
	 */
	protected abstract void loadAndBuild(Program program);

	/**
	 * Unloads and free the specified program.
	 * @param program to unload and free
	 */
	protected abstract void unloadAndFree(Program program);

}
