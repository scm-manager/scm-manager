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

package sonia.scm.server;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cli.CliException;
import sonia.scm.cli.CliParser;
import sonia.scm.cli.DefaultCliHelpBuilder;
import sonia.scm.util.ServiceUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class ServerApplication
{

  /** Field description */
  public static final String APPINFO = "/app-info.xml";

  /** Field description */
  public static final int RETURNCODE_CLI_ERROR = 2;

  /** Field description */
  public static final int RETURNCODE_MISSING_APPINFO = 1;

  /** Field description */
  public static final int RETURNCODE_MISSING_SERVER_IMPLEMENTATION = 3;

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(ServerApplication.class.getName());

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   *
   * @throws CliException
   * @throws IOException
   * @throws ServerException
   */
  public static void main(String[] args)
          throws CliException, ServerException, IOException
  {
    InputStream input = ServerApplication.class.getResourceAsStream(APPINFO);

    if (input == null)
    {
      System.err.println("could not find /app-info.xml in classpath");
      System.exit(RETURNCODE_MISSING_APPINFO);
    }

    ApplicationInformation appInfo = JAXB.unmarshal(input,
                                       ApplicationInformation.class);
    ServerConfig config = new ServerConfig();
    CliParser parser = new CliParser();

    parser.parse(parser, args);

    if (config.getShowHelp())
    {
      printHelp(appInfo, parser, config);
    }
    else
    {
      final Server server = ServiceUtil.getService(Server.class);

      if (server == null)
      {
        System.err.println("could not find an server implementation");
        System.exit(RETURNCODE_MISSING_SERVER_IMPLEMENTATION);
      }

      File webapp = new File("webapp", appInfo.getAppName());

      server.start(config, webapp);
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          if (server.isRunning())
          {
            try
            {
              server.stop();
            }
            catch (ServerException ex)
            {
              logger.error(ex.getMessage(), ex);
            }
            catch (IOException ex)
            {
              logger.error(ex.getMessage(), ex);
            }
          }
        }
      }));
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param appInfo
   * @param parser
   * @param config
   */
  private static void printHelp(ApplicationInformation appInfo,
                                CliParser parser, ServerConfig config)
  {
    String s = System.getProperty("line.separator");
    StringBuilder prefix = new StringBuilder(appInfo.getName());

    prefix.append(" ").append(appInfo.getVersion());
    prefix.append(s).append("usage: ");
    prefix.append(s);

    DefaultCliHelpBuilder helpBuilder =
      new DefaultCliHelpBuilder(prefix.toString(), null);

    System.err.println(parser.createHelp(helpBuilder, config));
    System.exit(RETURNCODE_CLI_ERROR);
  }
}
