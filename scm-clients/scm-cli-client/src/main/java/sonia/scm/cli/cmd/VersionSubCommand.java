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



package sonia.scm.cli.cmd;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.9
 */
@Command(
  name = "version",
  usage = "usageVersion",
  group = "misc"
)
public class VersionSubCommand extends SubCommand
{

  /** Default version {@link String} */
  public static final String DEFAULT_VERSION = "unknown";

  /** Path to the maven properties file of the scm-core artifact */
  public static final String MAVEN_PROPERTIES =
    "/META-INF/maven/sonia.scm.clients/scm-cli-client/pom.properties";

  /** Maven property for the version of the artifact */
  public static final String MAVEN_PROPERTY_VERSION = "version";

  /** the logger for VersionSubCommand */
  private static final Logger logger =
    LoggerFactory.getLogger(VersionSubCommand.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void run()
  {
    String version = getVersion();

    output.append("scm-cli-client version: ").println(version);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private String getVersion()
  {
    String version = null;
    InputStream stream = null;

    try
    {
      stream = VersionSubCommand.class.getResourceAsStream(MAVEN_PROPERTIES);

      if (stream != null)
      {
        Properties properties = new Properties();

        properties.load(stream);
        version = properties.getProperty(MAVEN_PROPERTY_VERSION);
      }
    }
    catch (IOException ex)
    {
      logger.warn("could not parse maven.properties", ex);
    }
    finally
    {
      IOUtil.close(stream);
    }

    if (Util.isEmpty(version))
    {
      version = DEFAULT_VERSION;
    }

    return version;
  }
}
