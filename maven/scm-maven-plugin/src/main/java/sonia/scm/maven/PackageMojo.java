/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.maven;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Sebastian Sdorra
 * @goal package
 * @requiresDependencyResolution runtime
 * @execute phase="package"
 */
public class PackageMojo extends AbstractBaseScmMojo
{

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
    File warFile = getWebApplicationArchive();
    List<String> excludeList = createExcludeList(warFile);

    installArtifacts(excludeList, packageDirectory);
    copyDescriptor();
    createPackage();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public File getDescriptor()
  {
    return descriptor;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public File getPackageDirectory()
  {
    return packageDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public File getPackageFile()
  {
    return packageFile;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param descriptor
   */
  public void setDescriptor(File descriptor)
  {
    this.descriptor = descriptor;
  }

  /**
   * Method description
   *
   *
   * @param packageDirectory
   */
  public void setPackageDirectory(File packageDirectory)
  {
    this.packageDirectory = packageDirectory;
  }

  /**
   * Method description
   *
   *
   * @param packageFile
   */
  public void setPackageFile(File packageFile)
  {
    this.packageFile = packageFile;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param zout
   * @param baseDirectory
   * @param baseDirectoryLength
   * @param file
   *
   * @throws IOException
   */
  private void appendFile(ZipOutputStream zout, int baseDirectoryLength,
    File file)
    throws IOException
  {
    if (file.isDirectory())
    {
      for (File f : file.listFiles())
      {
        appendFile(zout, baseDirectoryLength, f);
      }
    }
    else
    {
      InputStream input = null;
      ZipEntry entry =
        new ZipEntry(file.getAbsolutePath().substring(baseDirectoryLength));

      try
      {
        input = new FileInputStream(file);

        zout.putNextEntry(entry);
        IOUtils.copy(input, zout);
      }
      finally
      {

        IOUtils.closeQuietly(input);
      }

      zout.closeEntry();
    }
  }

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   */
  private void copyDescriptor() throws MojoExecutionException
  {
    try
    {
      File packageDescriptorFile = new File(packageDirectory, "plugin.xml");

      FileUtils.copyFile(descriptor, packageDescriptorFile);
    }
    catch (IOException ex)
    {
      throw new MojoExecutionException("could not copy descriptor", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @throws MojoExecutionException
   */
  private void createPackage() throws MojoExecutionException
  {

    ZipOutputStream zout = null;

    try
    {
      zout = new ZipOutputStream(new FileOutputStream(packageFile));
      appendFile(zout, packageDirectory.getAbsolutePath().length() + 1,
        packageDirectory);

    }
    catch (IOException ex)
    {
      throw new MojoExecutionException("could not create package", ex);
    }
    finally
    {
      IOUtils.closeQuietly(zout);
    }

  }

  //~--- fields ---------------------------------------------------------------

  /**
   * @parameter default-value="${project.build.directory}/classes/META-INF/scm/plugin.xml"
   */
  private File descriptor;

  /**
   * @parameter expression="${packageDirectory}" default-value="${project.build.directory}/${project.artifactId}-${project.version}-package"
   */
  private File packageDirectory;

  /**
   * @parameter expression="${packageFile}" default-value="${project.build.directory}/${project.artifactId}-${project.version}.scmp"
   */
  private File packageFile;
}
