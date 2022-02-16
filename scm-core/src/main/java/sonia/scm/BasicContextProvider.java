/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

//~--- JDK imports ------------------------------------------------------------

/**
 * The default implementation of {@link SCMContextProvider}.
 *
 * @author Sebastian Sdorra
 */
@SuppressWarnings("java:S106") // we can not use logger until base directory is not determined
public class BasicContextProvider implements SCMContextProvider
{

  /** Default version {@link String} */
  public static final String VERSION_DEFAULT = "unknown";

  /**
   * System property to override scm-manager version.
   * <b>Note</b>: This should only be used for testing and never in production.
   **/
  public static final String VERSION_PROPERTY = "sonia.scm.version.override";

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
    "/META-INF/scm/build-info.properties";

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
      version = determineVersion();
      stage = loadProjectStage();
      instanceId = readOrCreateInstanceId();
    }
    catch (Exception ex)
    {
      this.startupError = ex;

      // print exception to system err
      ex.printStackTrace(System.err);
    }
  }

  @VisibleForTesting
  BasicContextProvider(File baseDirectory, String version, Stage stage) {
    this.baseDirectory = baseDirectory;
    this.version = version;
    this.stage = stage;
  }

  //~--- methods --------------------------------------------------------------


  @Override
  public Path resolve(Path path) {
    if (path.isAbsolute()) {
      return path;
    }

    return baseDirectory.toPath().resolve(path);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Stage getStage()
  {
    return stage;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Throwable getStartupError()
  {
    return startupError;
  }

  /**
   * Returns the version of the SCM-Manager. If the version is not set, the
   * {@link #VERSION_DEFAULT} is returned.
   *
   *
   * @return the version of the SCM-Manager
   */
  @Override
  public String getVersion()
  {
    return version;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Find the base directory of SCM-Manager.
   *
   *
   * @return base directory SCM-Manager
   */
  private File findBaseDirectory() {
    File directory = BaseDirectory.get().toFile();

    if (!directory.exists() &&!directory.mkdirs()) {
      error("could not create home directory at " + directory.getAbsolutePath());
    } else if (directory.exists() && !directory.canWrite()) {
      error("could not modify home directory at " + directory.getAbsolutePath());
    }

    return directory;
  }


  private void error(String msg) {
    // do not use logger
    // http://www.slf4j.org/codes.html#substituteLogger
    System.err.println("===================================================");
    System.err.append("Error: ").println(msg);
    System.err.println("===================================================");

    throw new IllegalStateException(msg);
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
        s = Stage.valueOf(stageProperty.toUpperCase(Locale.ENGLISH));
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

  private String determineVersion() {
    String v = System.getProperty(VERSION_PROPERTY);
    if (Strings.isNullOrEmpty(v)) {
      v = loadVersion();
    }
    return v;
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

    return properties.getProperty(MAVEN_PROPERTY_VERSION, VERSION_DEFAULT);
  }

  private String readOrCreateInstanceId() throws IOException {
    File configDirectory = new File(baseDirectory, "config");
    IOUtil.mkdirs(configDirectory);
    File instanceIdFile = new File(configDirectory, ".instance-id");
    if (instanceIdFile.exists()) {
      return Files.asCharSource(instanceIdFile, StandardCharsets.UTF_8).read();
    }
    String uuid = UUID.randomUUID().toString();
    Files.asCharSink(instanceIdFile, StandardCharsets.UTF_8).write(uuid);
    return uuid;
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

  /** the instance id of the SCM-Manager */
  private String instanceId;
}
