/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.osgi.internal;

import javolution.context.ComputeContext;
import javolution.context.ComputeContext.Program;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Service to load/unload OpenCL programs.
 */
public final class ProgramTrackerImpl extends ServiceTracker<Program, Program> {

	public ProgramTrackerImpl(BundleContext bc) {
		super(bc, Program.class, null);
	}

	public Program addingService(ServiceReference<Program> reference) {
		Program program = super.addingService(reference);
		ComputeContext.load(program);
		return program;
	}

	public void removedService(ServiceReference<Program> reference,
			Program program) {
		super.removedService(reference, program);
		ComputeContext.unload(program);
	}
}
