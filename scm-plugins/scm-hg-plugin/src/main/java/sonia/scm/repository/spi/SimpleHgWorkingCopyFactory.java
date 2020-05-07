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

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CloneCommand;
import com.aragost.javahg.commands.ExecutionException;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.StatusCommand;
import com.aragost.javahg.commands.UpdateCommand;
import com.aragost.javahg.commands.flags.CloneCommandFlags;
import sonia.scm.repository.util.WorkingCopyPool;
import sonia.scm.repository.util.SimpleWorkingCopyFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

public class SimpleHgWorkingCopyFactory extends SimpleWorkingCopyFactory<Repository, Repository, HgCommandContext> implements HgWorkingCopyFactory {

  private final Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder;

  @Inject
  public SimpleHgWorkingCopyFactory(Provider<HgRepositoryEnvironmentBuilder> hgRepositoryEnvironmentBuilder, WorkingCopyPool workdirProvider) {
    super(workdirProvider);
    this.hgRepositoryEnvironmentBuilder = hgRepositoryEnvironmentBuilder;
  }
  @Override
  public ParentAndClone<Repository, Repository> cloneRepository(HgCommandContext context, File target, String initialBranch) throws IOException {
    Repository centralRepository = openCentral(context);
    CloneCommand cloneCommand = CloneCommandFlags.on(centralRepository);
    if (initialBranch != null) {
      cloneCommand.updaterev(initialBranch);
    }
    cloneCommand.execute(target.getAbsolutePath());

    BaseRepository clone = Repository.open(target);

    return new ParentAndClone<>(centralRepository, clone, target);
  }

  public Repository openCentral(HgCommandContext context) {
    BiConsumer<sonia.scm.repository.Repository, Map<String, String>> repositoryMapBiConsumer =
      (repository, environment) -> hgRepositoryEnvironmentBuilder.get().buildFor(repository, null, environment);
    return context.openWithSpecialEnvironment(repositoryMapBiConsumer);
  }

  @Override
  protected ParentAndClone<Repository, Repository> reclaimRepository(HgCommandContext context, File target, String initialBranch) throws ReclaimFailedException {
    Repository centralRepository = openCentral(context);
    try {
      BaseRepository clone = Repository.open(target);
      for (String unknown : StatusCommand.on(clone).execute().getUnknown()) {
        delete(clone.getDirectory(), unknown);
      }
      UpdateCommand.on(clone).rev(initialBranch).clean().execute();
      return new ParentAndClone<>(centralRepository, clone, target);
    } catch (ExecutionException | IOException e) {
      throw new ReclaimFailedException(e);
    }
  }

  private void delete(File directory, String unknownFile) throws IOException {
    IOUtil.delete(new File(directory, unknownFile));
  }

  @Override
  protected void closeRepository(Repository repository) {
    repository.close();
  }

  @Override
  protected void closeWorkingCopyInternal(Repository workingCopy) throws Exception {
    workingCopy.close();
  }

  @Override
  protected sonia.scm.repository.Repository getScmRepository(HgCommandContext context) {
    return context.getScmRepository();
  }

  @Override
  public void configure(PullCommand pullCommand) {
    pullCommand.cmdAppend("--config", "hooks.changegroup.scm=python:scmhooks.postHook");
    pullCommand.cmdAppend("--config", "hooks.pretxnchangegroup.scm=python:scmhooks.preHook");
  }
}
