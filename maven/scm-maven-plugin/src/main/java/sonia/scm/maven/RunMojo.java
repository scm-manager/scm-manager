/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 * @goal run
 * @requiresDependencyResolution runtime
 * @phase package
 */
public class RunMojo extends AbstractMojo
{

  /** Field description */
  private static final String FILE_CLASSPATH = "classpath.xml";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   * @throws MojoFailureException
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    Artifact artifact = artifactFactory.createArtifact(groupId, artifactId,
                          version, "", type);

    try
    {
      artifactResolver.resolve(artifact, remoteRepositories, localRepository);

      File warFile = artifact.getFile();

      if (!warFile.exists())
      {
        throw new MojoFailureException("could not find webapp artifact file");
      }

      installArtifacts();
      runServletContainer(warFile);
    }
    catch (ArtifactNotFoundException ex)
    {
      throw new MojoExecutionException("could not fetch war-file", ex);
    }
    catch (ArtifactResolutionException ex)
    {
      throw new MojoExecutionException("could not fetch war-file", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param pluginDirectoryPath
   * @param classpath
   * @param source
   * @param artifact
   * @param localRepository
   *
   * @throws ArtifactInstallationException
   */
  private void install(String pluginDirectoryPath, List<String> classpath,
                       File source, Artifact artifact,
                       ArtifactRepository localRepository)
          throws ArtifactInstallationException
  {
    try
    {
      String localPath = localRepository.pathOf(artifact);
      File destination = new File(localRepository.getBasedir(), localPath);

      if (!destination.getParentFile().exists())
      {
        destination.getParentFile().mkdirs();
      }

      getLog().info("Installing artifact " + source.getPath() + " to "
                    + destination);
      FileUtils.copyFile(source, destination);

      String relativePath =
        destination.getAbsolutePath().substring(pluginDirectoryPath.length());

      classpath.add(relativePath);
    }
    catch (IOException e)
    {
      throw new ArtifactInstallationException("Error installing artifact: "
              + e.getMessage(), e);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param pluginDirectoryPath
   * @param classpath
   * @param pluginRepository
   * @param artifact
   *
   * @throws MojoExecutionException
   */
  private void installArtifact(String pluginDirectoryPath,
                               List<String> classpath,
                               ArtifactRepository pluginRepository,
                               Artifact artifact)
          throws MojoExecutionException
  {
    if (artifact.getFile() != null)
    {
      try
      {

        // See: http://mail-archives.apache.org/mod_mbox/maven-dev/200511.mbox/%3c437288F4.4080003@apache.org%3e
        artifact.isSnapshot();
        install(pluginDirectoryPath, classpath, artifact.getFile(), artifact,
                pluginRepository);
      }
      catch (ArtifactInstallationException e)
      {
        throw new MojoExecutionException("Failed to copy artifact.", e);
      }
    }
    else
    {
      throw new MojoExecutionException(
          "could not find file for ".concat(artifact.getId()));
    }
  }

  /**
   * Method description
   *
   *
   *
   * @throws MojoExecutionException
   */
  private void installArtifacts() throws MojoExecutionException
  {
    File pluginDirectory = new File(scmHome, "plugins");

    if (!pluginDirectory.exists() &&!pluginDirectory.mkdirs())
    {
      throw new MojoExecutionException(
          "could not create plugin directory ".concat(
            pluginDirectory.getPath()));
    }

    String repositoryUrl = "file://".concat(pluginDirectory.getAbsolutePath());
    ArtifactRepositoryLayout layout =
      availableRepositoryLayouts.get(repositoryLayout);

    if (layout == null)
    {
      throw new MojoExecutionException(
          "could not find repository layout ".concat(repositoryLayout));
    }

    ArtifactRepository pluginRepository =
      artifactRepositoryFactory.createDeploymentArtifactRepository(
          "scm-run-plugin", repositoryUrl, layout, true);
    List<String> classpath = new ArrayList<String>();
    String pluginDirectoryPath = pluginDirectory.getAbsolutePath();

    installArtifact(pluginDirectoryPath, classpath, pluginRepository,
                    projectArtifact);

    if (artifacts != null)
    {
      for (Artifact artifact : artifacts)
      {
        installArtifact(pluginDirectoryPath, classpath, pluginRepository,
                        artifact);
      }
    }

    File classpathFile = new File(pluginDirectory, FILE_CLASSPATH);

    if (classpathFile.exists())
    {
      readClasspathFile(classpath, classpathFile);
    }

    writeClasspathFile(classpath, classpathFile);
  }

  /**
   * Method description
   *
   *
   * @param classpath
   * @param classpathFile
   */
  private void readClasspathFile(List<String> classpath, File classpathFile)
  {
    Classpath c = JAXB.unmarshal(classpathFile, Classpath.class);

    classpath.addAll(c.getClasspathElements());
  }

  /**
   * Method description
   *
   *
   * @param warFile
   *
   * @throws MojoFailureException
   */
  private void runServletContainer(File warFile) throws MojoFailureException
  {
    getLog().error("start servletcontainer at port " + port);

    try
    {
      System.setProperty("scm.home", scmHome);

      Server server = new Server();
      SelectChannelConnector connector = new SelectChannelConnector();

      connector.setPort(port);
      server.addConnector(connector);

      WebAppContext warContext = new WebAppContext();

      warContext.setContextPath(contextPath);
      warContext.setExtractWAR(true);
      warContext.setWar(warFile.getAbsolutePath());
      server.setHandler(warContext);
      server.start();
      server.join();
    }
    catch (Exception ex)
    {
      throw new MojoFailureException("could not start servletcontainer", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param classpath
   * @param classpathFile
   */
  private void writeClasspathFile(List<String> classpath, File classpathFile)
  {
    Classpath c = new Classpath();

    c.setClasspathElements(classpath);
    JAXB.marshal(c, classpathFile);
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
   * @required
   * @readonly
   */
  private ArtifactFactory artifactFactory;

  /**
   * @parameter
   */
  private String artifactId = "scm-webapp";

  /**
   * @component
   */
  private ArtifactRepositoryFactory artifactRepositoryFactory;

  /**
   * Used to look up Artifacts in the remote repository.
   *
   * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
   * @required
   * @readonly
   */
  private ArtifactResolver artifactResolver;

  /**
   * @readonly
   * @parameter expression="${project.artifacts}"
   */
  private Set<Artifact> artifacts;

  /**
   * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
   */
  private Map<String, ArtifactRepositoryLayout> availableRepositoryLayouts;

  /**
   * @parameter
   */
  private String contextPath = "/scm";

  /**
   * @parameter
   */
  private String groupId = "sonia.scm";

  /**
   * @readonly
   * @parameter expression="${localRepository}"
   */
  private ArtifactRepository localRepository;

  /**
   * @parameter
   */
  private int port = 8081;

  /**
   * @readonly
   * @parameter expression="${project.artifact}"
   */
  private Artifact projectArtifact;

  /**
   * List of Remote Repositories used by the resolver
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  private List remoteRepositories;

  /**
   * @parameter
   */
  private String repositoryLayout = "default";

  /**
   * @parameter expression="${project.build.directory}/scm-home"
   */
  private String scmHome;

  /**
   * @parameter
   */
  private String type = "war";

  /**
   * @parameter expression="${project.parent.version}"
   */
  private String version;
}
