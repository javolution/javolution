/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javolution.context.LogContext;
import javolution.context.SecurityContext;
import javolution.context.SecurityContext.Permission;
import javolution.context.StorageContext;

/**
 * Holds the default implementation of StorageContext.
 */
public final class StorageContextImpl extends StorageContext {

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Serializable> V read(Resource<V> resource)
            throws SecurityException {
        SecurityContext.check(new Permission<Resource<V>>(Resource.class,
                "write", resource));
        try {
            File file = new File(FILE_STORAGE_LOCATION.get(),
                    resource.uniqueID());
            if (file.exists()) {
                LogContext.debug("Read resource file ", file.getAbsolutePath());
            } else {
                LogContext.debug("Resource file ", file.getAbsolutePath(),
                        " does not exist.");
                return null;
            }

            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            V value = (V) in.readObject();
            in.close();
            fileIn.close();
            return value;
        } catch (IOException e1) {
            LogContext.error(e1);
        } catch (ClassNotFoundException e2) {
            LogContext.error(e2);
        }
        return null;

    }

    @Override
    public <V extends Serializable> void write(Resource<V> resource, V value)
            throws SecurityException {
        SecurityContext.check(new Permission<Resource<V>>(Resource.class,
                "write", resource));
        try {
            File storage = FILE_STORAGE_LOCATION.get();
            storage.mkdirs();
            File file = new File(storage, resource.uniqueID());
            LogContext.debug("Write resource ", file.getAbsolutePath());

            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(value);
            out.close();
            fileOut.close();
        } catch (IOException error) {
            LogContext.error(error);
        }
    }

    @Override
    protected StorageContext inner() {
        return this;
    }

}
