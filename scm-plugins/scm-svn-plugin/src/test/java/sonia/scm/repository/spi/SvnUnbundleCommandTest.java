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

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.SvnUtil;
import sonia.scm.repository.api.HookChangesetBuilder;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.UnbundleResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvnUnbundleCommandTest extends AbstractSvnCommandTestBase {

  private final Repository repository = RepositoryTestData.createHeartOfGold("svn");
  private HookContextFactory hookContextFactory;
  private SvnLogCommand logCommand;
  private HookChangesetBuilder hookChangesetBuilder;

  @Before
  public void initMocks() {
    hookContextFactory = mock(HookContextFactory.class);
    logCommand = mock(SvnLogCommand.class);
    HookContext hookContext = mock(HookContext.class);
    hookChangesetBuilder = mock(HookChangesetBuilder.class);
    when(hookContextFactory.createContext(any(), eq(repository))).thenReturn(hookContext);
    when(hookContext.getChangesetProvider()).thenReturn(hookChangesetBuilder);
  }

  @Test
  public void shouldFireRepositoryHookEventAfterUnbundle() throws IOException, SVNException {
    Changeset first = new Changeset("1", 0L, new Person("trillian"), "first commit");
    when(hookChangesetBuilder.getChangesetList()).thenReturn(ImmutableList.of(first));

    File bundle = bundle();
    SvnContext ctx = createEmptyContext();

    AtomicReference<RepositoryHookEvent> eventSink = new AtomicReference<>();
    UnbundleCommandRequest request = new UnbundleCommandRequest(Files.asByteSource(bundle));
    request.setPostEventSink(eventSink::set);

    UnbundleResponse res = new SvnUnbundleCommand(ctx, hookContextFactory, logCommand)
      .unbundle(request);

    assertThat(res).isNotNull();
    assertThat(res.getChangesetCount()).isEqualTo(5);
    SVNRepository repo = ctx.open();
    assertThat(repo.getLatestRevision()).isEqualTo(5);

    RepositoryHookEvent event = eventSink.get();
    List<Changeset> changesets = event.getContext().getChangesetProvider().getChangesetList();
    assertThat(changesets).hasSize(1).contains(first);

    SvnUtil.closeSession(repo);
  }

  private File bundle() throws IOException {
    File file = tempFolder.newFile();

    //J-
    new SvnBundleCommand(createContext())
      .bundle(new BundleCommandRequest(Files.asByteSink(file))
      );
    //J+

    return file;
  }

  private SvnContext createEmptyContext() throws IOException, SVNException {
    File folder = tempFolder.newFolder();

    SVNRepositoryFactory.createLocalRepository(folder, true, true);

    return new SvnContext(repository, folder);
  }
}
