/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Float32.hpp"

using namespace javolution::lang;
using namespace javolution::lang;

Float32 Float32_API::createStaticZero() {
    return new Float32_API(0.0f);
}
