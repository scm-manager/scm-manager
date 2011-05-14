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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.client.ScmClientSession;

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
   * @param session
   */
  public void init(BufferedReader input, PrintWriter output,
                   ScmClientSession session)
  {
    this.input = input;
    this.output = output;
    this.session = session;
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
        parser.printUsage(output, I18n.getBundle());
        System.exit(1);
      }
      else
      {
        run();
      }
    }
    catch (CmdLineException ex)
    {

      // todo error handling
      logger.error("could not parse command line", ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSessionRequired()
  {
    return sessionRequired;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Method description
   *
   *
   * @param sessionRequired
   */
  public void setSessionRequired(boolean sessionRequired)
  {
    this.sessionRequired = sessionRequired;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected BufferedReader input;

  /** Field description */
  protected String name;

  /** Field description */
  protected PrintWriter output;

  /** Field description */
  protected ScmClientSession session;

  /** Field description */
  @Option(
    name = "--help",
    usage = "optionHelpText",
    aliases = { "-h" }
  )
  private boolean help = false;

  /** Field description */
  private boolean sessionRequired = true;
}
