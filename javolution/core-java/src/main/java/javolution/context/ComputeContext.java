/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2007 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.osgi.internal.OSGiServices;

/**
 * <p> A context for executing kernels functions accelerated 
 *     through the use of OpenCL.</p>
 *     
 * <p> For example, the following calculates the sum of the specified 
 *     vectors.</p>
 * [code]
 * static final String VECTOR_MATH_OPEN_CL = 
 *    "__kernel void sum(__global const float *a, __global const float *b, __global float *c) {" +
 *    "    int gid = get_global_id(0);" +
 *    "    c[gid] = a[gid] * b[gid];" +
 *    "}";
 * float[] arrayX = { 1.0f, 2.0f, 3.0f };
 * float[] arrayY = { 3.0f, 2.0f, 1.0f };
 * float[] arrayZ; // Z = X + Y
 * ComputeContext ctx = ComputeContext.enter();
 * try {
 *     Program vectorMath = ctx.newProgram(VECTOR_MATH_OPEN_CL);
 *     Kernel sum = vectorMath.newKernel("sum");
 *     Buffer x = ctx.newBuffer(FloatBuffer.wrap(arrayX));
 *     Buffer y = ctx.newBuffer(FloatBuffer.wrap(arrayY));
 *     Buffer z = ctx.newBuffer(arrayX.length * 4);
 *     ...
 *         // Somewhere in inner function.
 *         sum.setArguments(x, y, z);
 *         sum.execute(arrayX.length); 
 *     ...
 *     arrayZ = z.read().asFloatBuffer().array(); // Read back buffer.
 *     System.out.println("Result: " + java.util.Arrays.toString(arrayZ));
 * } finally {
 *     ctx.exit(); // Unload programs, release buffers.
 * }
 * [/code]
 *     
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 * @see <a href="http://en.wikipedia.org/wiki/OpenCL">Wikipedia: OpenCL</a>
 */
public abstract class ComputeContext extends AbstractContext {

    /**
     * A program which can be loaded/compiled on a device.
     */
    public interface Program {
    
        /**
         * Returns a new kernel having the specified name.
         */
        Kernel newKernel(String name);
      
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
         * Executes this kernel for the specified global work size. 
         */
        void execute(int globalWorkSize);
      
        /**
         * Executes this kernel for the specified global work size and 
         * local work size. 
         */
        void execute(int globalWorkSize, int localWorkSize);
        
    }
    
    /**
     * A high speed memory buffer which can be created on a device. 
     */
    public interface Buffer{        
        
    }

    /**
     * Default constructor.
     */
    protected ComputeContext() {}

    /**
     * Enters and returns a new processing context instance.
     */
    public static ComputeContext enter() {
        ComputeContext ctx = current(ComputeContext.class);
        if (ctx == null) { // Root.
            ctx = OSGiServices.getComputeContext();
        }
        return (ComputeContext) ctx.enterInner();
    }

    /**
     * Indicates if OpenCL is supported by the platform.
     */
    public abstract boolean isOpenCLSupported();
    
    /**
     * Returns a new program loaded and built from the specified OpenCL 
     * source code.
     * 
     * @throws UnsupportedOperationException if OpenCL is not 
     *         {@link #isOpenCLSupported() supported}
     */
    public abstract Program newProgram(String openCL) throws UnsupportedOperationException;
    
    /**
     * Creates a new memory buffer on the processing device.
     * 
     * @throws UnsupportedOperationException if OpenCL is not 
     *         {@link #isOpenCLSupported() supported}
     */
    public abstract Buffer newBuffer(java.nio.Buffer data);
    
    /**
     * Exits the scope of this processing context; any program loaded is unloaded
     * and allocated buffers are released.
     * 
     * @throws IllegalStateException if this context is not the current 
     *         context.
     */
    @Override
    public void exit() { // Redefine here for documentation purpose.
        super.exit();
    }    
   
}
