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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tmatesoft.svn.core.SVNException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.util.NoneCachingWorkdirProvider;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleSvnWorkDirFactoryTest extends AbstractSvnCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  // keep this so that it will not be garbage collected (Transport keeps this in a week reference)
  private WorkdirProvider workdirProvider;

  @Before
  public void initWorkDirProvider() throws IOException {
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder());
  }

  @Test
  public void shouldCheckoutLatestRevision() throws SVNException, IOException {
    SimpleSvnWorkDirFactory factory = new SimpleSvnWorkDirFactory(new NoneCachingWorkdirProvider(workdirProvider));

    try (WorkingCopy<File, File> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      assertThat(new File(workingCopy.getWorkingRepository(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a and b\nline for blame test");
    }
  }

  @Test
  public void cloneFromPoolshouldNotBeReused() {
    SimpleSvnWorkDirFactory factory = new SimpleSvnWorkDirFactory(new NoneCachingWorkdirProvider(workdirProvider));

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
    SimpleSvnWorkDirFactory factory = new SimpleSvnWorkDirFactory(new NoneCachingWorkdirProvider(workdirProvider));

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
  public void shouldReturnRepository() {
    SimpleSvnWorkDirFactory factory = new SimpleSvnWorkDirFactory(new NoneCachingWorkdirProvider(workdirProvider));
    Repository scmRepository = factory.getScmRepository(createContext());
    assertThat(scmRepository).isSameAs(repository);
  }
}
