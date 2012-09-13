package org.codehaus.mojo.natives.msvc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.mojo.natives.linker.LinkerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.cli.Commandline;

public class MSVCLinkerTest
    extends PlexusTestCase
{
    private MSVCLinker linker;

    private LinkerConfiguration config;

    private static final File objectFile0 = new File( "source1.obj" );

    private static final File objectFile1 = new File( "source2.obj" );

    private List defautlObjectFiles;

    private String basedir;

    public void setUp()
        throws Exception
    {
        super.setUp();

        this.defautlObjectFiles = new ArrayList();
        this.defautlObjectFiles.add( objectFile0 );
        this.defautlObjectFiles.add( objectFile1 );

        this.linker = new MSVCLinker();
        this.config = new LinkerConfiguration();
        this.basedir = getBasedir();
        config.setWorkingDirectory( new File( basedir ) );
        config.setOutputDirectory( new File( basedir, "target" ) );
        config.setOutputFileExtension( "exe" );
        config.setOutputFileName( "test" );
    }

    public void testDefaultLinkerExecutable()
        throws Exception
    {
        Commandline cl = this.getCommandline();

      	assertTrue( cl.getExecutable().endsWith("link.exe") );

        assertEquals( basedir, cl.getWorkingDirectory().getPath() );

        System.out.println( cl.toString() );
    }

    public void testSimpleLinkerCommand()
        throws Exception
    {
        Commandline cl = this.getCommandline();

        assertTrue(  StringUtils.contains( cl.toString(), "link.exe /out:" + config.getOutputFile() + " source1.obj source2.obj" ) );
    }

    /////////////////////////// HELPERS //////////////////////////////////////
    private Commandline getCommandline()
        throws NativeBuildException
    {
        return this.linker.createLinkerCommandLine( defautlObjectFiles, config );
    }

 
}
