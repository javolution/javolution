/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Integer64.hpp"

using namespace javolution::lang;
using namespace javolution::lang;

Integer64 Integer64_API::BYTES_VALUES[256];

Integer64* Integer64_API::getBytesValues() { // Static.
    for (Type::int64 i=0; i < 256; i++) {
        BYTES_VALUES[i] = Integer64(new Integer64_API(i-128));
    }
    return BYTES_VALUES;
}


