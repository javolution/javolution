/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Integer32.hpp"

using namespace javolution::lang;
using namespace javolution::lang;

Integer32 Integer32_API::BYTES_VALUES[256];

Integer32* Integer32_API::getBytesValues() { // Static.
    for (Type::int32 i=0; i < 256; i++) {
        BYTES_VALUES[i] = Integer32(new Integer32_API(i-128));
    }
    return BYTES_VALUES;
}
