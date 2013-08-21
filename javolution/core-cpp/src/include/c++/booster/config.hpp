//
//  Copyright (c) 2010 Artyom Beilis (Tonkikh)
//
//  Distributed under the Boost Software License, Version 1.0. (See
//  accompanying file LICENSE_1_0.txt or copy at
//  http://www.boost.org/LICENSE_1_0.txt)
//
// SVN: http://cppcms.svn.sourceforge.net/viewvc/cppcms/framework/trunk/booster/
//
#ifndef BOOSTER_CONFIG_HPP
#define BOOSTER_CONFIG_HPP

#include "Javolution.hpp"

#define BOOSTER_API JAVOLUTION_DLL

#if (defined(WIN32) || defined(_WIN32) || defined(__WIN32)) && !defined(__CYGWIN__)
#define BOOSTER_WIN_NATIVE
#endif

#if defined(__CYGWIN__)
#define BOOSTER_CYGWIN
#endif

#if defined(BOOSTER_WIN_NATIVE) || defined(BOOSTER_CYGWIN)
#define BOOSTER_WIN32
#endif

#if !defined(BOOSTER_WIN_NATIVE)
#define BOOSTER_POSIX
#endif

#if defined(_MSC_VER)
#define BOOSTER_MSVC
// This warning is really not revevant
#pragma warning (disable: 4275 4251)
#endif


#undef BOOSTER_HAS_CHAR16_T
#undef BOOSTER_HAS_CHAR32_T
#undef BOOSTER_NO_STD_WSTRING
#undef BOOST_NO_SWPRINTF

#ifdef __GNUC__
#  define BOOSTER_GCC
#endif

#if defined(__GNUC__) && __GNUC__ < 4
#  define BOOSTER_GCC3
#endif

#if defined(__CYGWIN__) || (defined(BOOSTER_WIN32) && defined(BOOSTER_GCC3))
#  define BOOSTER_NO_STD_WSTRING
#endif

#if defined(BOOSTER_WIN32) && defined(BOOSTER_GCC)
#  define BOOST_NO_SWPRINTF
#endif


#endif /// BOOSTER_CONFIG_H
