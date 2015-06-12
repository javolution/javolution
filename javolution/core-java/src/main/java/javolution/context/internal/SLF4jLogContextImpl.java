/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4jLogContextImpl extends LogContextImpl {
	
	private static final Logger LOG = LoggerFactory.getLogger(SLF4jLogContextImpl.class);
	
	protected Level currentLevel() {
		if(LOG.isDebugEnabled()){
			return Level.DEBUG;
		}
		else if(LOG.isInfoEnabled()){
			return Level.INFO;
		}
		else if(LOG.isWarnEnabled()){
			return Level.WARNING;
		}
		
		return Level.ERROR;
	}
}
