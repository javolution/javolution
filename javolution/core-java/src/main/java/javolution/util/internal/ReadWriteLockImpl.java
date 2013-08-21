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

/**
 * Simple and efficient read/write lock implementation giving 
 * preferences to writers. Acquiring a write lock then a read lock is 
 * supported. Writers may acquire a read lock after having the write lock
 * but the reverse would result in deadlock.
 */
public final class ReadWriteLockImpl implements ReadWriteLock, Serializable {
    
    /** Read-Lock Implementation. */
    public final class ReadLock implements Lock, Serializable {
        private static final long serialVersionUID = 0x600L; // Version.

        @Override
        public void lock() {
            try {
                lockInterruptibly();
            } catch (java.lang.InterruptedException e) {}
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

    /** Write-Lock Implementation. */
    public final class WriteLock implements Lock, Serializable {
        private static final long serialVersionUID = 0x600L; // Version.

        @Override
        public void lock() {
            try {
                lockInterruptibly();
            } catch (java.lang.InterruptedException e) {}
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

    private static final long serialVersionUID = 0x600L; // Version.
    public final ReadLock readLock = new ReadLock();
    public final WriteLock writeLock = new WriteLock();
    private transient int givenLocks;
    private transient int waitingWriters;
    private transient Thread writerThread;

    @Override
    public ReadLock readLock() {
        return readLock;
    }

    @Override
    public WriteLock writeLock() {
        return writeLock;
    }
}
