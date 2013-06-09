/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.util.table;

import java.util.Comparator;

import javolution.util.service.TableService;

/**
 * A quick sort utility class.
 *       
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0.0, December 12, 2012
 */
public class QuickSort<E> {
      
    private TableService<E> table;
    private Comparator<? super E> comparator;
   
    public QuickSort(TableService<E> table, Comparator<? super E> comparator) {
        this.table = table;
        this.comparator = comparator;
    }

    public void sort() {
       sort(0, table.size()); 
    }
    
    // From Wikipedia Quick Sort - http://en.wikipedia.org/wiki/Quicksort
    //
    private void sort(int first, int last) {
        if (first < last) {
            int pivIndex = partition(first, last);
            sort(first, (pivIndex - 1));
            sort((pivIndex + 1), last);
        }
    }

    private int partition(int f, int l) {
        int up, down;
        E piv = table.get(f);
        up = f;
        down = l;
        do {
            while (comparator.compare(table.get(up), piv) <= 0 && up < l) {
                up++;
            }
            while (comparator.compare(table.get(down), piv) > 0 && down > f) {
                down--;
            }
            if (up < down) { // Swaps.
                E temp = table.get(up);
                table.set(up, table.get(down));
                table.set(down, temp);
            }
        } while (down > up);
        table.set(f, table.get(down));
        table.set(down, piv);
        return down;
    }

}
