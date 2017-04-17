package org.javolution.maven;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.codehaus.mojo.natives.NativeBuildException;
import org.codehaus.mojo.natives.msvc.AbstractMSVCEnvFactory;
import org.codehaus.mojo.natives.msvc.EnvStreamConsumer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class MSVC2017x86EnvFactory extends AbstractMSVCEnvFactory {

    @SuppressWarnings("rawtypes")
    protected Map createEnvs() throws NativeBuildException {
        return this.createEnvs("VS150COMNTOOLS", "x86");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Problem with Visual Studio 2017 Installation (environment variable may not be set properly).
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("rawtypes")
    protected Map createEnvs(String commonToolEnvKey, String platform) throws NativeBuildException {
        File tmpEnvExecFile = null;
        try {
            File vsCommonToolDir = this.getCommonToolDirectory(commonToolEnvKey);
            File vsInstallDir = this.getVisualStudioInstallDirectory(vsCommonToolDir);
            if (!vsInstallDir.isDirectory()) {
                throw new NativeBuildException(vsInstallDir.getPath() + " is not a directory.");
            }
            tmpEnvExecFile = this.createEnvWrapperFile(vsInstallDir, platform);
            Commandline cl = new Commandline();
            cl.setExecutable(tmpEnvExecFile.getAbsolutePath());
            EnvStreamConsumer stdout = new EnvStreamConsumer();
            StreamConsumer stderr = new DefaultConsumer();
            CommandLineUtils.executeCommandLine(cl, stdout, stderr);
            return stdout.getParsedEnv();
        } catch (Exception e) {
            throw new NativeBuildException("Unable to retrieve env", e);
        } finally {
            if (tmpEnvExecFile != null) {
                tmpEnvExecFile.delete();
            }
        }

    }

    private File getCommonToolDirectory(String commonToolEnvKey) throws NativeBuildException {
        String envValue = System.getenv(commonToolEnvKey);
        if (envValue == null) {
            envValue = "C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Community\\Common7\\Tools\\";
            //throw new NativeBuildException("Environment variable: " + commonToolEnvKey + " not available.");
        }
        return new File(envValue);
    }

    private File getVisualStudioInstallDirectory(File commonToolDir) throws NativeBuildException {
        try {
            return new File(commonToolDir, "../..").getCanonicalFile();
        } catch (IOException e) {
            throw new NativeBuildException("Unable to contruct Visual Studio install directory using: " + commonToolDir,
                    e);
        }
    }

    private File createEnvWrapperFile(File vsInstallDir, String platform) throws IOException {
        File tmpFile = File.createTempFile("msenv", ".bat");

        StringBuffer buffer = new StringBuffer();
        buffer.append("@echo off\r\n");
        buffer.append("call \"").append(vsInstallDir).append("\"")
                .append("\\VC\\Auxiliary\\Build\\vcvarsall.bat " + platform + "\n\r");
        buffer.append("echo " + EnvStreamConsumer.START_PARSING_INDICATOR).append("\r\n");
        buffer.append("set\n\r");
        FileUtils.fileWrite(tmpFile.getAbsolutePath(), buffer.toString());

        return tmpFile;
    }

}
