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
import org.codehaus.mojo.natives.compiler.MessageCompilerConfiguration;
import org.codehaus.mojo.natives.compiler.AbstractMessageCompiler;
import org.codehaus.mojo.natives.util.EnvUtil;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

public class MSVCMessageCompiler
    extends AbstractMessageCompiler
{

    protected Commandline getCommandLine( MessageCompilerConfiguration config, File source )
        throws NativeBuildException
    {

        Commandline cl = new Commandline();

        EnvUtil.setupCommandlineEnv( cl, config.getEnvFactory() );

        if ( config.getWorkingDirectory() != null )
        {
            cl.setWorkingDirectory( config.getWorkingDirectory().getPath() );
        }

	if ( config.getExecutable() == null || config.getExecutable().trim().length() == 0 )
	{
  	   config.setExecutable ( "mc.exe" );
	}
	cl.setExecutable(config.getExecutable().trim());

        cl.addArguments( config.getOptions() );

        if ( config.getOutputDirectory() != null && config.getOutputDirectory().getPath().trim().length() != 0 )
        {
            cl.createArg().setValue( "-r" );
            cl.createArg().setValue( config.getOutputDirectory().getPath() );

            cl.createArg().setValue( "-h" );
            cl.createArg().setValue( config.getOutputDirectory().getPath() );

        }

        if ( config.getDebugOutputDirectory() != null
            && config.getDebugOutputDirectory().getPath().trim().length() != 0 )
        {
            cl.createArg().setValue( "-x" );
            cl.createArg().setValue( config.getDebugOutputDirectory().getPath() );
        }

        cl.createArg().setValue( source.getPath() );

        return cl;
    }

}
