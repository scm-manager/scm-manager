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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cli.I18n;
import sonia.scm.cli.config.ServerConfig;
import sonia.scm.client.ScmClient;
import sonia.scm.client.ScmClientSession;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.PrintWriter;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class SubCommand
{

  /** the logger for SubCommand */
  private static final Logger logger =
    LoggerFactory.getLogger(SubCommand.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  protected abstract void run();

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   * @param i18n
   * @param config
   */
  public void init(BufferedReader input, PrintWriter output, I18n i18n,
                   ServerConfig config)
  {
    this.input = input;
    this.output = output;
    this.i18n = i18n;
    this.config = config;
  }

  /**
   * Method description
   *
   *
   * @param args
   */
  public void run(Collection<String> args)
  {
    CmdLineParser parser = new CmdLineParser(this);

    try
    {
      parser.parseArgument(args);

      if (help)
      {
        parser.printUsage(output, i18n.getBundle());
        System.exit(1);
      }
      else
      {
        try
        {
          run();
        }
        finally
        {
          IOUtil.close(session);
        }
      }
    }
    catch (CmdLineException ex)
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("could not parse comannd line", ex);
      }

      if (!help)
      {
        output.append(i18n.getMessage(I18n.ERROR)).append(": ");
        output.println(ex.getMessage());
        output.println();
      }

      printHelp(parser);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getCommandName()
  {
    return commandName;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setCommandName(String name)
  {
    this.commandName = name;
  }

  //~--- methods --------------------------------------------------------------

  /**
   *  Method description
   *
   *
   *  @return
   */
  protected ScmClientSession createSession()
  {
    if (Util.isNotEmpty(config.getUsername())
        && Util.isNotEmpty(config.getPassword()))
    {
      session = ScmClient.createSession(config.getServerUrl(),
                                        config.getUsername(),
                                        config.getPassword());
    }
    else
    {
      session = ScmClient.createSession(config.getServerUrl());
    }

    return session;
  }

  /**
   * Method description
   *
   *
   * @param parser
   */
  protected void printHelp(CmdLineParser parser)
  {
    parser.printUsage(output, i18n.getBundle());
    System.exit(1);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected ServerConfig config;

  /** Field description */
  protected I18n i18n;

  /** Field description */
  protected BufferedReader input;

  /** Field description */
  protected PrintWriter output;

  /** Field description */
  private String commandName;

  /** Field description */
  @Option(
    name = "--help",
    usage = "optionHelpText",
    aliases = { "-h" }
  )
  private boolean help = false;

  /** Field description */
  private ScmClientSession session;
}
