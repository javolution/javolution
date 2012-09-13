/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/osgi/OSGi.hpp"
#include "javolution/internal/osgi/OSGiImpl.hpp"

using namespace javolution::osgi;
using namespace javolution::internal::osgi;

OSGi OSGi_API::newInstance() {
    return new OSGiImpl_API();
}
