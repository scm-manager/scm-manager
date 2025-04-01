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

package sonia.scm.store.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.TempDirRepositoryLocationResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FileNamespaceUpdateIteratorTest {

  private TempDirRepositoryLocationResolver locationResolver;

  @BeforeEach
  void initLocationResolver(@TempDir Path tempDir) throws IOException {
    locationResolver = new TempDirRepositoryLocationResolver(tempDir.toFile());
    Files.write(tempDir.resolve("metadata.xml"), asList(
      "<repositories>",
      "  <namespace>hitchhike</namespace>",
      "</repositories>"
    ));
  }

  @Test
  void shouldFindNamespaces() {
    Collection<String> foundNamespaces = new ArrayList<>();
    new FileNamespaceUpdateIterator(locationResolver)
      .forEachNamespace(foundNamespaces::add);

    assertThat(foundNamespaces).containsExactly("hitchhike");
  }
}
