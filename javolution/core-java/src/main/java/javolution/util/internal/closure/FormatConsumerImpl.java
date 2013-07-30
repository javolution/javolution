/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util.internal.closure;

import java.io.IOException;

import javolution.text.TextContext;
import javolution.text.TextFormat;
import javolution.util.function.Consumer;
import javolution.util.service.CollectionService.IterationController;

/**
 * The consumer to format the collection element into an appendable.
 * 
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
public final class FormatConsumerImpl implements Consumer<Object>,
        IterationController {

    private final Appendable appendable;
    private volatile IOException ioException;
    private boolean isFirst = true;
    private TextFormat<Object> textFormat;
    private Class<?> type;

    public FormatConsumerImpl(Appendable appendable) {
        this.appendable = appendable;
    }

    public IOException ioException() {
        return ioException;
    }

    @Override
    public boolean doSequential() {
        return true; // Iteration order is important.
    }

    @Override
    public boolean doReversed() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return ioException != null;
    }

    @Override
    public void accept(Object obj) {
        try {
            if (!isFirst) {
                appendable.append(", ");
            } else {
                isFirst = false;
            }
            if (obj != null) {
                if ((type == null) || (!type.equals(obj.getClass()))) {
                    type = obj.getClass();
                    textFormat = TextContext.getFormat(type);
                }
                if (textFormat == null) { // No format associated.
                    appendable.append(obj.toString());
                } else {
                    textFormat.format(obj, appendable);
                }
            } else {
                appendable.append("null");
            }
        } catch (IOException error) {
            ioException = error;
        }
    }
}
