/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Float64.hpp"

using namespace javolution::lang;
using namespace javolution::lang;


Float64 Float64_API::createStaticZero() { // Static.
    return Float64(new Float64_API(0.0f));
}
