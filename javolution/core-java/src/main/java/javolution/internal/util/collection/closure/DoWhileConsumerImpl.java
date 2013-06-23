/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection.closure;

import javolution.util.function.Consumer;
import javolution.util.function.Predicate;
import javolution.util.service.CollectionService.IterationController;

/**
 * The consumer to perform {@code doWhile(predicate)} collections iterations.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public final class DoWhileConsumerImpl<E> implements Consumer<E>,
        IterationController {

    private final Predicate<? super E> doContinue;
    private volatile boolean terminate;

    public DoWhileConsumerImpl(Predicate<? super E> doContinue) {
        this.doContinue = doContinue;
    }

    @Override
    public boolean doSequential() {
        return false;
    }

    @Override
    public boolean doReversed() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return terminate;
    }

    @Override
    public void accept(E param) {
        if (!doContinue.test(param)) {
            terminate = true;
        }
    }
}
