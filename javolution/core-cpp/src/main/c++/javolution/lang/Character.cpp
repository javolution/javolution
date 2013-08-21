/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/lang/Character.hpp"

using namespace javolution::lang;

Character ASCII_CHARACTERS[128];

Character* Character_API::getASCIICharacters() { // Static.
    for (Type::int32 i=0; i < 128; i++) {
    	ASCII_CHARACTERS[i] = new Character_API((Type::wchar) i);
    }
    return ASCII_CHARACTERS;
}

