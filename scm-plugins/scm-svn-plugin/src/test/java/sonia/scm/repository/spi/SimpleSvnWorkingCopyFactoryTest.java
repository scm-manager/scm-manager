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
