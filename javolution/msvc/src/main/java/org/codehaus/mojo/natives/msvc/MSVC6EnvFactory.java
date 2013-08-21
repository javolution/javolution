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
 * Equivalent of MSVC6's vcvars32.bat
 *
 */

public class MSVC6EnvFactory
    extends AbstractMSVCEnvFactory
{
    private static final String MSVS6_INSTALL_ENV_KEY = "MSVS6_INSTALL_DIR";

    private static final String DEFAULT_MSVS6_INSTALL_DIR = getProgramFilesX86() + "/Microsoft Visual Studio";

    protected Map createEnvs()
        throws NativeBuildException
    {
        File vsDir = new File( EnvUtil.getEnv( MSVS6_INSTALL_ENV_KEY, MSVS6_INSTALL_ENV_KEY, DEFAULT_MSVS6_INSTALL_DIR ) );

        if ( !vsDir.isDirectory() )
        {
            throw new NativeBuildException( vsDir.getPath() + " is not a directory." );
        }

        Map envs = new HashMap();

        String vcOsDir = "WINNT";

        String winDir = EnvUtil.getEnv( "windir" );

        File vsCommonDir = new File( vsDir + "/Common" );

        File vsCommonToolDir = new File( vsCommonDir + "/TOOLS" );

        File msDevDir = new File( vsCommonDir + "/msdev98" );

        File msvcDir = new File( vsDir + "/VC98" );

        envs.put( "MSVCDir", msvcDir.getPath() );

        //setup new PATH
        String currentPath = System.getProperty( "java.library.path" );

        String newPath = msDevDir.getPath() + "\\BIN;" + msvcDir.getPath() + "\\BIN;" + vsCommonToolDir.getPath()
            + "\\" + vcOsDir + ";" + vsCommonToolDir.getPath() + ";" + winDir + ";" + currentPath;

        envs.put( "PATH", newPath );

        //setup new INCLUDE PATH
        String currentIncludePath = EnvUtil.getEnv( "INCLUDE" );

        String newIncludePath = msvcDir.getPath() + "\\ATL\\INCLUDE;" + msvcDir.getPath() + "\\INCLUDE;"
            + msvcDir.getPath() + "\\MFC\\INCLUDE;" + vsCommonToolDir.getPath() + vcOsDir + ";"
            + vsCommonToolDir.getPath() + ";" + currentIncludePath;

        envs.put( "INCLUDE", newIncludePath );

        //
        //setup new LIB PATH
        //
        String currentLibPath = EnvUtil.getEnv( "LIB" );

        String newLibPath = msvcDir.getPath() + "\\LIB;" + msvcDir.getPath() + "\\MFC\\LIB;" + currentLibPath;

        envs.put( "LIB", newLibPath );

        return envs;

    }

}
