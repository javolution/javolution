package org.codehaus.mojo.natives.msvc;

import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

public class RegQuery
{
    public static String getValue( String valueType, String folderName, String folderKey )
        throws NativeBuildException
    {
        Commandline cl = new Commandline();
        cl.setExecutable( "reg" );
        cl.createArg().setValue( "query" );
        cl.createArg().setValue( folderName );
        cl.createArg().setValue( "/v" );
        cl.createArg().setValue( folderKey );

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        try
        {
            int ok = CommandLineUtils.executeCommandLine( cl, stdout, stderr );

            if ( ok != 0 )
            {
                return null;
            }
        }
        catch ( CommandLineException e )
        {
            throw new NativeBuildException ( e.getMessage(), e );
        }

        String result = stdout.getOutput();

        int p = result.indexOf( valueType );

        if ( p == -1 )
        {
            return null;
        }

        return result.substring( p + valueType.length() ).trim();
    }
}
