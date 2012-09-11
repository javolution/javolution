package org.codehaus.mojo.natives.msvc;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.Os;

public class RegQueryTest
    extends PlexusTestCase
{
    public void testRegQuery()
    {
        if ( Os.isFamily( "windows" ) )
        {
            String value = RegQuery.getValue( "REG_SZ", "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion", "ProgramFilesDir" );
            assertNotNull( value );
        }
    }
}
