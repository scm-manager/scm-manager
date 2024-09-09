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

package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.repository.Feature;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.SvnWorkingCopyFactory;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.HookContextFactory;

import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;


public class SvnRepositoryServiceProvider extends RepositoryServiceProvider {

  public static final Set<Command> COMMANDS = ImmutableSet.of(
    Command.BLAME,
    Command.BROWSE,
    Command.CAT,
    Command.DIFF,
    Command.LOG,
    Command.BUNDLE,
    Command.UNBUNDLE,
    Command.MODIFY,
    Command.LOOKUP,
    Command.FULL_HEALTH_CHECK,
    Command.MIRROR,
    Command.FILE_LOCK,
    Command.CHANGESETS
  );

  public static final Set<Feature> FEATURES = EnumSet.of(
    Feature.MODIFICATIONS_BETWEEN_REVISIONS
  );

  private final SvnContext context;
  private final SvnWorkingCopyFactory workingCopyFactory;
  private final HookContextFactory hookContextFactory;
  private final TrustManager trustManager;
  private final GlobalProxyConfiguration globalProxyConfiguration;

  SvnRepositoryServiceProvider(SvnRepositoryHandler handler,
                               Repository repository,
                               SvnWorkingCopyFactory workingCopyFactory,
                               HookContextFactory hookContextFactory,
                               TrustManager trustManager,
                               GlobalProxyConfiguration globalProxyConfiguration) {
    this.context = new SvnContext(repository, handler.getDirectory(repository.getId()));
    this.workingCopyFactory = workingCopyFactory;
    this.hookContextFactory = hookContextFactory;
    this.trustManager = trustManager;
    this.globalProxyConfiguration = globalProxyConfiguration;
  }

  @Override
  public void close() throws IOException {
    Closeables.close(context, true);
  }

  @Override
  public SvnBlameCommand getBlameCommand() {
    return new SvnBlameCommand(context);
  }

  @Override
  public SvnBrowseCommand getBrowseCommand() {
    return new SvnBrowseCommand(context);
  }

  @Override
  public BundleCommand getBundleCommand() {
    return new SvnBundleCommand(context);
  }

  @Override
  public SvnCatCommand getCatCommand() {
    return new SvnCatCommand(context);
  }

  @Override
  public SvnDiffCommand getDiffCommand() {
    return new SvnDiffCommand(context);
  }

  @Override
  public SvnLogCommand getLogCommand() {
    return new SvnLogCommand(context);
  }

  @Override
  public ModificationsCommand getModificationsCommand() {
    return new SvnModificationsCommand(context);
  }

  @Override
  public ModifyCommand getModifyCommand() {
    return new SvnModifyCommand(context, workingCopyFactory);
  }

  @Override
  public LookupCommand getLookupCommand() {
    return new SvnLookupCommand(context);
  }

  @Override
  public Set<Command> getSupportedCommands() {
    return COMMANDS;
  }

  @Override
  public Set<Feature> getSupportedFeatures() {
    return FEATURES;
  }

  @Override
  public UnbundleCommand getUnbundleCommand() {
    return new SvnUnbundleCommand(context, hookContextFactory, new SvnLogCommand(context));
  }

  @Override
  public FullHealthCheckCommand getFullHealthCheckCommand() {
    return new SvnFullHealthCheckCommand(context);
  }

  @Override
  public MirrorCommand getMirrorCommand() {
    return new SvnMirrorCommand(context, trustManager, globalProxyConfiguration);
  }

  @Override
  public FileLockCommand getFileLockCommand() {
    return new SvnFileLockCommand(context);
  }

  @Override
  public ChangesetsCommand getChangesetsCommand() { return new SvnChangesetsCommand(context);}
}
