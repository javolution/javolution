/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */

#include "javolution/osgi/JavolutionActivator.hpp"

#include "org/osgi/framework/Bundle.hpp"
#include "org/osgi/framework/Constants.hpp"
#include "javolution/log/Logging.hpp"
#include "javolution/lang/String.hpp"
#include "javolution/lang/UnsupportedOperationException.hpp"

using namespace javolution::lang;
using namespace org::osgi::framework;
using namespace javolution::log;
using namespace javolution::osgi;

void JavolutionActivator_API::start(BundleContext const& context) {
    Logging_API::info(L"JAVOLUTION", L"Start Bundle: JAVOLUTION, Version: ", (String) JAVOLUTION_VERSION);

    // Activates the memory cache.
    Object_API::getMemoryCache().enable(true);
}

void JavolutionActivator_API::stop(BundleContext const& context) {
    Logging_API::info(L"JAVOLUTION", L"Stop Bundle: JAVOLUTION, Version: ", (String) JAVOLUTION_VERSION);

    // Deactivates the memory cache.
    Object_API::getMemoryCache().enable(false);

    // There is no need to unregister services in the stop() method.
    // This is done automatically by the framework.
}

void JavolutionActivator_API::checkVersion(javolution::lang::String const& majorVersion, char*) {
	String runtimeVersion = JAVOLUTION_VERSION;
	if (!runtimeVersion->startsWith(majorVersion))
		throw UnsupportedOperationException_API::newInstance(
				L"JAVOLUTION version mismatch. JAVOLUTION Version (headers): " + majorVersion +
				L", Implementation Version: " + runtimeVersion);
}
