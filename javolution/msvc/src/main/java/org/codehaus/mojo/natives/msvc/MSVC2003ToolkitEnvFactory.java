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
 * Equivalent of Microsoft Visual C++ Toolkit 2003's vcvars32.bat
 *
 */

public class MSVC2003ToolkitEnvFactory
    extends AbstractMSVCEnvFactory
{
    private static final String MSVC2003_TOOLKIT_INSTALL_ENV_KEY = "MSVC2003_TOOLKIT_INSTALL_DIR";

    private static final String DEFAULT_MSVC2003_TOOLKT_INSTALL_DIR = getProgramFiles() + "/Microsoft Visual C++ Toolkit 2003";

    protected Map createEnvs()
        throws NativeBuildException
    {
        File vcInstallDir = new File( EnvUtil.getEnv( MSVC2003_TOOLKIT_INSTALL_ENV_KEY,
                                                      MSVC2003_TOOLKIT_INSTALL_ENV_KEY,
                                                      DEFAULT_MSVC2003_TOOLKT_INSTALL_DIR ) );

        if ( !vcInstallDir.isDirectory() )
        {
            throw new NativeBuildException( vcInstallDir.getPath() + " is not a directory." );
        }

        Map envs = new HashMap();

        //setup new PATH
        String currentPath = System.getProperty( "java.library.path" );

        String newPath = vcInstallDir.getPath() + "\\BIN;" + currentPath;

        envs.put( "PATH", newPath );

        //setup new INCLUDE PATH
        String currentIncludePath = EnvUtil.getEnv( "INCLUDE" );

        String newIncludePath = vcInstallDir.getPath() + "\\INCLUDE;" + currentIncludePath;

        envs.put( "INCLUDE", newIncludePath );

        //
        //setup new LIB PATH
        //
        String currentLibPath = EnvUtil.getEnv( "LIB" );

        String newLibPath = vcInstallDir.getPath() + "\\LIB;" + currentLibPath;

        envs.put( "LIB", newLibPath );

        return envs;

    }

}
