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
import javolution.util.service.CollectionService;
import javolution.util.service.ComparatorService;
import javolution.util.service.ConsumerService;

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
    public void forEach(final ConsumerService<? super E> consumer) {
        if (consumer instanceof ConsumerService.Sequential) {
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
    public ComparatorService<? super E> comparator() {
        return target.comparator();
    }

    @Override
    public SequentialCollectionImpl<E> service() {
        return this;
    }
    
    /**
    * A sequential consumer wrapper.
    */
   private static class SequentialConsumerWrapper<E> implements ConsumerService.Sequential<E> {
       private final ConsumerService<? super E> consumer;

       public SequentialConsumerWrapper(ConsumerService<? super E> consumer) {
           this.consumer = consumer;
       }

       @Override
       public void accept(E e, ConsumerService.Controller controller) {
           consumer.accept(e, controller);
       }
   }    
}
