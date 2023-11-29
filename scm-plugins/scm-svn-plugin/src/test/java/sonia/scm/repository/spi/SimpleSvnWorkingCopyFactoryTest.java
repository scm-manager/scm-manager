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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.SimpleCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.repository.work.WorkingCopy;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleSvnWorkingCopyFactoryTest extends AbstractSvnCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private MeterRegistry meterRegistry = new SimpleMeterRegistry();

  // keep this so that it will not be garbage collected (Transport keeps this in a week reference)
  private WorkdirProvider workdirProvider;


  @Before
  public void initWorkDirProvider() throws IOException {
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder(), repositoryLocationResolver, false);
  }

  @Test
  public void shouldCheckoutLatestRevision() {
    SimpleSvnWorkingCopyFactory factory = new SimpleSvnWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());

    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      assertThat(new File(workingCopy.getWorkingRepository(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a and b\nline for blame test");
    }
  }

  @Test
  public void cloneFromPoolShouldNotBeReused() {
    SimpleSvnWorkingCopyFactory factory = new SimpleSvnWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());

    File firstDirectory;
    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      firstDirectory = workingCopy.getDirectory();
    }
    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      File secondDirectory = workingCopy.getDirectory();
      assertThat(secondDirectory).isNotEqualTo(firstDirectory);
    }
  }

  @Test
  public void shouldDeleteCloneOnClose() {
    SimpleSvnWorkingCopyFactory factory = new SimpleSvnWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());

    File directory;
    File workingRepository;
    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      directory = workingCopy.getDirectory();
      workingRepository = workingCopy.getWorkingRepository();
    }

    assertThat(directory).doesNotExist();
    assertThat(workingRepository).doesNotExist();
  }

  @Test
  public void shouldDeleteUntrackedFileOnReclaim() throws IOException {
    SimpleSvnWorkingCopyFactory factory = new SimpleSvnWorkingCopyFactory(new SimpleCachingWorkingCopyPool(5, workdirProvider, meterRegistry), new SimpleMeterRegistry());

    WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null);
    File directory = workingCopy.getWorkingRepository();
    File untracked = new File(directory, "untracked");
    untracked.createNewFile();

    workingCopy.close();
    assertThat(untracked).exists();

    workingCopy = factory.createWorkingCopy(createContext(), null);

    assertThat(workingCopy.getWorkingRepository()).isEqualTo(directory);
    assertThat(untracked).doesNotExist();
  }

  @Test
  public void shouldRestoreDeletedFileOnReclaim() {
    SimpleSvnWorkingCopyFactory factory = new SimpleSvnWorkingCopyFactory(new SimpleCachingWorkingCopyPool(5, workdirProvider, meterRegistry), new SimpleMeterRegistry());

    WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null);
    File directory = workingCopy.getWorkingRepository();
    File a_txt = new File(directory, "a.txt");
    a_txt.delete();
    workingCopy.close();
    assertThat(a_txt).doesNotExist();

    workingCopy = factory.createWorkingCopy(createContext(), null);

    assertThat(workingCopy.getWorkingRepository()).isEqualTo(directory);
    assertThat(a_txt).exists();
  }
}
