package org.codehaus.mojo.natives.msvc;

import org.codehaus.mojo.natives.AbstractEnvFactory;
import org.codehaus.mojo.natives.util.EnvUtil;

public abstract class AbstractMSVCEnvFactory
    extends AbstractEnvFactory
{

    protected static String getProgramFiles()
    {
        return EnvUtil.getEnv( "ProgramFiles", "ProgramFiles", "C:\\Program Files" );
    }

    protected static String getProgramFilesX86()
    {
        return EnvUtil.getEnv( "ProgramFiles(x86)", "ProgramFiles", getProgramFiles() );
    }
    
    protected static String getSystemRoot()
    {
        return EnvUtil.getEnv( "SystemRoot", "SystemRoot", "C:\\WINDOWS" );
    }

}
