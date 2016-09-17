/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package org.javolution.util.internal.function;

import org.javolution.util.function.Order;
/**
 * The lexicographic order implementation. Enum-based singleton, ref. Effective
 * Java Reloaded (Joshua Bloch).
 */
public enum CaseInsensitiveLexicalOrderImpl implements Order<CharSequence> {
	INSTANCE(0), INSTANCE_2(2), INSTANCE_4(4), INSTANCE_6(6), 
	INSTANCE_8(8), INSTANCE_10(10), INSTANCE_12(12), INSTANCE_14(14), 
	INSTANCE_16(16), INSTANCE_18(18), INSTANCE_20(20), INSTANCE_22(22),
	INSTANCE_24(24), INSTANCE_26(26), INSTANCE_28(28), INSTANCE_30(30);

	private final Helper helper;

	/**
	 * Creates a lexical order from the specified index. Anything before that
	 * index is ignored.
	 */
	private CaseInsensitiveLexicalOrderImpl(int fromIndex) {
		this.helper = new Helper(fromIndex);
	};

	@Override
	public boolean areEqual(CharSequence left, CharSequence right) {
		return helper.areEqual(left, right);
	}

	@Override
	public int compare(CharSequence left, CharSequence right) {
		return helper.compare(left, right);
	}

	@Override
	public int indexOf(CharSequence csq) {
		return helper.indexOf(csq);
	}

	@Override
	public Order<CharSequence> subOrder(CharSequence csq) {
		return helper.subOrder(csq);
	}

	/** Actual implementation **/
	private static class Helper implements Order<CharSequence> {
		private static final long serialVersionUID = 0x700L; // Version.
		private final int fromIndex;

		private Helper(int fromIndex) {
			this.fromIndex = fromIndex;
		}

		@Override
		public boolean areEqual(CharSequence left, CharSequence right) {
			if (left == right)
				return true;
			if ((left == null) || (right == null))
				return false;
			int n = left.length();
			if (right.length() != n)
				return false;
			for (int i = n; i > fromIndex;) { // Search from the tail.
				if (Character.toUpperCase(left.charAt(--i)) != Character.toUpperCase(right.charAt(i)))
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
				char c1 = Character.toUpperCase(left.charAt(i));
				char c2 = Character.toUpperCase(right.charAt(i++));
				if (c1 != c2)
					return c1 - c2;
			}
			return left.length() - right.length();
		}

		@Override
		public int indexOf(CharSequence csq) {
			if (csq == null)
				return 0;
			int length = csq.length();
			int j = fromIndex;
			int i = (j < length) ? Character.toUpperCase(csq.charAt(j++)) : 0;
			i <<= 16;
			i |= (j < length) ? Character.toUpperCase(csq.charAt(j++)) : 0;
			return i;
		}

		@Override
		public Order<CharSequence> subOrder(CharSequence csq) {
			return fromIndex < 30 ? LexicalOrderImpl.values()[fromIndex >> 1] : new Helper(fromIndex + 2);
		}

	}

}
