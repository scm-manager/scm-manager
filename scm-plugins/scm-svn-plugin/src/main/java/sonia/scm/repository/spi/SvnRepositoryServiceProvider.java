/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.spi;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.SvnWorkingCopyFactory;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.HookContextFactory;

import java.io.IOException;
import java.util.Set;

/**
 * @author Sebastian Sdorra
 */
public class SvnRepositoryServiceProvider extends RepositoryServiceProvider {

  //J-
  public static final Set<Command> COMMANDS = ImmutableSet.of(
    Command.BLAME, Command.BROWSE, Command.CAT, Command.DIFF,
    Command.LOG, Command.BUNDLE, Command.UNBUNDLE, Command.MODIFY, Command.LOOKUP
  );
  //J+


  private final SvnContext context;
  private final SvnWorkingCopyFactory workingCopyFactory;
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;

  SvnRepositoryServiceProvider(SvnRepositoryHandler handler,
                               Repository repository,
                               SvnWorkingCopyFactory workingCopyFactory,
                               HookContextFactory hookContextFactory,
                               ScmEventBus eventBus) {
    this.context = new SvnContext(repository, handler.getDirectory(repository.getId()));
    this.workingCopyFactory = workingCopyFactory;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
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
  public UnbundleCommand getUnbundleCommand() {
    return new SvnUnbundleCommand(context, hookContextFactory, eventBus, new SvnLogCommand(context));
  }
}
