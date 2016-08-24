/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.function;

import java.io.Serializable;

import org.javolution.util.function.Order;

/**
 * The lexicographic order implementation.
 */
public class LexicalOrderImpl implements Order<CharSequence>,
		Serializable {

	private static final long serialVersionUID = 0x700L; // Version.
	public static final LexicalOrderImpl INSTANCE = new LexicalOrderImpl(0);

	final int fromIndex;

	/** Creates a lexical order from the specified index.
	 *  Anything before that index is ignored. */
	public LexicalOrderImpl(int fromIndex) {
		this.fromIndex = fromIndex;
	};

	@Override
	public boolean areEqual(CharSequence left, CharSequence right) {
		if (left == right)
			return true;
		if ((left == null) || (right == null))
			return false;
		int n = left.length();
		if (right.length() != n)
			return false;
		for (int i = n; i >= fromIndex;) { // Search from the tail.
			if (left.charAt(--i) != right.charAt(i))
				return false;
		}
		return true;
	}

	@Override
	public int compare(CharSequence left, CharSequence right) {
		if (left == null)
			return -1;
		if (right == null)
			return 1;
		int i = fromIndex;
		int n = Math.min(left.length(), right.length());
		while (n-- > fromIndex) {
			char c1 = left.charAt(i);
			char c2 = right.charAt(i++);
			if (c1 != c2)
				return c1 - c2;
		}
		return left.length() - right.length();
	}

	@Override
	public int indexOf(CharSequence csq) {
		if ((csq == null) || (csq.length() <= fromIndex)) return 0;
		int c = csq.charAt(fromIndex);
		return (c <= 64) ? 0 : c >= 127 ? 63 : c - 64;		
	}
	
	@Override
	public Order<CharSequence> subOrder(CharSequence csq) {
		int i = indexOf(csq);
		if (i == 0) return new Low(fromIndex);
		if (i == 63) return new High(fromIndex);
		return new LexicalOrderImpl(fromIndex+1);
	}
	
	private static class Low extends LexicalOrderImpl {
		private Low(int fromIndex) {
			super(fromIndex);
		}
		@Override
		public int indexOf(CharSequence csq) {
			if ((csq == null) || (csq.length() <= fromIndex)) return 0;
			int c = csq.charAt(fromIndex);
			return c & 63;		
		}
		public int bitLength() {
			return 6;
		}
		@Override
		public Order<CharSequence> subOrder(CharSequence csq) {
			return new LexicalOrderImpl(fromIndex+1);
		}
	}

	private static class High extends LexicalOrderImpl {
		private High(int fromIndex) {
			super(fromIndex);
		}
		@Override
		public int indexOf(CharSequence csq) {
			if ((csq == null) || (csq.length() <= fromIndex)) return 0;
			int c = csq.charAt(fromIndex);
			return c - 127;		
		}
		public int bitLength() {
			return 16;
		}
		@Override
		public Order<CharSequence> subOrder(CharSequence csq) {
			return new LexicalOrderImpl(fromIndex+1);
		}
	}
}
