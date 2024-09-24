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

package sonia.scm.update.repository;

import com.google.common.io.Resources;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.xml.SingleRepositoryUpdateProcessor;
import sonia.scm.security.SystemRepositoryPermissionProvider;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrateVerbsToPermissionRolesTest {

  private static final String EXISTING_REPOSITORY_ID = "id";

  @Mock
  private SingleRepositoryUpdateProcessor singleRepositoryUpdateProcessor;
  @Mock
  private SystemRepositoryPermissionProvider systemRepositoryPermissionProvider;

  @InjectMocks
  private MigrateVerbsToPermissionRoles migration;

  @TempDir
  private Path tempDir;

  @BeforeEach
  void init() throws IOException {
    URL metadataUrl = Resources.getResource("sonia/scm/update/repository/metadataWithoutRoles.xml");
    Files.copy(metadataUrl.openStream(), tempDir.resolve("metadata.xml"));
    doAnswer(invocation -> {
      ((BiConsumer<String, Path>) invocation.getArgument(0)).accept(EXISTING_REPOSITORY_ID, tempDir);
      return null;
    }).when(singleRepositoryUpdateProcessor).doUpdate(any());
    when(systemRepositoryPermissionProvider.availableRoles()).thenReturn(Collections.singletonList(new RepositoryRole("ROLE", asList("read", "write"), "")));
  }

  @Test
  void shouldUpdateToRolesIfPossible() throws IOException {
    migration.doUpdate();

    List<String> newMetadata = Files.readAllLines(tempDir.resolve("metadata.xml"));
    Assertions.assertThat(newMetadata.stream().map(String::trim)).
      containsSubsequence(
        "<groupPermission>false</groupPermission>",
        "<name>user</name>",
        "<role>ROLE</role>"
      )
      .containsSubsequence(
        "<groupPermission>true</groupPermission>",
        "<name>group</name>",
        "<verb>special</verb>"
      )
      .doesNotContain(
        "<verb>read</verb>",
        "<verb>write</verb>"
      );
  }

}
