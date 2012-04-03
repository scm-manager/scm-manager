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
 * The default implementation of {@link SCMContextProvider}.
 *
 * @author Sebastian Sdorra
 */
public class BasicContextProvider implements SCMContextProvider
{

  /** Default version {@link String} */
  public static final String DEFAULT_VERSION = "unknown";

  /** Default name of the SCM-Manager base directory */
  public static final String DIRECTORY_DEFAULT = ".scm";

  /** Environment varibale for the SCM-Manager base directory */
  public static final String DIRECTORY_ENVIRONMENT = "SCM_HOME";

  /** Java system property for the SCM-Manager base directory */
  public static final String DIRECTORY_PROPERTY = "scm.home";

  /** Classpath resource for the SCM-Manager base directory */
  public static final String DIRECTORY_RESOURCE = "/scm.properties";

  /** Path to the maven properties file of the scm-core artifact */
  public static final String MAVEN_PROPERTIES =
    "/META-INF/maven/sonia.scm/scm-core/pom.properties";

  /** Maven property for the version of the artifact */
  public static final String MAVEN_PROPERTY_VERSION = "version";

  /**
   * Java system property for the SCM-Manager project stage
   * @since 1.12
   */
  public static final String STAGE_PROPERTY = "scm.stage";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link BasicContextProvider} object.
   *
   */
  public BasicContextProvider()
  {
    try
    {
      baseDirectory = findBaseDirectory();
      version = loadVersion();
      stage = loadProjectStage();
    }
    catch (Throwable ex)
    {
      this.startupError = ex;

      // print exception to system err
      ex.printStackTrace(System.err);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@see java.io.Closeable#close()}
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {}

  /**
   * {@see SCMContextProvider#init()}
   *
   */
  @Override
  public void init() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * {@see SCMContextProvider#getBaseDirectory()}
   *
   *
   * @return {@see SCMContextProvider#getBaseDirectory()}
   */
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  @Override
  public Stage getStage()
  {
    return stage;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public Throwable getStartupError()
  {
    return startupError;
  }

  /**
   * Returns the version of the SCM-Manager. If the version is not set, the
   * {@link #DEFAULT_VERSION} is returned.
   *
   *
   * @return the version of the SCM-Manager
   */
  @Override
  public String getVersion()
  {
    return version;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Find the base directory of SCM-Manager.
   *
   *
   * @return base directory SCM-Manager
   */
  private File findBaseDirectory()
  {
    String path = getPathFromResource();

    if (Util.isEmpty(path))
    {
      path = System.getProperty(DIRECTORY_PROPERTY);

      if (Util.isEmpty(path))
      {
        path = System.getenv(DIRECTORY_ENVIRONMENT);

        if (Util.isEmpty(path))
        {
          path = System.getProperty("user.home").concat(File.separator).concat(
            DIRECTORY_DEFAULT);
        }
      }
    }

    File directory = new File(path);

    if (!directory.exists() &&!directory.mkdirs())
    {
      String msg = "could not create home directory at ".concat(
                       directory.getAbsolutePath());

      // do not use logger
      // http://www.slf4j.org/codes.html#substituteLogger
      System.err.println("===================================================");
      System.err.append("Error: ").println(msg);
      System.err.println("===================================================");

      throw new IllegalStateException(msg);
    }

    return directory;
  }

  /**
   * Find the current stage.
   *
   *
   * @return current stage
   */
  private Stage loadProjectStage()
  {
    Stage s = Stage.PRODUCTION;
    String stageProperty = System.getProperty(STAGE_PROPERTY);

    if (Util.isNotEmpty(stageProperty))
    {
      try
      {
        s = Stage.valueOf(stageProperty.toUpperCase());
      }
      catch (IllegalArgumentException ex)
      {

        // do not use logger or IOUtil,
        // http://www.slf4j.org/codes.html#substituteLogger
        ex.printStackTrace(System.err);
      }
    }

    return s;
  }

  /**
   * Loads the version of the SCM-Manager from maven properties file.
   *
   *
   * @return the version of the SCM-Manager
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
      finally
      {

        // do not use logger or IOUtil,
        // http://www.slf4j.org/codes.html#substituteLogger
        try
        {
          input.close();
        }
        catch (IOException ex)
        {
          ex.printStackTrace(System.err);
        }
      }
    }

    return properties.getProperty(MAVEN_PROPERTY_VERSION, DEFAULT_VERSION);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Load path from classpath resource.
   *
   *
   * @return path from classpath resource or null
   */
  private String getPathFromResource()
  {
    String path = null;
    InputStream input = null;

    try
    {
      input =
        BasicContextProvider.class.getResourceAsStream(DIRECTORY_RESOURCE);

      if (input != null)
      {
        Properties properties = new Properties();

        properties.load(input);
        path = properties.getProperty(DIRECTORY_PROPERTY);
      }
    }
    catch (IOException ex)
    {
      throw new ConfigurationException(
          "could not load properties form resource ".concat(
            DIRECTORY_RESOURCE), ex);
    }
    finally
    {

      // do not use logger or IOUtil,
      // http://www.slf4j.org/codes.html#substituteLogger
      if (input != null)
      {
        try
        {
          input.close();
        }
        catch (IOException ex)
        {
          ex.printStackTrace(System.err);
        }
      }
    }

    return path;
  }

  //~--- fields ---------------------------------------------------------------

  /** The base directory of the SCM-Manager */
  private File baseDirectory;

  /** stage of the current SCM-Manager instance */
  private Stage stage;

  /** startup exception */
  private Throwable startupError;

  /** the version of the SCM-Manager */
  private String version;
}
