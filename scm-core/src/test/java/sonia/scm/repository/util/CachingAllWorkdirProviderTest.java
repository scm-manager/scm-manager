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

package sonia.scm.repository.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, TempDirectory.class})
class CachingAllWorkdirProviderTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "X");

  @Mock
  WorkdirProvider workdirProvider;
  @InjectMocks
  CachingAllWorkdirProvider cachingAllWorkdirProvider;

  @Mock
  CreateWorkdirContext<Object, Path, Path> createWorkdirContext;
  @Mock
  SimpleWorkdirFactory.WorkdirInitializer<Object, Path> initializer;
  @Mock
  SimpleWorkdirFactory.WorkdirReclaimer<Object, Path> reclaimer;

  @BeforeEach
  void initContext() throws IOException, SimpleWorkdirFactory.ReclaimFailedException {
    lenient().when(createWorkdirContext.getInitializer()).thenReturn(initializer);
    lenient().when(createWorkdirContext.getReclaimer()).thenReturn(reclaimer);

    lenient().when(initializer.initialize(any()))
      .thenAnswer(invocationOnMock -> new SimpleWorkdirFactory.ParentAndClone<Object, Object>(null, null, invocationOnMock.getArgument(0, File.class)));
    lenient().when(reclaimer.reclaim(any()))
      .thenAnswer(invocationOnMock -> new SimpleWorkdirFactory.ParentAndClone<Object, Object>(null, null, invocationOnMock.getArgument(0, File.class)));
  }

  @Test
  void shouldCreateNewWorkdirForTheFirstRequest(@TempDirectory.TempDir Path temp) throws IOException {
    when(createWorkdirContext.getScmRepository()).thenReturn(REPOSITORY);
    when(workdirProvider.createNewWorkdir()).thenReturn(temp.toFile());

    SimpleWorkdirFactory.ParentAndClone<?, ?> workdir = cachingAllWorkdirProvider.getWorkdir(createWorkdirContext);

    verify(initializer).initialize(temp.toFile());
  }

  @Test
  void shouldCreateWorkdirOnlyOnceForTheSameRepository(@TempDirectory.TempDir Path temp) throws IOException, SimpleWorkdirFactory.ReclaimFailedException {
    when(createWorkdirContext.getScmRepository()).thenReturn(REPOSITORY);
    when(workdirProvider.createNewWorkdir()).thenReturn(temp.toFile());

    SimpleWorkdirFactory.ParentAndClone<?, ?> firstWorkdir = cachingAllWorkdirProvider.getWorkdir(createWorkdirContext);
    cachingAllWorkdirProvider.contextClosed(createWorkdirContext, firstWorkdir.getDirectory());
    SimpleWorkdirFactory.ParentAndClone<?, ?> secondWorkdir = cachingAllWorkdirProvider.getWorkdir(createWorkdirContext);

    verify(initializer).initialize(temp.toFile());
    verify(reclaimer).reclaim(temp.toFile());
    assertThat(secondWorkdir.getDirectory()).isEqualTo(temp.toFile());
  }

  @Test
  void shouldCacheOnlyOneWorkdirForRepository(@TempDirectory.TempDir Path temp) throws IOException, SimpleWorkdirFactory.ReclaimFailedException {
    when(createWorkdirContext.getScmRepository()).thenReturn(REPOSITORY);
    File firstDirectory = temp.resolve("first").toFile();
    firstDirectory.mkdirs();
    File secondDirectory = temp.resolve("second").toFile();
    secondDirectory.mkdirs();
    when(workdirProvider.createNewWorkdir()).thenReturn(
      firstDirectory,
      secondDirectory);

    SimpleWorkdirFactory.ParentAndClone<?, ?> firstWorkdir = cachingAllWorkdirProvider.getWorkdir(createWorkdirContext);
    SimpleWorkdirFactory.ParentAndClone<?, ?> secondWorkdir = cachingAllWorkdirProvider.getWorkdir(createWorkdirContext);
    cachingAllWorkdirProvider.contextClosed(createWorkdirContext, firstWorkdir.getDirectory());
    cachingAllWorkdirProvider.contextClosed(createWorkdirContext, secondWorkdir.getDirectory());

    verify(reclaimer, never()).reclaim(any());
    verify(initializer).initialize(firstDirectory);
    verify(initializer).initialize(secondDirectory);
    assertThat(firstWorkdir.getDirectory()).isNotEqualTo(secondWorkdir.getDirectory());
    assertThat(firstWorkdir.getDirectory()).exists();
    assertThat(secondWorkdir.getDirectory()).doesNotExist();
  }
}
