/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.api;

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Feature;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryReadOnlyChecker;
import sonia.scm.repository.spi.BranchDetailsCommand;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.security.Authentications;
import sonia.scm.user.EMail;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

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
 * @since 1.17
 */
public final class RepositoryService implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryService.class);

  private final CacheManager cacheManager;
  private final PreProcessorUtil preProcessorUtil;
  private final RepositoryServiceProvider provider;

  @Getter
  private final Repository repository;

  @SuppressWarnings({"rawtypes", "java:S3740"})
  private final Set<ScmProtocolProvider> protocolProviders;
  private final WorkdirProvider workdirProvider;

  @Nullable
  private final EMail eMail;
  private final RepositoryExportingCheck repositoryExportingCheck;

  /**
   * Constructs a new {@link RepositoryService}. This constructor should only
   * be called from the {@link RepositoryServiceFactory}.
   *
   * @param cacheManager             cache manager
   * @param provider                 implementation for {@link RepositoryServiceProvider}
   * @param repository               the repository
   * @param workdirProvider          provider for workdirs
   * @param eMail                    utility to compute email addresses if missing
   * @param repositoryExportingCheck
   */
  RepositoryService(CacheManager cacheManager,
                    RepositoryServiceProvider provider,
                    Repository repository,
                    PreProcessorUtil preProcessorUtil,
                    @SuppressWarnings({"rawtypes", "java:S3740"}) Set<ScmProtocolProvider> protocolProviders,
                    WorkdirProvider workdirProvider,
                    @Nullable EMail eMail, RepositoryExportingCheck repositoryExportingCheck) {
    this.cacheManager = cacheManager;
    this.provider = provider;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
    this.protocolProviders = protocolProviders;
    this.workdirProvider = workdirProvider;
    this.eMail = eMail;
    this.repositoryExportingCheck = repositoryExportingCheck;
  }

  /**
   * Closes the connection to the repository and releases all locks
   * and resources. This method should be called in a finally block; e.g.:
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
  public void close() {
    try {
      provider.close();
    } catch (IOException ex) {
      LOG.error("Could not close repository service provider", ex);
    }
  }

  /**
   * Returns true if the command is supported by the repository service.
   *
   * @param command command
   * @return true if the command is supported
   */
  public boolean isSupported(Command command) {
    return provider.getSupportedCommands().contains(command);
  }

  /**
   * Returns true if the feature is supported by the repository service.
   *
   * @param feature feature
   * @return true if the feature is supported
   * @since 1.25
   */
  public boolean isSupported(Feature feature) {
    return provider.getSupportedFeatures().contains(feature);
  }

  /**
   * Creates a {@link BlameCommandBuilder}. It can take the respective parameters and be executed to show
   * changeset information by line for a given file.
   *
   * @return instance of {@link BlameCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public BlameCommandBuilder getBlameCommand() {
    LOG.debug("create blame command for repository {}", repository);

    return new BlameCommandBuilder(cacheManager, provider.getBlameCommand(),
      repository, preProcessorUtil);
  }

  /**
   * Creates a {@link BranchCommandBuilder}. It can take the respective parameters and be executed to
   * create new branches, if supported by the particular SCM system.
   *
   * @return instance of {@link BranchCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public BranchCommandBuilder getBranchCommand() {
    RepositoryReadOnlyChecker.checkReadOnly(getRepository());
    RepositoryPermissions.push(getRepository()).check();
    LOG.debug("create branch command for repository {}", repository);

    return new BranchCommandBuilder(repository, provider.getBranchCommand());
  }

  /**
   * Creates a {@link BranchDetailsCommandBuilder}. It can take the respective parameters and be executed to
   * get details for a branch.
   *
   * @return instance of {@link BranchDetailsCommand}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 2.28.0
   */
  public BranchDetailsCommandBuilder getBranchDetailsCommand() {
    LOG.debug("create branch details command for repository {}", repository);
    return new BranchDetailsCommandBuilder(repository, provider.getBranchDetailsCommand(), cacheManager);
  }

  /**
   * Creates a {@link BranchesCommandBuilder}. It can take the respective parameters and be executed to list
   * all repository branches.
   *
   * @return instance of {@link BranchesCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public BranchesCommandBuilder getBranchesCommand() {
    LOG.debug("create branches command for repository {}", repository);

    return new BranchesCommandBuilder(cacheManager,
      provider.getBranchesCommand(), repository);
  }

  /**
   * Creates a {@link BrowseCommandBuilder}. It can take the respective parameters and be executed to
   * browse for content within a repository.
   *
   * @return instance of {@link BrowseCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public BrowseCommandBuilder getBrowseCommand() {
    LOG.debug("create browse command for repository {}", repository);

    return new BrowseCommandBuilder(cacheManager, provider.getBrowseCommand(),
      repository, preProcessorUtil, provider::getBrowseCommand);
  }

  /**
   * Creates a {@link BundleCommandBuilder}. It can take the respective parameters and be executed to
   * create an archive from the repository.
   *
   * @return instance of {@link BundleCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 1.43
   */
  public BundleCommandBuilder getBundleCommand() {
    LOG.debug("create bundle command for repository {}", repository);

    return new BundleCommandBuilder(provider.getBundleCommand(), repositoryExportingCheck, repository);
  }

  /**
   * Creates a {@link CatCommandBuilder}. It can take the respective parameters and be executed to
   * show the content of a given file.
   *
   * @return instance of {@link CatCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public CatCommandBuilder getCatCommand() {
    LOG.debug("create cat command for repository {}", repository);

    return new CatCommandBuilder(provider.getCatCommand());
  }

  /**
   * Creates a {@link ChangesetsCommandBuilder}. It can take the respective parameters and be executed to
   * retrieve a set of at least one changeset.
   *
   * @return Instance of {@link ChangesetsCommandBuilder}.
   * @throws CommandNotSupportedException if the command is not supported by
   *                                      the implementation of the {@link RepositoryServiceProvider}.
   */
  public ChangesetsCommandBuilder getChangesetsCommand() {
    LOG.debug("create changesets command for repository {}", repository);
    return new ChangesetsCommandBuilder(repository, provider.getChangesetsCommand());
  }

  /**
   * Creates a {@link DiffCommandBuilder}. It can take the respective parameters and be executed to
   * show differences between revisions for a specified file or the entire revision.
   *
   * @return instance of {@link DiffCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public DiffCommandBuilder getDiffCommand() {
    LOG.debug("create diff command for repository {}", repository);

    return new DiffCommandBuilder(provider.getDiffCommand(), provider.getSupportedFeatures());
  }

  /**
   * Creates a {@link DiffResultCommandBuilder}. It can take the respective parameters and be executed to
   * show differences between revisions for a specified file or the entire revision.
   *
   * @return instance of {@link DiffResultCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public DiffResultCommandBuilder getDiffResultCommand() {
    LOG.debug("create diff result command for repository {}", repository);

    return new DiffResultCommandBuilder(provider.getDiffResultCommand(), provider.getSupportedFeatures());
  }

  /**
   * Creates a {@link FullHealthCheckCommandBuilder}. It can take the respective parameters and be executed to
   * inspect a repository profoundly. This might take a while in contrast to the lighter checks executed at startup.
   *
   * @return instance of {@link FullHealthCheckCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 2.17.0
   */
  public FullHealthCheckCommandBuilder getFullCheckCommand() {
    LOG.debug("create full check command for repository {}", repository);
    return new FullHealthCheckCommandBuilder(provider.getFullHealthCheckCommand());
  }

  /**
   * Creates a {@link FileLockCommandBuilder}. It can take the respective parameters and be executed to
   * lock and unlock files.
   *
   * @return instance of {@link FileLockCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 2.26.0
   */
  public FileLockCommandBuilder getLockCommand() {
    LOG.debug("create lock command for repository {}", repository);
    return new FileLockCommandBuilder(provider.getFileLockCommand(), repository);
  }

  /**
   * Creates a {@link IncomingCommandBuilder}. It can take the respective parameters and be executed to
   * show new {@link Changeset}s found in a different repository location.
   *
   * @return instance of {@link IncomingCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 1.31
   */
  public IncomingCommandBuilder getIncomingCommand() {
    LOG.debug("create incoming command for repository {}", repository);

    return new IncomingCommandBuilder(cacheManager,
      provider.getIncomingCommand(), repository, preProcessorUtil);
  }

  /**
   * Creates a {@link LogCommandBuilder}. It can take the respective parameters and be executed to
   * show revision history of entire repository or files.
   *
   * @return instance of {@link LogCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public LogCommandBuilder getLogCommand() {
    LOG.debug("create log command for repository {}", repository);

    return new LogCommandBuilder(cacheManager, provider.getLogCommand(),
      repository, preProcessorUtil, provider.getSupportedFeatures());
  }

  /**
   * Creates a {@link LookupCommandBuilder}. It can take the respective parameters and be executed to
   * conduct a lookup which returns additional information for the repository.
   *
   * @return instance of {@link LookupCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 2.10.0
   */
  public LookupCommandBuilder getLookupCommand() {
    LOG.debug("create lookup command for repository {}", repository);
    return new LookupCommandBuilder(provider.getLookupCommand());
  }

  /**
   * Creates a {@link MergeCommandBuilder}. It can take the respective parameters and be executed to
   * conduct a merge of two branches.
   *
   * @return instance of {@link MergeCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 2.0.0
   */
  public MergeCommandBuilder getMergeCommand() {
    RepositoryReadOnlyChecker.checkReadOnly(getRepository());
    LOG.debug("create merge command for repository {}", repository);

    return new MergeCommandBuilder(provider.getMergeCommand(), eMail);
  }

  /**
   * Creates a {@link MirrorCommandBuilder}. It can take the respective parameters and be executed to
   * create a 'mirror' of an existing repository (specified by a URL) by copying all data
   * to the repository of this service. Therefore, this repository has to be empty (otherwise the behaviour is
   * not specified).
   *
   * @return instance of {@link MirrorCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 2.19.0
   */
  public MirrorCommandBuilder getMirrorCommand() {
    LOG.debug("create mirror command for repository {}", repository);
    return new MirrorCommandBuilder(provider.getMirrorCommand(), repository);
  }

  /**
   * Creates a {@link ModificationsCommandBuilder}. It can take the respective parameters and be executed to
   * show file modifications in a revision.
   *
   * @return instance of {@link ModificationsCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public ModificationsCommandBuilder getModificationsCommand() {
    LOG.debug("create modifications command for repository {}", repository);
    return new ModificationsCommandBuilder(provider.getModificationsCommand(), repository, cacheManager.getCache(ModificationsCommandBuilder.CACHE_NAME), preProcessorUtil);
  }

  /**
   * Creates a {@link ModifyCommandBuilder}. It can take the respective parameters and be executed to
   * makes changes to the files within a changeset.
   *
   * @return instance of {@link ModifyCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @see ModifyCommandBuilder
   * @since 2.0.0
   */
  public ModifyCommandBuilder getModifyCommand() {
    RepositoryReadOnlyChecker.checkReadOnly(getRepository());
    LOG.debug("create modify command for repository {}", repository);

    return new ModifyCommandBuilder(provider.getModifyCommand(), workdirProvider, repository.getId(), eMail);
  }

  /**
   * Creates an {@link OutgoingCommandBuilder}. It can take the respective parameters and be executed to
   * show {@link Changeset}s not found in a remote repository.
   *
   * @return instance of {@link OutgoingCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 1.31
   */
  public OutgoingCommandBuilder getOutgoingCommand() {
    LOG.debug("create outgoing command for repository {}", repository);

    return new OutgoingCommandBuilder(cacheManager,
      provider.getOutgoingCommand(), repository, preProcessorUtil);
  }

  /**
   * Creates a {@link PullCommandBuilder}. It can take the respective parameters and be executed to
   * pull changes from another repository.
   *
   * @return instance of {@link PullCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 1.31
   */
  public PullCommandBuilder getPullCommand() {
    RepositoryReadOnlyChecker.checkReadOnly(getRepository());
    LOG.debug("create pull command for repository {}", repository);

    return new PullCommandBuilder(provider.getPullCommand(), repository);
  }

  /**
   * Creates a {@link PushCommandBuilder}. It can take the respective parameters and be executed to
   * push changes to another repository.
   *
   * @return instance of {@link PushCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 1.31
   */
  public PushCommandBuilder getPushCommand() {
    LOG.debug("create push command for repository {}", repository);

    return new PushCommandBuilder(provider.getPushCommand(), provider.getSupportedFeatures());
  }

  /**
   * Creates a {@link RevertCommandBuilder}. It can take the respective parameters and be executed to
   * apply a revert of a chosen changeset onto the given repository/branch combination.
   *
   * @return Instance of {@link RevertCommandBuilder}.
   * @throws CommandNotSupportedException if the command is not supported by
   *                                      the implementation of the {@link RepositoryServiceProvider}.
   * @since 3.8
   * @see RevertCommandBuilder
   */
  public RevertCommandBuilder getRevertCommand() {
    LOG.debug("create revert command for repository {}", repository);
    return new RevertCommandBuilder(provider.getRevertCommand(), eMail);
  }

  /**
   * The tags command list all repository tag.
   *
   * @return instance of {@link TagsCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public TagsCommandBuilder getTagsCommand() {
    LOG.debug("create tags command for repository {}", repository);

    return new TagsCommandBuilder(cacheManager, provider.getTagsCommand(),
      repository);
  }

  /**
   * The tag command allows the management of repository tags.
   *
   * @return instance of {@link TagCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   */
  public TagCommandBuilder getTagCommand() {
    RepositoryReadOnlyChecker.checkReadOnly(getRepository());
    return new TagCommandBuilder(provider.getTagCommand());
  }

  /**
   * Creates an {@link UnbundleCommandBuilder}. It can take the respective parameters and be executed to
   * restore a repository from the given bundle.
   *
   * @return instance of {@link UnbundleCommandBuilder}
   * @throws CommandNotSupportedException if the command is not supported
   *                                      by the implementation of the repository service provider.
   * @since 1.43
   */
  public UnbundleCommandBuilder getUnbundleCommand() {
    LOG.debug("create unbundle command for repository {}", repository);

    return new UnbundleCommandBuilder(provider.getUnbundleCommand(),
      repository);
  }

  public Stream<ScmProtocol> getSupportedProtocols() {
    return protocolProviders.stream()
      .filter(protocolProvider -> protocolProvider.getType().equals(getRepository().getType()))
      .map(this::createProviderInstanceForRepository)
      .filter(protocol -> !Authentications.isAuthenticatedSubjectAnonymous() || protocol.isAnonymousEnabled());
  }

  @SuppressWarnings({"rawtypes", "java:S3740"})
  private ScmProtocol createProviderInstanceForRepository(ScmProtocolProvider protocolProvider) {
    return protocolProvider.get(repository);
  }

  @SuppressWarnings("unchecked")
  public <T extends ScmProtocol> T getProtocol(Class<T> clazz) {
    return this.getSupportedProtocols()
      .filter(scmProtocol -> clazz.isAssignableFrom(scmProtocol.getClass()))
      // no idea how to fix this, without cast
      .map(p -> (T) p)
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("no implementation for %s and repository type %s", clazz.getName(), getRepository().getType())));
  }
}
