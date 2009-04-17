/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package _templates.java.util.zip;


import java.util.Enumeration;

import _templates.java.lang.UnsupportedOperationException;

public class ZipFile {

    public ZipFile(String fileName) {
    }

    public Enumeration entries() {
        throw new UnsupportedOperationException(
                "ZipFile operations not supported for J2ME build");
    }

}
