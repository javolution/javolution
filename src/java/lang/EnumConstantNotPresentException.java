/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package java.lang;

public class EnumConstantNotPresentException extends RuntimeException {
    private Class _enumType;

    private String _constantName;

    public EnumConstantNotPresentException(Class enumType, String constantName) {
        super(enumType.getName() + "." + constantName);
        _enumType = enumType;
        _constantName = constantName;
    }

    public Class enumType() {
        return _enumType;
    }

    public String constantName() {
        return _constantName;
    }
}