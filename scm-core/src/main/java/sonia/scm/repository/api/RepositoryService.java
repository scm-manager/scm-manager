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



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Feature;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.RepositoryServiceProvider;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.IOException;

/**
 * From the {@link RepositoryService} it is possible to access all commands for
 * a single {@link Repository}. The {@link RepositoryService} is only access
 * able from the {@link RepositoryServiceFactory}.<br />
 * <br />
 *
 * <b>Note:</b> Not every {@link RepositoryService} supports every command. If
 * the command is not supported the method will trow a
 * {@link CommandNotSupportedException}. It is possible to check if the command
 * is supported by the {@link RepositoryService} with the
 * {@link RepositoryService#isSupported(Command)} method.<br />
 * <br />
 *
 * <b>Warning:</b> You should always close the connection to the repository
 * after work is finished. For closing the connection to the repository use the
 * {@link #close()} method.
 *
 * @author Sebastian Sdorra
 * @since 1.17
 *
 * @apiviz.uses sonia.scm.repository.Feature
 * @apiviz.uses sonia.scm.repository.api.Command
 * @apiviz.uses sonia.scm.repository.api.BlameCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.BrowseCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.CatCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.DiffCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.LogCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.TagsCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.BranchesCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.IncomingCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.OutgoingCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.PullCommandBuilder
 * @apiviz.uses sonia.scm.repository.api.PushCommandBuilder
 */
public final class RepositoryService implements Closeable
{

  /**
   * the logger for RepositoryService
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryService.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link RepositoryService}. This constructor should only
   * be called from the {@link RepositoryServiceFactory}.
   *
   * @param cacheManager cache manager
   * @param provider implementation for {@link RepositoryServiceProvider}
   * @param repository the repository
   * @param preProcessorUtil
   */
  RepositoryService(CacheManager cacheManager,
    RepositoryServiceProvider provider, Repository repository,
    PreProcessorUtil preProcessorUtil)
  {
    this.cacheManager = cacheManager;
    this.provider = provider;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Closes the connection to the repository and releases all locks
   * and resources. This method should be called in a finally block e.g.:
   *
   * <pre><code>
   * RepositoryService service = null;
   * try {
   *   service = factory.create("repositoryId");
   *   // do something with the service
   * } finally {
   *   if ( service != null ){
   *     service.close();
   *   }
   * }
   * </code></pre>
   */
  @Override
  public void close()
  {
    try
    {
      provider.close();
    }
    catch (IOException ex)
    {
      logger.error("cound not close repository service provider", ex);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * The blame command shows changeset information by line for a given file.
   *
   * @return instance of {@link BlameCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   */
  public BlameCommandBuilder getBlameCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create blame command for repository {}",
        repository.getName());
    }

    return new BlameCommandBuilder(cacheManager, provider.getBlameCommand(),
      repository, preProcessorUtil);
  }

  /**
   * The branches command list all repository branches.
   *
   * @return instance of {@link BranchesCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   */
  public BranchesCommandBuilder getBranchesCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create branches command for repository {}",
        repository.getName());
    }

    return new BranchesCommandBuilder(cacheManager,
      provider.getBranchesCommand(), repository);
  }

  /**
   * The browse command allows browsing of a repository.
   *
   * @return instance of {@link BrowseCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   */
  public BrowseCommandBuilder getBrowseCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create browse command for repository {}",
        repository.getName());
    }

    return new BrowseCommandBuilder(cacheManager, provider.getBrowseCommand(),
      repository, preProcessorUtil);
  }

  /**
   * The cat command show the content of a given file.
   *
   * @return instance of {@link CatCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   */
  public CatCommandBuilder getCatCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create cat command for repository {}",
        repository.getName());
    }

    return new CatCommandBuilder(provider.getCatCommand());
  }

  /**
   * The diff command shows differences between revisions for a specified file
   * or the entire revision.
   *
   * @return instance of {@link DiffCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   */
  public DiffCommandBuilder getDiffCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create diff command for repository {}",
        repository.getName());
    }

    return new DiffCommandBuilder(provider.getDiffCommand());
  }

  /**
   * The incoming command shows new {@link Changeset}s found in a different
   * repository location.
   *
   *
   * @return instance of {@link IncomingCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   * @since 1.31
   */
  public IncomingCommandBuilder getIncomingCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create incoming command for repository {}",
        repository.getName());
    }

    return new IncomingCommandBuilder(cacheManager,
      provider.getIncomingCommand(), repository, preProcessorUtil);
  }

  /**
   * The log command shows revision history of entire repository or files.
   *
   * @return instance of {@link LogCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   */
  public LogCommandBuilder getLogCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create log command for repository {}",
        repository.getName());
    }

    return new LogCommandBuilder(cacheManager, provider.getLogCommand(),
      repository, preProcessorUtil);
  }

  /**
   * The outgoing command show changesets not found in a remote repository.
   *
   *
   * @return instance of {@link OutgoingCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   * @since 1.31
   */
  public OutgoingCommandBuilder getOutgoingCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create outgoing command for repository {}",
        repository.getName());
    }

    return new OutgoingCommandBuilder(cacheManager,
      provider.getOutgoingCommand(), repository, preProcessorUtil);
  }

  /**
   * The pull command pull changes from a other repository.
   *
   * @return instance of {@link PullCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   * @since 1.31
   */
  public PullCommandBuilder getPullCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create pull command for repository {}",
        repository.getName());
    }

    return new PullCommandBuilder(provider.getPullCommand(), repository);
  }

  /**
   * The push command push changes to a other repository.
   *
   * @return instance of {@link PushCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   * @since 1.31
   */
  public PushCommandBuilder getPushCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create push command for repository {}",
        repository.getName());
    }

    return new PushCommandBuilder(provider.getPushCommand());
  }

  /**
   * Returns the repository of this service.
   *
   * @return repository of this service
   */
  public Repository getRepository()
  {
    return repository;
  }

  /**
   * The tags command list all repository tag.
   *
   * @return instance of {@link TagsCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *         by the implementation of the repository service provider.
   */
  public TagsCommandBuilder getTagsCommand()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("create tags command for repository {}",
        repository.getName());
    }

    return new TagsCommandBuilder(cacheManager, provider.getTagsCommand(),
      repository);
  }

  /**
   * Returns true if the command is supported by the repository service.
   *
   *
   * @param command command
   *
   * @return true if the command is supported
   */
  public boolean isSupported(Command command)
  {
    return provider.getSupportedCommands().contains(command);
  }

  /**
   * Returns true if the feature is supported by the repository service.
   *
   *
   * @param feature feature
   *
   * @return true if the feature is supported
   * 
   * @since 1.25
   */
  public boolean isSupported(Feature feature)
  {
    return provider.getSupportedFeatures().contains(feature);
  }

  //~--- fields ---------------------------------------------------------------

  /** cache manager */
  private CacheManager cacheManager;

  /** Field description */
  private PreProcessorUtil preProcessorUtil;

  /** implementation of the repository service provider */
  private RepositoryServiceProvider provider;

  /** the repository */
  private Repository repository;
}
