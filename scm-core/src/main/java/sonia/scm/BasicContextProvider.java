/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import sonia.scm.config.WebappConfigProvider;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

/**
 * The default implementation of {@link SCMContextProvider}.
 *
 */
@SuppressWarnings("java:S106") // we can not use logger until base directory is not determined
public class BasicContextProvider implements SCMContextProvider
{

  /** Default version {@link String} */
  public static final String VERSION_DEFAULT = "unknown";

  /** Default name of the SCM-Manager base directory */
  public static final String DIRECTORY_DEFAULT = ".scm";

  /** Java system property for the SCM-Manager base directory */
  public static final String DIRECTORY_PROPERTY = "scm.home";

  /** Path to the maven properties file of the scm-core artifact */
  public static final String MAVEN_PROPERTIES =
    "/META-INF/scm/build-info.properties";

  /** Maven property for the version of the artifact */
  public static final String MAVEN_PROPERTY_VERSION = "version";

  public static final String DEVELOPMENT_INSTANCE_ID = "00000000-0000-0000-0000-000000000000";

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
      stage = Stage.valueOf(WebappConfigProvider.resolveAsString("stage").orElse(Stage.PRODUCTION.name()));
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

  @Override
  public Path resolve(Path path) {
    if (path.isAbsolute()) {
      return path;
    }

    return baseDirectory.toPath().resolve(path);
  }


 
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }


  @Override
  public Stage getStage()
  {
    return stage;
  }


  @Override
  public Throwable getStartupError()
  {
    return startupError;
  }

  /**
   * Returns the version of the SCM-Manager. If the version is not set, the
   * {@link #VERSION_DEFAULT} is returned.
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

  private String determineVersion() {
    String v = WebappConfigProvider.resolveAsString("versionOverride").orElse(null);
    if (Strings.isNullOrEmpty(v)) {
      v = loadVersion();
    }
    return v;
  }

  /**
   * Loads the version of the SCM-Manager from maven properties file.
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
    if (stage != Stage.PRODUCTION) {
      return DEVELOPMENT_INSTANCE_ID;
    }
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


}
