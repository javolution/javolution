package org.javolution.maven;

import org.codehaus.mojo.natives.msvc.AbstractMSVCEnvFactory;
import java.util.Map;
import org.codehaus.mojo.natives.NativeBuildException;

public class MSVC2015x64EnvFactory extends AbstractMSVCEnvFactory
{

    @SuppressWarnings("rawtypes")
    protected Map createEnvs()
        throws NativeBuildException
    {
        return this.createEnvs( "VS140COMNTOOLS", "amd64" );
    }

}
