/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_UTIL_DICTIONARY_HPP
#define _JAVOLUTION_UTIL_DICTIONARY_HPP

#include "javolution/lang/String.hpp"
#include "javolution/util/FastMap.hpp"

namespace javolution {
    namespace util {
        typedef javolution::util::FastMap<javolution::lang::String, javolution::lang::String> Dictionary;
        typedef javolution::util::FastMap_API<javolution::lang::String, javolution::lang::String> Dictionary_API;
    }
}

/**
 * This class is obsolete and should be replaced by the Map interface
 * (this dictionary is a rename of javolution::util::FastMap<String,String>).
 *
 * @see  <a href="http://java.sun.com/javase/6/docs/api/java/lang/Dictionary.html">
 *       Java - Dictionary</a>
 * @version 1.0
 */


#endif
