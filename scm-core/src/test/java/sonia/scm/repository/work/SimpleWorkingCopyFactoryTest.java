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

package sonia.scm.repository.work;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.util.IOUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleWorkingCopyFactoryTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "X");

  private final Closeable parent = mock(Closeable.class);
  private final Closeable clone = mock(Closeable.class);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private SimpleWorkingCopyFactory<Closeable, Closeable, Context> simpleWorkingCopyFactory;

  private final RepositoryLocationResolver repositoryLocationResolver = mock(RepositoryLocationResolver.class);

  private String initialBranchForLastCloneCall;

  private boolean workdirIsCached = false;
  private File workdir;

  private MeterRegistry meterRegistry;

  @Before
  public void initFactory() throws IOException {
    meterRegistry = new SimpleMeterRegistry();
    WorkdirProvider workdirProvider = new WorkdirProvider(temporaryFolder.newFolder(), repositoryLocationResolver, false);
    WorkingCopyPool configurableTestWorkingCopyPool = new WorkingCopyPool() {
      @Override
      public <R, W> WorkingCopy<R, W> getWorkingCopy(SimpleWorkingCopyFactory<R, W, ?>.WorkingCopyContext context) {
        workdir = workdirProvider.createNewWorkdir(context.getScmRepository().getId());
        return context.initialize(workdir);
      }

      @Override
      public void contextClosed(SimpleWorkingCopyFactory<?, ?, ?>.WorkingCopyContext createWorkdirContext, File workdir) {
        if (!workdirIsCached) {
          IOUtil.deleteSilently(workdir);
        }
      }

      @Override
      public void shutdown() {
      }
    };
    simpleWorkingCopyFactory = new SimpleWorkingCopyFactory<Closeable, Closeable, Context>(configurableTestWorkingCopyPool, meterRegistry) {
      @Override
      protected void closeRepository(Closeable repository) throws IOException {
        repository.close();
      }

      @Override
      protected ParentAndClone<Closeable, Closeable> initialize(Context context, File target, String initialBranch) {
        initialBranchForLastCloneCall = initialBranch;
        return new ParentAndClone<>(parent, clone, target);
      }

      @Override
      protected ParentAndClone<Closeable, Closeable> reclaim(Context context, File target, String initialBranch) throws ReclaimFailedException {
        throw new UnsupportedOperationException();
      }

      @Override
      protected void closeWorkingCopy(Closeable workingCopy) throws Exception {
        workingCopy.close();
      }
    };

    RepositoryLocationResolver.RepositoryLocationResolverInstance instanceMock = mock(RepositoryLocationResolver.RepositoryLocationResolverInstance.class);
    when(repositoryLocationResolver.forClass(Path.class)).thenReturn(instanceMock);
    when(instanceMock.getLocation(anyString())).thenAnswer(invocation -> temporaryFolder.newFolder(invocation.getArgument(0, String.class)).toPath());
  }

  @Test
  public void shouldCreateParentAndClone() {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {
      assertThat(workingCopy.getCentralRepository()).isSameAs(parent);
      assertThat(workingCopy.getWorkingRepository()).isSameAs(clone);
    }
  }

  @Test
  public void shouldCloseParent() throws IOException {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    verify(parent).close();
    assertThat(meterRegistry.getMeters()).hasSize(1);
    Meter.Id meterId = meterRegistry.getMeters().get(0).getId();
    assertThat(meterId.getName()).isEqualTo("scm.workingcopy.duration");
    assertThat(meterId.getTag("type")).isEqualTo("git");
  }

  @Test
  public void shouldCloseClone() throws IOException {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    verify(clone).close();
    assertThat(meterRegistry.getMeters()).hasSize(1);
    Meter.Id meterId = meterRegistry.getMeters().get(0).getId();
    assertThat(meterId.getName()).isEqualTo("scm.workingcopy.duration");
    assertThat(meterId.getTag("type")).isEqualTo("git");
  }

  @Test
  public void shouldDeleteWorkdirIfNotCached() {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    assertThat(workdir).doesNotExist();
  }

  @Test
  public void shouldNotDeleteWorkdirIfCached() {
    Context context = new Context();
    workdirIsCached = true;
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    assertThat(workdir).exists();
  }

  @Test
  public void shouldPropagateInitialBranch() {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, "some")) {
      assertThat(initialBranchForLastCloneCall).isEqualTo("some");
    }
  }

  private static class Context implements RepositoryProvider {
    @Override
    public Repository get() {
      return REPOSITORY;
    }
  }
}
