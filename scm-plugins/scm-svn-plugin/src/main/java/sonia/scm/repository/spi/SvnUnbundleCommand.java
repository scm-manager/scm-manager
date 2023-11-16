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
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.admin.SVNAdminClient;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static sonia.scm.repository.RepositoryHookType.POST_RECEIVE;

public class SvnUnbundleCommand extends AbstractSvnCommand implements UnbundleCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SvnUnbundleCommand.class);
  private final HookContextFactory hookContextFactory;
  private final SvnLogCommand svnLogCommand;

  public SvnUnbundleCommand(SvnContext context,
                            HookContextFactory hookContextFactory,
                            SvnLogCommand svnLogCommand
  ) {
    super(context);
    this.hookContextFactory = hookContextFactory;
    this.svnLogCommand = svnLogCommand;
  }

  @Override
  public UnbundleResponse unbundle(UnbundleCommandRequest request)
    throws IOException {
    ByteSource archive = checkNotNull(request.getArchive(),
      "archive is required");

    LOG.debug("archive repository {} to {}", context.getDirectory(),
      archive);

    UnbundleResponse response;

    SVNClientManager clientManager = null;

    try {
      clientManager = SVNClientManager.newInstance();

      SVNAdminClient adminClient = clientManager.getAdminClient();

      restore(adminClient, archive, context.getDirectory());

      response = new UnbundleResponse(context.open().getLatestRevision());
    } catch (SVNException ex) {
      throw new IOException("could not restore dump", ex);
    } finally {
      SvnUtil.dispose(clientManager);
    }

    fireHookEvent(request);
    return response;
  }

  private void fireHookEvent(UnbundleCommandRequest request) {
    request.getPostEventSink().accept(createEvent());
  }

  private RepositoryHookEvent createEvent() {
    Repository repository = this.context.getRepository();
    HookContext context = hookContextFactory.createContext(new SvnImportHookContextProvider(repository, svnLogCommand), repository);
    return new RepositoryHookEvent(context, repository, POST_RECEIVE);
  }

  private void restore(SVNAdminClient adminClient, ByteSource dump, File repository) throws SVNException, IOException {
    InputStream inputStream = null;

    try {
      inputStream = dump.openBufferedStream();
      adminClient.doLoad(repository, inputStream);
    } finally {
      Closeables.close(inputStream, true);
    }
  }

  private static class SvnImportHookContextProvider extends HookContextProvider {

    private final Repository repository;
    private final LogCommand logCommand;

    private SvnImportHookContextProvider(Repository repository, SvnLogCommand logCommand) {
      this.repository = repository;
      this.logCommand = logCommand;
    }

    @Override
    public Set<HookFeature> getSupportedFeatures() {
      return ImmutableSet.of(HookFeature.CHANGESET_PROVIDER);
    }

    @Override
    public HookChangesetProvider getChangesetProvider() {
      ChangesetResolver changesetResolver = new ChangesetResolver(repository, logCommand);
      return r -> new HookChangesetResponse(changesetResolver.call());
    }
  }

  private static class ChangesetResolver implements Callable<Iterable<Changeset>> {

    private final Repository repository;
    private final LogCommand logCommand;

    ChangesetResolver(Repository repository, LogCommand logCommand) {
      this.repository = repository;
      this.logCommand = logCommand;
    }

    @Override
    public Iterable<Changeset> call() {
      return SingleLogRequestChangesetIterator::new;
    }

    private class SingleLogRequestChangesetIterator implements Iterator<Changeset> {

      private int currentNumber = 0;
      private Changeset nextChangeset;

      SingleLogRequestChangesetIterator() {
        prefetch();
      }

      @Override
      public boolean hasNext() {
        return nextChangeset != null;
      }

      @Override
      public Changeset next() {
        if (nextChangeset == null) {
          throw new NoSuchElementException();
        }
        Changeset currentChangeset = nextChangeset;
        prefetch();
        return currentChangeset;
      }

      private void prefetch() {
        try {
          List<Changeset> changesets = fetchSingleChangesetPage();
          if (changesets.isEmpty()) {
            nextChangeset = null;
          } else {
            nextChangeset = changesets.get(0);
          }
        } catch (IOException e) {
          throw new ImportFailedException(
            ContextEntry.ContextBuilder.entity(repository).build(),
            "Could not provide changeset nr " + currentNumber + " for imported repository",
            e
          );
        }
      }

      private List<Changeset> fetchSingleChangesetPage() throws IOException {
        LogCommandRequest request = new LogCommandRequest();
        request.setPagingStart(currentNumber);
        request.setPagingLimit(1);
        List<Changeset> changesets = logCommand.getChangesets(request).getChangesets();
        currentNumber = currentNumber + 1;
        return changesets;
      }
    }
  }
}
