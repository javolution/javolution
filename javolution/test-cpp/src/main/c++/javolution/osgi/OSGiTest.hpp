/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 */
#ifndef _JAVOLUTION_OSGI_OSGI_TEST_HPP
#define _JAVOLUTION_OSGI_OSGI_TEST_HPP

#include "junit/framework/TestCase.hpp"
#include "javolution/osgi/ActivatorA.hpp"
#include "javolution/osgi/ActivatorB.hpp"
#include "javolution/osgi/JavolutionActivator.hpp"
#include "javolution/osgi/OSGi.hpp"

namespace javolution {
    namespace osgi {
        class OSGiTest_API;
    }
}
using namespace javolution::lang;
using namespace javolution::util;
using namespace org::osgi::framework;
using namespace javolution::osgi;


/**
 * OSGi Tests.
 */
class javolution::osgi::OSGiTest_API : public junit::framework::TestCase_API {
public:
    BEGIN_SUITE(OSGiTest_API)
    ADD_TEST(testActivator)
    END_SUITE()

    void testActivator() {
        assertNull(JAVOLUTION_LINE_INFO, ActivatorB_API::getServiceA());
        assertNull(JAVOLUTION_LINE_INFO, ActivatorA_API::getServiceB());

        ActivatorA activatorA = ActivatorA_API::newInstance();
        osgi->start(L"Bundle A", activatorA);

        assertNull(JAVOLUTION_LINE_INFO, ActivatorB_API::getServiceA());
        assertNull(JAVOLUTION_LINE_INFO, ActivatorA_API::getServiceB());

        ActivatorB activatorB = ActivatorB_API::newInstance();
        osgi->start(L"Bundle B", activatorB);

        assertNotNull(JAVOLUTION_LINE_INFO,ActivatorB_API::getServiceA());
        assertNotNull(JAVOLUTION_LINE_INFO, ActivatorA_API::getServiceB());
        
        osgi->stop(L"Bundle A");

        assertNull(JAVOLUTION_LINE_INFO, ActivatorB_API::getServiceA());
        assertNull(JAVOLUTION_LINE_INFO, ActivatorA_API::getServiceB());

        osgi->stop(L"Bundle B");

        assertNull(JAVOLUTION_LINE_INFO, ActivatorB_API::getServiceA());
        assertNull(JAVOLUTION_LINE_INFO, ActivatorA_API::getServiceB());
    }

protected:
    void setUp() {
        osgi = OSGi_API::newInstance();
        osgi->start(L"Javolution", JavolutionActivator_API::newInstance());
    }
    void tearDown() {
        osgi->stop(L"Javolution");
    }

private:
    OSGi osgi;
};
#endif
