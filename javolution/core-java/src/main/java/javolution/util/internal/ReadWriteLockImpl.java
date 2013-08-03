/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javolution.context.LogContext;

/**
 * Simple and efficient read/write lock implementation giving preferences 
 * to writers.
 */
public final class ReadWriteLockImpl implements ReadWriteLock, Serializable {
    private static final long serialVersionUID = 0x600L; // Version.
    private final ReadLock readLock = new ReadLock();
    private final WriteLock writeLock = new WriteLock();
    private int givenLocks; 
    private int waitingWriters;
    private Thread writerThread;

    public final class ReadLock implements Lock, Serializable {
        private static final long serialVersionUID = 0x600L; // Version.

        @Override
        public void lock() {
            try {
                lockInterruptibly();
            } catch (java.lang.InterruptedException e) {
                LogContext.error(e);
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            synchronized (ReadWriteLockImpl.this) {
                if (writerThread == Thread.currentThread()) return; // Current thread has the writer lock.
                while ((writerThread != null) || (waitingWriters != 0)) {
                    ReadWriteLockImpl.this.wait();
                }
                givenLocks++;
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit)
                throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unlock() {
            synchronized (ReadWriteLockImpl.this) {
                if (writerThread == Thread.currentThread()) return; // Itself is the writing thread.
                assert (givenLocks > 0);
                givenLocks--;
                ReadWriteLockImpl.this.notifyAll();
            }
        }

    }

    public final class WriteLock implements Lock, Serializable {
        private static final long serialVersionUID = 0x600L; // Version.

        @Override
        public void lock() {
            try {
                lockInterruptibly();
            } catch (java.lang.InterruptedException e) {
                LogContext.error(e);
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            synchronized (ReadWriteLockImpl.this) {
                waitingWriters++;
                while (givenLocks != 0) {
                    ReadWriteLockImpl.this.wait();
                }
                waitingWriters--;
                writerThread = Thread.currentThread();
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit)
                throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unlock() {
            synchronized (ReadWriteLockImpl.this) {
                writerThread = null;
                ReadWriteLockImpl.this.notifyAll();
            }
        }

    }

    @Override
    public ReadLock readLock() {
        return readLock;
    }

    @Override
    public WriteLock writeLock() {
        return writeLock;
    }
}
