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

import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

/**
 *
 * @author Sebastian Sdorra
 */
public class CommandDescriptor implements Comparable<CommandDescriptor>
{

  /** the logger for CommandDescriptor */
  private static final Logger logger =
    LoggerFactory.getLogger(CommandDescriptor.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param commandClass
   */
  public CommandDescriptor(Class<? extends SubCommand> commandClass)
  {
    AssertUtil.assertIsNotNull(commandClass);
    this.commandClass = commandClass;

    Command cmd = commandClass.getAnnotation(Command.class);

    if (cmd != null)
    {
      this.name = cmd.name();
      this.group = cmd.group();
      this.usage = cmd.usage();
    }

    if (Util.isEmpty(name))
    {
      name = commandClass.getSimpleName();
    }
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param usage
   * @param commandClass
   */
  public CommandDescriptor(String name, String usage,
                           Class<? extends SubCommand> commandClass)
  {
    this.name = name;
    this.usage = usage;
    this.commandClass = commandClass;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param desc
   *
   * @return
   */
  @Override
  public int compareTo(CommandDescriptor desc)
  {
    int result = group.compareTo(desc.group);

    if (result == 0)
    {
      result = name.compareTo(desc.name);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public SubCommand createSubCommand()
  {
    SubCommand command = null;

    try
    {
      command = commandClass.newInstance();
      command.setCommandName(name);
    }
    catch (Exception ex)
    {
      logger.error("could not create SubCommand {}", commandClass.getName());
    }

    return command;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<? extends SubCommand> getCommandClass()
  {
    return commandClass;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getGroup()
  {
    return group;
  }

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
  public String getUsage()
  {
    return usage;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Class<? extends SubCommand> commandClass;

  /** Field description */
  private String group = "misc";

  /** Field description */
  private String name;

  /** Field description */
  private String usage;
}
