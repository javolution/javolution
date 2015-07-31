/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.xml.internal.annotation;

import java.util.Iterator;

import javolution.text.CharArray;
import javolution.util.FastSet;
import javolution.util.FastTable;
import javolution.util.function.Equalities;

public class AnnotationStackData {
	
	final AnnotationStackType _annotationStackType;
	final FastTable<Object> _list;
	final Object _object;
	final AnnotationStackData _parent;
	final Class<?> _type;
	final CharArray _xmlElementName;
	final Iterator<CharArray> _propOrderIterator;
	
	FastSet<CharArray> _processedSet;
	FastSet<CharArray> _requiredSet;

	public AnnotationStackData(final AnnotationStackType annotationStackType, final AnnotationStackData parent, 
			final Object object, final FastTable<Object> list, final Class<?> type, final CharArray xmlElementName, 
			final FastSet<CharArray> requiredSet, final Iterator<CharArray> propOrderIterator){
		_annotationStackType = annotationStackType;
		_object = object;
		_parent = parent;
		_type = type;
		_xmlElementName = xmlElementName;
		_list = list;
		_requiredSet = requiredSet;
		_propOrderIterator = propOrderIterator;
		
		if(requiredSet == null){
			_processedSet = null;
		}
		else {
			_processedSet = new FastSet<CharArray>(Equalities.CHAR_ARRAY_FAST);
		}
	}

}
