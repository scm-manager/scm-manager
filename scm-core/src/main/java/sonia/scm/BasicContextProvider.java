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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

/**
 *
 * @author Sebastian Sdorra
 */
public class BasicContextProvider implements SCMContextProvider
{

  /** Field description */
  public static final String DEFAULT_VERSION = "unknown";

  /** Field description */
  public static final String DIRECTORY_DEFAULT = ".scm";

  /** Field description */
  public static final String DIRECTORY_ENVIRONMENT = "SCM_HOME";

  /** Field description */
  public static final String DIRECTORY_PROPERTY = "scm.home";

  /** Field description */
  public static final String MAVEN_PROPERTIES =
    "/META-INF/maven/sonia.scm/scm-core/pom.properties";

  /** Field description */
  public static final String MAVEN_PROPERTY_VERSION = "version";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public BasicContextProvider()
  {
    baseDirectory = findBaseDirectory();
    version = loadVersion();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {}

  /**
   * Method description
   *
   */
  @Override
  public void init() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getVersion()
  {
    return version;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private File findBaseDirectory()
  {
    String path = System.getProperty(DIRECTORY_PROPERTY);

    if (Util.isEmpty(path))
    {
      path = System.getenv(DIRECTORY_ENVIRONMENT);

      if (Util.isEmpty(path))
      {
        path = System.getProperty("user.home").concat(File.separator).concat(
          DIRECTORY_DEFAULT);
      }
    }

    File directory = new File(path);

    if (!directory.exists() &&!directory.mkdirs())
    {
      throw new IllegalStateException("could not create directory");
    }

    return directory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String loadVersion()
  {
    Properties properties = new Properties();
    InputStream input =
      BasicContextProvider.class.getResourceAsStream(MAVEN_PROPERTIES);

    if (input != null)
    {
      try
      {
        properties.load(input);
      }
      catch (IOException ex)
      {
        throw new ConfigurationException(ex);
      }
    }

    return properties.getProperty(MAVEN_PROPERTY_VERSION, DEFAULT_VERSION);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private String version;
}
