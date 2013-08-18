/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.table;

import java.util.Comparator;

import javolution.util.service.TableService;

/**
 * A quick sort utility class.
 * From Wikipedia Quick Sort - http://en.wikipedia.org/wiki/Quicksort
 */
public class QuickSort<E> {

    private final Comparator<? super E> comparator;
    private final TableService<E> table;

    public QuickSort(TableService<E> table, Comparator<? super E> comparator) {
        this.table = table;
        this.comparator = comparator;
    }

    public void sort() {
        int size = table.size();
        if (size > 0) quicksort(0, table.size() - 1);
    }

    public void sort(int first, int last) {
        if (first < last) {
            int pivIndex = partition(first, last);
            sort(first, (pivIndex - 1));
            sort((pivIndex + 1), last);
        }
    }

    // From Wikipedia Quick Sort - http://en.wikipedia.org/wiki/Quicksort
    //
    void quicksort(int first, int last) {
        int pivIndex = 0;
        if (first < last) {
            pivIndex = partition(first, last);
            quicksort(first, (pivIndex - 1));
            quicksort((pivIndex + 1), last);
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
