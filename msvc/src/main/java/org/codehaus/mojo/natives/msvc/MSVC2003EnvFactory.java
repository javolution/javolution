package org.codehaus.mojo.natives.msvc;

/*
 * The MIT License
 *
 * Copyright (c) 2004, The Codehaus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.mojo.natives.util.EnvUtil;

/**
 * Equivalent of Microsoft Visual Studio .NET 2003's vcvars32.bat
 */

public class MSVC2003EnvFactory
    extends AbstractMSVCEnvFactory
{
    private static final String MSVS2003_INSTALL_ENV_KEY = "MSVS2003_INSTALL_DIR";

    private static final String DEFAULT_MSVS2003_INSTALL_DIR = getProgramFiles() + "/Microsoft Visual Studio .NET 2003";

    protected Map createEnvs()
        throws NativeBuildException
    {
        File vcInstallDir = new File( EnvUtil.getEnv( MSVS2003_INSTALL_ENV_KEY, MSVS2003_INSTALL_ENV_KEY,
                                                      DEFAULT_MSVS2003_INSTALL_DIR ) );

        if ( !vcInstallDir.isDirectory() )
        {
            throw new NativeBuildException( vcInstallDir.getPath() + " is not a directory." );
        }

        Map envs = new HashMap();

        File vsInstallDir = new File( vcInstallDir.getPath() + "/Common7/IDE" );

        //TODO get winhome dir
        File frameworkDir = new File( getSystemRoot() + "/Microsoft.NET/Framework" );
        envs.put( "FrameworkDir", frameworkDir.getPath() );

        File frameworkSDKDir = new File( vcInstallDir.getPath() + "/SDK/v1.1" );
        envs.put( "FrameworkSDKDir", frameworkSDKDir.getPath() );

        String frameworkVersion = "v1.1.4322";
        envs.put( "frameworkVersion", frameworkVersion );

        File devEnvDir = vsInstallDir;

        File msvcDir = new File( vcInstallDir.getPath() + "/VC7" );

        //setup new PATH
        String currentPath = System.getProperty( "java.library.path" );

        String newPath = devEnvDir.getPath() + ";" + msvcDir.getPath() + "\\BIN;" + vcInstallDir.getPath()
            + "\\Common7\\Tools;" + vcInstallDir.getPath() + "\\Common7\\Tools\\bin\\prerelease;"
            + vcInstallDir.getPath() + "\\Common7\\Tools\\bin;" + frameworkSDKDir.getPath() + "\\bin;"
            + frameworkDir.getPath() + "\\" + frameworkVersion + ";" + currentPath;

        envs.put( "PATH", newPath );

        //setup new INCLUDE PATH
        String currentIncludePath = EnvUtil.getEnv( "INCLUDE" );

        String newIncludePath = msvcDir.getPath() + "\\ATLMFC\\INCLUDE;" + msvcDir.getPath() + "\\INCLUDE;"
            + msvcDir.getPath() + "\\PlatformSDK\\include\\prerelease;" + msvcDir.getPath() + "\\PlatformSDK\\include;"
            + frameworkSDKDir.getPath() + "\\include;" + currentIncludePath;

        envs.put( "INCLUDE", newIncludePath );

        //
        //setup new LIB PATH
        //
        String currentLibPath = EnvUtil.getEnv( "LIB" );

        String newLibPath = msvcDir.getPath() + "\\ATLMFC\\LIB;" + msvcDir.getPath() + "\\LIB;" + msvcDir.getPath()
            + "\\PlatformSDK\\lib\\prerelease;" + msvcDir.getPath() + "\\PlatformSDK\\lib;" + currentLibPath;

        envs.put( "LIB", newLibPath );

        return envs;

    }

}
