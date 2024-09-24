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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.work.SimpleWorkingCopyFactory.ReclaimFailedException;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SimpleCachingWorkingCopyPoolTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "X");

  @Mock
  WorkdirProvider workdirProvider;
  MeterRegistry meterRegistry = new SimpleMeterRegistry();

  SimpleCachingWorkingCopyPool simpleCachingWorkingCopyPool;

  @Mock
  SimpleWorkingCopyFactory<Object, Path, ?>.WorkingCopyContext workingCopyContext;

  @BeforeEach
  void initContext() throws ReclaimFailedException {
    lenient().when(workingCopyContext.initialize(any()))
      .thenAnswer(invocationOnMock -> new WorkingCopy<>(null, null, () -> {}, invocationOnMock.getArgument(0, File.class)));
    lenient().when(workingCopyContext.reclaim(any()))
      .thenAnswer(invocationOnMock -> new WorkingCopy<>(null, null, () -> {}, invocationOnMock.getArgument(0, File.class)));
  }

  @Nested
  class WithCache {

    @BeforeEach
    void initContext() {
      simpleCachingWorkingCopyPool = new SimpleCachingWorkingCopyPool(2, workdirProvider, meterRegistry);
    }

    @Test
    void shouldCreateNewWorkdirForTheFirstRequest(@TempDir Path temp) {
      when(workingCopyContext.getScmRepository()).thenReturn(REPOSITORY);
      when(workdirProvider.createNewWorkdir(anyString())).thenReturn(temp.toFile());

      WorkingCopy<?, ?> workdir = simpleCachingWorkingCopyPool.getWorkingCopy(workingCopyContext);

      verify(workingCopyContext).initialize(temp.toFile());
      assertThat(meterRegistry.get("scm.workingcopy.pool.cache.miss").counter().count()).isEqualTo(1d);
    }

    @Test
    void shouldReuseWorkdirForTheSameRepository(@TempDir Path temp) throws ReclaimFailedException {
      when(workingCopyContext.getScmRepository()).thenReturn(REPOSITORY);
      when(workdirProvider.createNewWorkdir(anyString())).thenReturn(temp.toFile());

      WorkingCopy<?, ?> firstWorkdir = simpleCachingWorkingCopyPool.getWorkingCopy(workingCopyContext);
      simpleCachingWorkingCopyPool.contextClosed(workingCopyContext, firstWorkdir.getDirectory());
      WorkingCopy<?, ?> secondWorkdir = simpleCachingWorkingCopyPool.getWorkingCopy(workingCopyContext);

      verify(workingCopyContext).initialize(temp.toFile());
      verify(workingCopyContext).reclaim(temp.toFile());
      assertThat(secondWorkdir.getDirectory()).isEqualTo(temp.toFile());
      assertThat(meterRegistry.get("scm.workingcopy.pool.cache.hit").counter().count()).isEqualTo(1d);
    }

    @Test
    void shouldCreateNewWorkdirIfReclaimFails(@TempDir Path temp) throws ReclaimFailedException {
      when(workingCopyContext.getScmRepository()).thenReturn(REPOSITORY);
      when(workdirProvider.createNewWorkdir(anyString())).thenReturn(temp.resolve("1").toFile(), temp.resolve("2").toFile());
      when(workingCopyContext.reclaim(any())).thenThrow(ReclaimFailedException.class);

      WorkingCopy<?, ?> firstWorkdir = simpleCachingWorkingCopyPool.getWorkingCopy(workingCopyContext);
      simpleCachingWorkingCopyPool.contextClosed(workingCopyContext, firstWorkdir.getDirectory());
      WorkingCopy<?, ?> secondWorkdir = simpleCachingWorkingCopyPool.getWorkingCopy(workingCopyContext);

      assertThat(secondWorkdir.getDirectory()).isNotEqualTo(temp.toFile());
      assertThat(meterRegistry.get("scm.workingcopy.pool.reclaim.failure").counter().count()).isEqualTo(1d);
    }

    @Test
    void shouldDeleteWorkdirIfCacheSizeReached(@TempDir Path temp) {
      fillPool(temp, 3);
      assertThat(temp.resolve("path-0")).doesNotExist();
      assertThat(temp.resolve("path-1")).exists();
      assertThat(temp.resolve("path-2")).exists();
      assertThat(meterRegistry.get("scm.workingcopy.pool.cache.overflow").counter().count()).isEqualTo(1d);
    }

    @Test
    void shouldReorderUsedWorkdirsInCache(@TempDir Path temp) {
      fillPool(temp, 2);
      queryAndCloseWorkdir(temp, 0); // querying first repository again should keep it from eviction
      queryAndCloseWorkdir(temp, 2);
      assertThat(temp.resolve("path-0")).exists();
      assertThat(temp.resolve("path-1")).doesNotExist();
      assertThat(temp.resolve("path-2")).exists();
    }
  }

  @Nested
  class WithoutCaching {

    @BeforeEach
    void initContext() {
      simpleCachingWorkingCopyPool = new SimpleCachingWorkingCopyPool(0, workdirProvider, meterRegistry);
    }

    @Test
    void shouldNotCacheAnything(@TempDir Path temp) {
      fillPool(temp, 2);
      assertThat(temp.resolve("path-0")).doesNotExist();
      assertThat(temp.resolve("path-1")).doesNotExist();
    }
  }

  private void fillPool(Path temp, int size) {
    for (int i = 0; i < size; ++i) {
      queryAndCloseWorkdir(temp, i);
    }
  }

  private void queryAndCloseWorkdir(Path temp, int index) {
    Repository repository = new Repository("repo-" + index, "git", "space", "X" + index);
    when(workingCopyContext.getScmRepository()).thenReturn(repository);
    String workdirName = "path-" + index;
    lenient().doAnswer(invocation -> {
      File newWorkdir = temp.resolve(workdirName).toFile();
      newWorkdir.mkdirs();
      return newWorkdir;
    }).when(workdirProvider).createNewWorkdir(anyString());
    WorkingCopy<Object, Path> workingCopy = simpleCachingWorkingCopyPool.getWorkingCopy(workingCopyContext);
    simpleCachingWorkingCopyPool.contextClosed(workingCopyContext, workingCopy.getDirectory());
  }
}
