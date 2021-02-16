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

import com.aragost.javahg.Branch;
import com.aragost.javahg.Repository;
import com.google.common.io.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static sonia.scm.util.Archives.extractTar;

public class HgUnbundleCommand implements UnbundleCommand {
  private static final Logger LOG = LoggerFactory.getLogger(HgUnbundleCommand.class);

  private final HgCommandContext context;
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;

  HgUnbundleCommand(HgCommandContext context, HookContextFactory hookContextFactory, ScmEventBus eventBus) {
    this.context = context;
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
  }

  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request) throws IOException {
    ByteSource archive = checkNotNull(request.getArchive(), "archive is required");
    Path repositoryDir = context.getDirectory().toPath();
    LOG.debug("archive repository {} to {}", repositoryDir, archive);

    if (!Files.exists(repositoryDir)) {
      Files.createDirectories(repositoryDir);
    }

    unbundleRepositoryFromRequest(request, repositoryDir);
    firePostReceiveRepositoryHookEvent();
    return new UnbundleResponse(0);
  }

  private void firePostReceiveRepositoryHookEvent() {
    Repository repository = context.open();
    List<String> branches = extractBranches(repository);
    List<Tag> tags = extractTags(repository);
    HgLazyChangesetResolver changesetResolver = new HgLazyChangesetResolver(repository);
    eventBus.post(createEvent(branches, tags, changesetResolver));
  }

  private List<Tag> extractTags(Repository repository) {
    return com.aragost.javahg.commands.TagsCommand.on(repository).execute().stream()
      .map(t -> new Tag(t.getName(), t.getChangeset().toString()))
      .collect(Collectors.toList());
  }

  private List<String> extractBranches(Repository repository) {
    return com.aragost.javahg.commands.BranchesCommand.on(repository).execute().stream()
      .map(Branch::getName)
      .collect(Collectors.toList());
  }

  private PostReceiveRepositoryHookEvent createEvent(List<String> branches, List<Tag> tags, HgLazyChangesetResolver changesetResolver) {
    HgImportHookContextProvider contextProvider = new HgImportHookContextProvider(branches, tags, changesetResolver);
    HookContext context = hookContextFactory.createContext(contextProvider, this.context.getScmRepository());
    RepositoryHookEvent repositoryHookEvent = new RepositoryHookEvent(context, this.context.getScmRepository(), RepositoryHookType.POST_RECEIVE);
    return new PostReceiveRepositoryHookEvent(repositoryHookEvent);
  }

  private void unbundleRepositoryFromRequest(UnbundleCommandRequest request, Path repositoryDir) throws IOException {
    extractTar(request.getArchive().openBufferedStream(), repositoryDir).run();
  }
}
