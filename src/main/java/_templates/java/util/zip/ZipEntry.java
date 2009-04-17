/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.java.util.zip;

import _templates.java.lang.UnsupportedOperationException;

public class ZipEntry {

    public String getName() {
        throw new UnsupportedOperationException(
                "ZipEntry operations not supported for J2ME build");
    }

}
