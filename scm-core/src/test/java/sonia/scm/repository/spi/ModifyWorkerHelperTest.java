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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.repository.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModifyWorkerHelperTest {

  private static final Repository REPOSITORY = new Repository("42", "git", "hitchhiker", "hog");

  private boolean pathProtected = false;

  @Test
  void shouldKeepExecutableFlag(@TempDir Path temp) throws IOException {

    File target = createFile(temp, "executable.sh");
    File newFile = createFile(temp, "other");

    target.setExecutable(true);

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    helper.modify("executable.sh", newFile);

    assertThat(target.canExecute()).isTrue();
  }

  @Test
  void shouldNotWriteInProtectedPath(@TempDir Path temp) throws IOException {

    pathProtected = true;
    File target = createFile(temp, "some.txt");

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      ScmConstraintViolationException.class,
      () -> helper.create("some.txt", target, true)
    );
  }

  @Test
  void shouldNotWritePathWithPathTraversal(@TempDir Path temp) throws IOException {

    File target = createFile(temp, "some.txt");

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      ScmConstraintViolationException.class,
      () -> helper.create("../some.txt", target, true)
    );
  }

  @Test
  void shouldNotMoveNotExistingFile(@TempDir Path temp) throws IOException {
    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      NotFoundException.class,
      () -> helper.move("no-such-file", "irrelevant", false));
  }

  @Test
  void shouldNotOverwriteExistingFileInMoveWithoutOverwrite(@TempDir Path temp) throws IOException {
    createFile(temp, "existing-source.txt");
    createFile(temp, "existing-target.txt");

    ModifyWorkerHelper helper = new MinimalModifyWorkerHelper(temp);

    assertThrows(
      AlreadyExistsException.class,
      () -> helper.move("existing-source.txt", "existing-target.txt", false));
  }

  private File createFile(Path temp, String fileName) throws IOException {
    File file = new File(temp.toFile(), fileName);
    FileWriter source = new FileWriter(file);
    source.write("something");
    source.close();
    return file;
  }

  private class MinimalModifyWorkerHelper implements ModifyWorkerHelper {

    private final Path temp;

    public MinimalModifyWorkerHelper(Path temp) {
      this.temp = temp;
    }

    @Override
    public void doScmDelete(String toBeDeleted) {

    }

    @Override
    public void addFileToScm(String name, Path file) {

    }

    @Override
    public File getWorkDir() {
      return temp.toFile();
    }

    @Override
    public Repository getRepository() {
      return REPOSITORY;
    }

    @Override
    public String getBranch() {
      return null;
    }

    @Override
    public boolean isProtectedPath(Path path) {
      return pathProtected;
    }
  }
}
