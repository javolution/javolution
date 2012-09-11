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

import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.mojo.natives.linker.Manifest;
import org.codehaus.mojo.natives.linker.ManifestConfiguration;
import org.codehaus.mojo.natives.util.CommandLineUtil;
import org.codehaus.mojo.natives.util.EnvUtil;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

public class MSVCManifest
    extends AbstractLogEnabled
    implements Manifest
{
    public void run( ManifestConfiguration config )
        throws NativeBuildException
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "mt.exe" );
        cl.setWorkingDirectory( config.getWorkingDirectory().getPath() );

        cl.createArg().setValue( "-manifest" );
        
        int manifestType = 0;
        
        if ( "EXE".equalsIgnoreCase( FileUtils.getExtension( config.getInputFile().getPath() ) ) )
        {
            manifestType = 1;
        }
        else if ( "DLL".equalsIgnoreCase( FileUtils.getExtension( config.getInputFile().getPath() ) ) )
        {
            manifestType = 2;
        }
        
        if ( manifestType == 0  )
        {
            throw new NativeBuildException( "Unknown manifest input file type: " + config.getInputFile() );
        }
        
        cl.createArg().setFile( config.getManifestFile() );
        cl.createArg().setValue( "-outputresource:" + config.getInputFile() + ";" + manifestType );
        
        EnvUtil.setupCommandlineEnv( cl, config.getEnvFactory() );

        CommandLineUtil.execute( cl, this.getLogger() );
    }
}
