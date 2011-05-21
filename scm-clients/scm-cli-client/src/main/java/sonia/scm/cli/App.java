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



package sonia.scm.cli;

//~--- non-JDK imports --------------------------------------------------------

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cli.cmd.CommandDescriptor;
import sonia.scm.cli.cmd.SubCommand;
import sonia.scm.cli.cmd.SubCommandHandler;
import sonia.scm.cli.cmd.SubCommandOptionHandler;
import sonia.scm.cli.config.ConfigOptionHandler;
import sonia.scm.cli.config.ScmClientConfig;
import sonia.scm.cli.config.ServerConfig;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class App
{

  /** the logger for App */
  private static final Logger logger = LoggerFactory.getLogger(App.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public App()
  {
    this(System.in, System.out);
  }

  /**
   * Constructs ...
   *
   *
   * @param input
   * @param output
   */
  public App(BufferedReader input, PrintWriter output)
  {
    this.input = input;
    this.output = output;
  }

  /**
   * Constructs ...
   *
   *
   * @param input
   * @param output
   */
  public App(InputStream input, OutputStream output)
  {
    this.input = new BufferedReader(new InputStreamReader(input));
    this.output = new PrintWriter(output);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   */
  public static void main(String[] args)
  {
    new App().run(args);
  }

  /**
   * Method description
   *
   *
   * @param args
   */
  protected void run(String[] args)
  {
    CmdLineParser parser = new CmdLineParser(this);

    try
    {
      parser.parseArgument(args);
    }
    catch (CmdLineException ex)
    {

      // todo error handling
      logger.warn("could not parse commandline", ex);
      System.exit(1);
    }

    loadConfig();

    I18n i18n = new I18n();

    if ((args.length == 0) || (subcommand == null) || help)
    {
      parser.printUsage(output, i18n.getBundle());
      output.println();
      output.println(i18n.getMessage(I18n.SUBCOMMANDS_TITLE));

      for (CommandDescriptor desc :
              SubCommandHandler.getInstance().getDescriptors())
      {
        output.append("  ").println(desc.getName());
      }
    }
    else
    {
      subcommand.init(input, output, i18n, config);
      subcommand.run(arguments);
    }

    IOUtil.close(input);
    IOUtil.close(output);
  }

  /**
   * Method description
   *
   */
  private void loadConfig()
  {
    if (config == null)
    {
      config = ScmClientConfig.getInstance().getDefaultConfig();
    }

    if (Util.isNotEmpty(serverUrl))
    {
      config.setServerUrl(serverUrl);
    }

    if (Util.isNotEmpty(username))
    {
      config.setUsername(username);
    }

    if (Util.isNotEmpty(password))
    {
      config.setPassword(password);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Option(
    name = "--config",
    usage = "optionConfig",
    metaVar = "metaVar_config",
    handler = ConfigOptionHandler.class,
    aliases = { "-c" }
  )
  private ServerConfig config;

  /** Field description */
  @Option(
    name = "--help",
    usage = "optionHelpText",
    aliases = { "-h" }
  )
  private boolean help = false;

  /** Field description */
  @Argument(index = 1, metaVar = "metaVar_arg")
  private List<String> arguments = new ArrayList<String>();

  /** Field description */
  private BufferedReader input;

  /** Field description */
  private PrintWriter output;

  /** Field description */
  @Option(
    name = "--password",
    usage = "optionPassword",
    aliases = { "-p" }
  )
  private String password;

  /** Field description */
  @Option(
    name = "--server",
    usage = "optionServerUrl",
    aliases = { "-s" }
  )
  private String serverUrl;

  /** Field description */
  @Argument(
    index = 0,
    metaVar = "metaVar_command",
    handler = SubCommandOptionHandler.class
  )
  private SubCommand subcommand;

  /** Field description */
  @Option(
    name = "--user",
    usage = "optionUsername",
    aliases = { "-u" }
  )
  private String username;
}
