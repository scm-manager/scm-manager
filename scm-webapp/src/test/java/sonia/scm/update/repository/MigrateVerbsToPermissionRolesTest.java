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
