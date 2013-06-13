/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.collection;

import java.util.Iterator;

import javolution.util.FastCollection;
import javolution.util.function.CollectionConsumer;
import javolution.util.function.FullComparator;
import javolution.util.service.CollectionService;

/**
 * A sequential view over a collection.
 */
public final class SequentialCollectionImpl<E> extends FastCollection<E>
        implements CollectionService<E> {

    private static final long serialVersionUID = 7400438782996852037L;
    private final CollectionService<E> target;

    public SequentialCollectionImpl(CollectionService<E> target) {
        this.target = target;
    }

    @Override
    public boolean add(E element) {
        return target.add(element);
    }

    @Override
    public void atomicRead(Runnable action) {
        target.atomicRead(action);
    }

    @Override
    public void atomicWrite(Runnable action) {
        target.atomicWrite(action);        
    }
    
    @Override
    public void forEach(final CollectionConsumer<? super E> consumer) {
        if (consumer instanceof CollectionConsumer.Sequential) {
            target.forEach(consumer);
        } else {
            target.forEach(new SequentialConsumerWrapper<E>(consumer));
        }
    }

    @Override
    public CollectionService<E>[] trySplit(int n) {
        return target.trySplit(n);
    }

    @Override
    public Iterator<E> iterator() {
        return target.iterator();
    }

    @Override
    public FullComparator<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public SequentialCollectionImpl<E> service() {
        return this;
    }
    
    /**
    * A sequential consumer wrapper.
    */
   private static class SequentialConsumerWrapper<E> implements CollectionConsumer.Sequential<E> {
       private final CollectionConsumer<? super E> consumer;

       public SequentialConsumerWrapper(CollectionConsumer<? super E> consumer) {
           this.consumer = consumer;
       }

       @Override
       public void accept(E e, CollectionConsumer.Controller controller) {
           consumer.accept(e, controller);
       }
   }    
}
