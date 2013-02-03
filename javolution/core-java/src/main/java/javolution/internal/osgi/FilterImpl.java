/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.internal.osgi;

import java.util.Dictionary;
import java.util.Map;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;

/**
 * Filter implementation (value type).
 */
class FilterImpl implements Filter{

    ////////////////////////////////////////////////////////////////////////////
    // Converted from FilterImpl.hpp (C++)
    //

    String filterString;
    String className;


    public FilterImpl(String filterString) {
        this.filterString = filterString;

        // Assumes: "(<Constants.OBJECTCLASS>=<className>)"
        int classNameStartIndex = Constants.OBJECTCLASS.length() + 2;
        this.className = filterString.substring(classNameStartIndex, filterString.length()-1);
    }

    @Override
    public boolean match(ServiceReference<?> reference) {
        ServiceReferenceImpl<?> sri = (ServiceReferenceImpl<?>) reference;
        return sri.serviceName.equals(className);
    }

    @Override
    public String toString() {
        return filterString;
    }

    // End conversion.
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean match(Dictionary<String, ? > dctnr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean matchCase(Dictionary<String, ? > dctnr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean matches(Map<String, ?> map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}