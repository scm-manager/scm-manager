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
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class SubCommandOptionHandler extends OptionHandler<SubCommand>
{

  /** Field description */
  public static final String RESOURCE_SERVICES =
    "/META-INF/services/".concat(SubCommand.class.getName());

  /** the logger for SubCommandOptionHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(SubCommandOptionHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param parser
   * @param option
   * @param setter
   */
  public SubCommandOptionHandler(CmdLineParser parser, OptionDef option,
                                 Setter<? super SubCommand> setter)
  {
    super(parser, option, setter);
    subCommands = new HashMap<String, CommandDescriptor>();
    loadSubCommands();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param parameters
   *
   * @return
   *
   * @throws CmdLineException
   */
  @Override
  public int parseArguments(Parameters parameters) throws CmdLineException
  {
    String name = parameters.getParameter(0);
    CommandDescriptor desc = subCommands.get(name);

    if (desc != null)
    {
      owner.stopOptionParsing();
      setter.addValue(desc.createSubCommand());
    }
    else
    {
      throw new CmdLineException(owner,
                                 "command ".concat(name).concat(" not found"));
    }

    return 1;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getDefaultMetaVariable()
  {
    return "metaVar_command";
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void loadSubCommands()
  {
    BufferedReader reader = null;

    try
    {
      reader = new BufferedReader(
          new InputStreamReader(
              SubCommandOptionHandler.class.getResourceAsStream(
                RESOURCE_SERVICES)));

      String line = reader.readLine();

      while (line != null)
      {
        parseLine(line);
        line = reader.readLine();
      }
    }
    catch (IOException ex)
    {
      logger.error("could not load commands");
    }
    finally
    {
      IOUtil.close(reader);
    }
  }

  /**
   * Method description
   *
   *
   * @param line
   */
  private void parseLine(String line)
  {
    line = line.trim();

    if (Util.isNotEmpty(line) &&!line.startsWith("#"))
    {
      try
      {
        Class<? extends SubCommand> clazz =
          (Class<? extends SubCommand>) Class.forName(line);
        CommandDescriptor desc = new CommandDescriptor(clazz);

        subCommands.put(desc.getName(), desc);
      }
      catch (ClassNotFoundException ex)
      {
        logger.warn("could not found command class {}", line);
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, CommandDescriptor> subCommands;
}
