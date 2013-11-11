/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;
import static org.jocl.CL.clReleaseKernel;
import static org.jocl.CL.clReleaseMemObject;
import static org.jocl.CL.clReleaseProgram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jocl.CL;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

import javolution.context.LogContext;
import javolution.context.ComputeContext;
import javolution.context.SecurityContext;
import javolution.context.SecurityContext.Permission;
import javolution.context.StorageContext;
import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * Holds the default implementation of compute context.
 */
public final class ComputeContextImpl extends ComputeContext {

    /** Program OpenCL implementation. */
    class ProgramImpl implements Program {
        cl_program openCL;
 
        @Override
        public Kernel newKernel(String name) {
            KernelImpl kernel = new KernelImpl();
            kernel.openCL = clCreateKernel(openCL, name, null);
            return kernel;
        }
    }
    
    /** Kernel OpenCL implementation. */
    class KernelImpl implements Kernel {
        cl_kernel openCL;

        @Override
        public void setArguments(Object... args) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void execute(int globalWorkSize) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void execute(int globalWorkSize, int localWorkSize) {
            // TODO Auto-generated method stub
            
        }
    }
    
    /** Buffer OpenCL implementation. */
    class BufferImpl implements Buffer {
        cl_mem openCL;
    }

    private FastTable<ProgramImpl> programs = new FastTable<ProgramImpl>();
    private FastMap<String, KernelImpl> kernels = new FastMap<String, KernelImpl>();
    private FastTable<BufferImpl> buffers = new FastTable<BufferImpl>();
    private cl_context context;
    private cl_command_queue commandQueue;
    
    public ComputeContextImpl() {
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        try {
            CL.setExceptionsEnabled(true);
        } catch (Throwable error) {
            LogContext.warning("Cannot load OpenCL Driver");
            return;
        }

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device}, 
            null, null, null);
        
        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);
    }
       
    @Override
    protected ComputeContext inner() {
        return this;
    }

    @Override
    public boolean isOpenCLSupported() {
        return context != null;
    }

    @Override
    public Program newProgram(String openCL) throws UnsupportedOperationException {
        ProgramImpl program = new ProgramImpl();
        program.openCL = clCreateProgramWithSource(context,
                1, new String[]{ openCL }, null, null);
        clBuildProgram(program.openCL, 0, null, null, null, null);
        return program;
    }

    @Override
    public Buffer newBuffer(java.nio.Buffer data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void exit() {
        super.exit();
        // Release kernel, program, and memory objects
        for (BufferImpl buffer : buffers) {
            clReleaseMemObject(buffer.openCL);                    
        }
        for (KernelImpl kernel : kernels.values()) {
            clReleaseKernel(kernel.openCL);                    
        }
        for (ProgramImpl program : programs) {
            clReleaseProgram(program.openCL);                    
        }
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }    

}
