/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.server;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.cli.CliException;
import sonia.scm.cli.CliParser;
import sonia.scm.cli.DefaultCliHelpBuilder;
import sonia.scm.util.ServiceUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    Logger.getLogger(ServerApplication.class.getName());

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
              logger.log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
              logger.log(Level.SEVERE, null, ex);
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
   * @param parser
   * @param config
   */
  private static void printHelp(ApplicationInformation appInfo, CliParser parser, ServerConfig config)
  {
    String s = System.getProperty("line.separator");
    StringBuilder prefix = new StringBuilder(appInfo.getName());
    prefix.append(" ").append( appInfo.getVersion() );

    prefix.append(s).append("usage: ");
    prefix.append(s);

    DefaultCliHelpBuilder helpBuilder =
      new DefaultCliHelpBuilder(prefix.toString(), null);

    System.err.println(parser.createHelp(helpBuilder, config));
    System.exit(RETURNCODE_CLI_ERROR);
  }
}
