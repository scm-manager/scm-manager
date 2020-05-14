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

package sonia.scm.repository.work;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class CachingAllWorkingCopyPoolTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "X");

  @Mock
  WorkdirProvider workdirProvider;
  @InjectMocks
  CachingAllWorkingCopyPool cachingAllWorkingCopyPool;

  @Mock
  WorkingCopyContext<Object, Path> workingCopyContext;

  @BeforeEach
  void initContext() throws SimpleWorkingCopyFactory.ReclaimFailedException {

    lenient().when(workingCopyContext.initialize(any()))
      .thenAnswer(invocationOnMock -> new WorkingCopyPool.ParentAndClone<>(null, null, invocationOnMock.getArgument(0, File.class)));
    lenient().when(workingCopyContext.reclaim(any()))
      .thenAnswer(invocationOnMock -> new WorkingCopyPool.ParentAndClone<>(null, null, invocationOnMock.getArgument(0, File.class)));
  }

  @Test
  void shouldCreateNewWorkdirForTheFirstRequest(@TempDir Path temp) {
    when(workingCopyContext.getScmRepository()).thenReturn(REPOSITORY);
    when(workdirProvider.createNewWorkdir()).thenReturn(temp.toFile());

    WorkingCopyPool.ParentAndClone<?, ?> workdir = cachingAllWorkingCopyPool.getWorkingCopy(workingCopyContext);

    verify(workingCopyContext).initialize(temp.toFile());
  }

  @Test
  void shouldCreateWorkdirOnlyOnceForTheSameRepository(@TempDir Path temp) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    when(workingCopyContext.getScmRepository()).thenReturn(REPOSITORY);
    when(workdirProvider.createNewWorkdir()).thenReturn(temp.toFile());

    WorkingCopyPool.ParentAndClone<?, ?> firstWorkdir = cachingAllWorkingCopyPool.getWorkingCopy(workingCopyContext);
    cachingAllWorkingCopyPool.contextClosed(workingCopyContext, firstWorkdir.getDirectory());
    WorkingCopyPool.ParentAndClone<?, ?> secondWorkdir = cachingAllWorkingCopyPool.getWorkingCopy(workingCopyContext);

    verify(workingCopyContext).initialize(temp.toFile());
    verify(workingCopyContext).reclaim(temp.toFile());
    assertThat(secondWorkdir.getDirectory()).isEqualTo(temp.toFile());
  }

  @Test
  void shouldCacheOnlyOneWorkdirForRepository(@TempDir Path temp) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    when(workingCopyContext.getScmRepository()).thenReturn(REPOSITORY);
    File firstDirectory = temp.resolve("first").toFile();
    firstDirectory.mkdirs();
    File secondDirectory = temp.resolve("second").toFile();
    secondDirectory.mkdirs();
    when(workdirProvider.createNewWorkdir()).thenReturn(
      firstDirectory,
      secondDirectory);

    WorkingCopyPool.ParentAndClone<?, ?> firstWorkdir = cachingAllWorkingCopyPool.getWorkingCopy(workingCopyContext);
    WorkingCopyPool.ParentAndClone<?, ?> secondWorkdir = cachingAllWorkingCopyPool.getWorkingCopy(workingCopyContext);
    cachingAllWorkingCopyPool.contextClosed(workingCopyContext, firstWorkdir.getDirectory());
    cachingAllWorkingCopyPool.contextClosed(workingCopyContext, secondWorkdir.getDirectory());

    verify(workingCopyContext, never()).reclaim(any());
    verify(workingCopyContext).initialize(firstDirectory);
    verify(workingCopyContext).initialize(secondDirectory);
    assertThat(firstWorkdir.getDirectory()).isNotEqualTo(secondWorkdir.getDirectory());
    assertThat(firstWorkdir.getDirectory()).exists();
    assertThat(secondWorkdir.getDirectory()).doesNotExist();
  }
}
