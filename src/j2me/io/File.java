/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2005 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package j2me.io;

import j2me.lang.UnsupportedOperationException;

/**
 *  Class provided for the sole purpose of compiling the FileFilter and 
 *  FilenameFilter interfaces.
 */
public class File {

    public static final String separator = System.getProperty("file.separator");

    public static final char separatorChar = separator.charAt(0);

    public static final String pathSeparator = System
            .getProperty("path.separator");

    public static final char pathSeparatorChar = pathSeparator.charAt(0);

    private String _path;

    public File(String path) {
        _path = path;
    }

    public String getPath() {
        return _path;
    }
    
    public boolean exists() {
        throw new UnsupportedOperationException(
                "File operations not supported for J2ME build");
    }

    public boolean isDirectory() {
        throw new UnsupportedOperationException(
                "File operations not supported for J2ME build");
    }

    public String getName() {
        throw new UnsupportedOperationException(
                "File operations not supported for J2ME build");
    }

    public File[] listFiles() {
        throw new UnsupportedOperationException(
                "File operations not supported for J2ME build");
    }
}