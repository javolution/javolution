/**
<p> Utilities to colorize source code examples in the javadoc.</p>

<pre>
* Overview

  The colapi plug-in formats and colorize Java(tm) code identified by the tags
  [code]...[/code] in the source code. The produced 'colorized' source code  
  can then be used in the sourcepath of javadoc.

* Usage

  To use it in a POM.xml file:
------------------------------------------
       &lt;plugin&gt;
           &lt;groupId&gt;org.javolution&lt;/groupId&gt;
           &lt;artifactId&gt;colapi&lt;/artifactId&gt;
           &lt;version&gt;2.0&lt;/version&gt;
       &lt;/plugin&gt;
      
       &lt;plugin&gt;
           &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
           &lt;artifactId&gt;maven-javadoc-plugin&lt;/artifactId&gt;
           &lt;version&gt;2.9&lt;/version&gt;
           &lt;configuration&gt;
                &lt;sourcepath&gt;${project.build.directory}/colorized&lt;/sourcepath&gt;
           &lt;/configuration&gt;
       &lt;/plugin&gt;
------------------------------------------

* Parameters

*----------------------*----------------------------------*
colapi.input           | The location of the directory holding the original source code (default $\{project.basedir\}/src/main/java).
*----------------------*----------------------------------*
colapi.output          | The location of the directory where the source code colorized is sent (default $\{project.build.directory\}/colorized).
*----------------------*----------------------------------*
colapi.filter          | The pathname filter (regex) of the files to colorize (default .*\\.java$).
*----------------------*----------------------------------*
colapi.encoding        | The file encoding (default $\{project.build.sourceEncoding\}).
*----------------------*----------------------------------*
colapi.code.start      | [code] replacement tag.
*----------------------*----------------------------------*
colapi.code.end        | [\code] replacement tag.
*----------------------*----------------------------------*
colapi.keyword.span    | The span tag for keywords.
*----------------------*----------------------------------*
colapi.comment.span    | The span tag for comments.
*----------------------*----------------------------------*
colapi.string.span     | The span tag for strings.
*----------------------*----------------------------------*
colapi.annotation.span | The span tag for annotations.
*----------------------*----------------------------------*
</pre>
 */
package javolution.tools.colapi;
