/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableSet;

import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.api.Command;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnRepositoryServiceProvider extends RepositoryServiceProvider
{

  /** Field description */
  private static final Set<Command> COMMANDS = ImmutableSet.of(Command.BLAME,
                                                 Command.BROWSE, Command.CAT,
                                                 Command.DIFF, Command.LOG);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  SvnRepositoryServiceProvider(SvnRepositoryHandler handler,
                               Repository repository)
  {
    this.repository = repository;
    this.context = new SvnContext(handler.getDirectory(repository));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public SvnBlameCommand getBlameCommand()
  {
    return new SvnBlameCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public SvnBrowseCommand getBrowseCommand()
  {
    return new SvnBrowseCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public SvnCatCommand getCatCommand()
  {
    return new SvnCatCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public SvnDiffCommand getDiffCommand()
  {
    return new SvnDiffCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public SvnLogCommand getLogCommand()
  {
    return new SvnLogCommand(context, repository);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<Command> getSupportedCommands()
  {
    return COMMANDS;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnContext context;

  /** Field description */
  private Repository repository;
}
