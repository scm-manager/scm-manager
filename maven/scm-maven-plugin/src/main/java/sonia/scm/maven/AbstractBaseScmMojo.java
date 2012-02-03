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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractBaseScmMojo extends AbstractScmMojo
{

  /** Field description */
  public static final String PROPERTY_SCM_HOME = "scm.home";

  /** Field description */
  private static final String FILE_CLASSPATH = "classpath.xml";

  /** Field description */
  private static final String RESOURCE_DEPENDENCY_LIST =
    "WEB-INF/classes/config/dependencies.list";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param warFile
   *
   * @return
   *
   * @throws MojoExecutionException
   */
  protected List<String> createExcludeList(File warFile)
          throws MojoExecutionException
  {
    List<String> excludeList = new ArrayList<String>();
    InputStream input = null;

    try
    {
      JarFile file = new JarFile(warFile);
      JarEntry entry = file.getJarEntry(RESOURCE_DEPENDENCY_LIST);

      if (entry == null)
      {
        throw new MojoExecutionException("could not find dependency list");
      }

      input = file.getInputStream(entry);

      Scanner scanner = null;

      try
      {
        scanner = new Scanner(input);

        while (scanner.hasNextLine())
        {
          parseLine(excludeList, scanner.nextLine());
        }
      }
      finally
      {
        if (scanner != null)
        {
          scanner.close();
        }
      }
    }
    catch (IOException ex)
    {
      throw new MojoExecutionException("could not read dependency file");
    }
    finally
    {
      IOUtils.closeQuietly(input);
    }

    return excludeList;
  }

  /**
   * Method description
   *
   *
   *
   *
   * @param excludeList
   * @throws MojoExecutionException
   */
  protected void installArtifacts(List<String> excludeList)
          throws MojoExecutionException
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

    installArtifact(excludeList, pluginDirectoryPath, classpath,
                    pluginRepository, getPluginArtifact());

    if (artifacts != null)
    {
      for (Artifact artifact : artifacts)
      {
        installArtifact(excludeList, pluginDirectoryPath, classpath,
                        pluginRepository, artifact);
      }
    }

    File classpathFile = new File(pluginDirectory, FILE_CLASSPATH);

    if (classpathFile.exists())
    {
      readClasspathFile(classpath, classpathFile);
    }

    writeClasspathFile(classpath, classpathFile);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected Artifact getPluginArtifact()
  {
    if (projectArtifact.getFile() == null)
    {
      File file = new File(project.getBuild().getDirectory(),
                           project.getBuild().getFinalName().concat(".jar"));

      projectArtifact.setFile(file);
    }

    return projectArtifact;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws MojoExecutionException
   */
  protected File getWebApplicationArchive() throws MojoExecutionException
  {
    File warFile = null;

    if (Util.isEmpty(webApplication.getVersion()))
    {
      String version = null;
      MavenProject parent = project.getParent();

      if (parent != null)
      {
        version = parent.getVersion();
      }
      else
      {
        version = project.getVersion();
      }

      webApplication.setVersion(version);
    }

    Artifact artifact =
      artifactFactory.createArtifact(webApplication.getGroupId(),
                                     webApplication.getArtifactId(),
                                     webApplication.getVersion(), "",
                                     webApplication.getType());

    try
    {
      artifactResolver.resolve(artifact, remoteRepositories, localRepository);
      warFile = artifact.getFile();

      if (!warFile.exists())
      {
        throw new MojoExecutionException("could not find webapp artifact file");
      }
    }
    catch (ArtifactNotFoundException ex)
    {
      throw new MojoExecutionException("could not find artifact", ex);
    }
    catch (ArtifactResolutionException ex)
    {
      throw new MojoExecutionException("could not fetch war-file", ex);
    }

    return warFile;
  }

  //~--- methods --------------------------------------------------------------

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
   *
   * @param excludeList
   * @param pluginDirectoryPath
   * @param classpath
   * @param pluginRepository
   * @param artifact
   *
   * @throws MojoExecutionException
   */
  private void installArtifact(List<String> excludeList,
                               String pluginDirectoryPath,
                               List<String> classpath,
                               ArtifactRepository pluginRepository,
                               Artifact artifact)
          throws MojoExecutionException
  {
    String id =
      artifact.getGroupId().concat(":").concat(artifact.getArtifactId());

    if (!excludeList.contains(id))
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
  }

  /**
   * Method description
   *
   *
   *
   * @param excludeList
   * @param line
   */
  private void parseLine(List<String> excludeList, String line)
  {
    line = line.trim();

    if (StringUtils.isNotEmpty(line))
    {
      String[] parts = line.split(":");

      if (parts.length >= 2)
      {
        excludeList.add(parts[0].concat(":").concat(parts[1]));
      }
    }
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
    try
    {
      JAXBContext context = JAXBContext.newInstance(Classpath.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Classpath c = (Classpath) unmarshaller.unmarshal(classpathFile);

      classpath.addAll(c.getClasspathElements());
    }
    catch (JAXBException ex)
    {
      throw new RuntimeException(ex);
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

    try
    {
      JAXBContext context = JAXBContext.newInstance(Classpath.class);
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(c, classpathFile);
    }
    catch (JAXBException ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
