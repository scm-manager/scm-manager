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

package sonia.scm.update;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.store.DataStore;
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRepositoryPermissionUpdaterTest {

  private final Repository repository = new Repository(
    "1",
    "git",
    "Kanto",
    "Saffron City",
    "Sabrina",
    "City",
    new RepositoryPermission("Trainer Red", Set.of("read", "pull", "readCIStatus"), false)
  );
  private final InMemoryByteDataStoreFactory dataStoreFactory = new InMemoryByteDataStoreFactory();
  @Mock
  private RepositoryLocationResolver locationResolver;
  @Mock
  private RepositoryLocationResolver.RepositoryLocationResolverInstance<Path> resolverInstance;
  private DefaultRepositoryPermissionUpdater updater;

  @TempDir
  private Path tempDir;

  @BeforeEach
  void setup() {
    updater = new DefaultRepositoryPermissionUpdater(locationResolver, dataStoreFactory);
  }

  @Nested
  class RemovePermission {

    @Test
    void shouldRemovePermissionFromUserAndGroupsForNamespace() {
      RepositoryPermission userPermission = new RepositoryPermission(
        "Trainer Red", Set.of("read", "pull", "readCIStatus"), false
      );
      RepositoryPermission groupPermission = new RepositoryPermission(
          "Elite Four", Set.of("read", "pull", "readCIStatus"), true
      );

      DataStore<Namespace> namespaceStore = dataStoreFactory.withType(Namespace.class).withName("namespaces").build();
      Namespace namespace = new Namespace("Kanto");
      namespace.setPermissions(Set.of(userPermission, groupPermission));
      namespaceStore.put(namespace.getId(), namespace);

      updater.removePermission(namespace, "readCIStatus");

      assertThat(namespaceStore.get(namespace.getId()).getPermissions()).containsOnly(
        new RepositoryPermission("Trainer Red", Set.of("read", "pull"), false),
        new RepositoryPermission("Elite Four", Set.of("read", "pull"), true)
      );
    }

    @Test
    void shouldRemovePermissionFromUserAndGroupsForRepository() throws IOException {
      URL metadataUrl = Resources.getResource(
        "sonia/scm/update/repository/metadataWithPermissionsToRemove.xml"
      );
      Files.copy(metadataUrl.openStream(), tempDir.resolve("metadata.xml"));
      when(locationResolver.forClass(Path.class)).thenReturn(resolverInstance);
      when(resolverInstance.getLocation(repository.getId())).thenReturn(tempDir);

      updater.removePermission(repository, "readCIStatus");

      List<String> newMetadata = Files.readAllLines(tempDir.resolve("metadata.xml"));
      assertThat(newMetadata.stream().map(String::trim))
        .contains(
        "<permission>",
        "<groupPermission>false</groupPermission>",
        "<name>Trainer Red</name>",
        "<verb>read</verb>",
        "<verb>pull</verb>",
        "</permission>"
      ).doesNotContain("<verb>readCIStatus</verb>");
    }
  }
}
