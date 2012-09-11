package org.codehaus.mojo.natives.msvc;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.codehaus.mojo.natives.compiler.CompilerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.cli.Commandline;

public class MSVCCompilerTest
    extends PlexusTestCase
{
    private MSVCCompiler compiler;

    private CompilerConfiguration config;

    private static File sourceFile = new File( "source.c" );

    private static File objectFile = new File( "object.obj" );

    private static String simpleArgv = "/Foobject.obj -c source.c";

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.compiler = new MSVCCompiler();
        this.config = new CompilerConfiguration();
    }

    public void testSimpleCompilation()
        throws Exception
    {
        Commandline cl = compiler.getCommandLine( sourceFile, objectFile, config );
        assertTrue(  StringUtils.contains( cl.toString(),  "cl.exe " + simpleArgv ) );
    }
}
