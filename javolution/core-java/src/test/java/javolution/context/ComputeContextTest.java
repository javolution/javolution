/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import java.nio.DoubleBuffer;

import javolution.context.ComputeContext.Buffer;
import javolution.context.ComputeContext.Kernel;
import javolution.context.ComputeContext.Program;
import javolution.text.TextBuilder;

/**
 * Validation and performance tests of ComputeContext.
 */
public class ComputeContextTest {

	public static void main(String... args) {
		ComputeContextTest ctt = new ComputeContextTest();
		ctt.testGPU();
	}

	public static class VectorMath implements Program {
		public String toOpenCL() {
			return "__kernel void sum(__global const double *a, __global const double *b, __global double *c) {"
					+ "    int gid = get_global_id(0);"
					+ "    c[gid] = a[gid] + b[gid];" + "}";
		}
	}

	static class GPUVector {
		private final Buffer buffer;

		public GPUVector(double... elements) {
			buffer = ComputeContext.newBuffer(DoubleBuffer.wrap(elements));
		}

		private GPUVector(int length) {
			buffer = ComputeContext.newBuffer(length * 8L); // Size in bytes.
		}

		GPUVector plus(GPUVector that) {
			if (this.length() != that.length())
				throw new IllegalArgumentException("Dimension mismatch");
			GPUVector result = new GPUVector(this.length());
			Kernel sum = ComputeContext.newKernel(VectorMath.class, "sum");
			sum.setArguments(this.buffer.readOnly(), that.buffer.readOnly(),
					result.buffer);
			sum.setGlobalWorkSize(length()); // Number of work-items.
			sum.execute(); // Executes in parallel with others kernels.
			return result;
		}

		public GPUVector export() {
			buffer.export();
			return this;
		}

		public int length() {
			return (int) buffer.getByteCount() / 8;
		}

		public String toString() {
			DoubleBuffer db = buffer.asDoubleBuffer();
			TextBuilder tb = new TextBuilder("{ ");
			for (int i = 0; i < db.capacity(); i++) {
				if (i != 0)
					tb.append(", ");
				tb.append(db.get(i));
			}
			return tb.append(" }").toString();
		}
	}

	public void testGPU() {
		GPUVector sumXYZ;
		ComputeContext ctx = ComputeContext.enter();
		try {
			GPUVector x = new GPUVector(1.0, 2.0, 3.0);
			GPUVector y = new GPUVector(1.0, 2.0, 3.0);
			GPUVector z = new GPUVector(1.0, 2.0, 3.0);
			sumXYZ = x.plus(y).plus(z).export();
		} finally {
			ctx.exit();
		}
		LogContext.info("Result: ", sumXYZ);
	}

}
