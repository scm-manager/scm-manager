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

package sonia.scm.importexport;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.store.QueryableMutableStore;
import sonia.scm.store.QueryableStoreExtension;
import sonia.scm.store.QueryableStoreFactory;
import sonia.scm.store.StoreMetaDataProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@ExtendWith({QueryableStoreExtension.class, MockitoExtension.class})
@QueryableStoreExtension.QueryableTypes({SimpleType.class, SimpleTypeWithTwoParents.class})
class RepositoryQueryableStoreExporterTest {

  @Mock
  private StoreMetaDataProvider storeMetaDataProvider;

  private RepositoryQueryableStoreExporter exporter;

  @BeforeEach
  void initExporter(QueryableStoreFactory storeFactory) {
    lenient().when(storeMetaDataProvider.getTypesWithParent(Repository.class)).thenReturn(List.of(SimpleType.class, SimpleTypeWithTwoParents.class));
    exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);
  }

  @Nested
  class ExportStores {
    @Test
    void shouldExportSimpleType(SimpleTypeStoreFactory simpleTypeStoreFactory, @TempDir java.nio.file.Path tempDir) {
      try (QueryableMutableStore<SimpleType> store = simpleTypeStoreFactory.getMutable("23")) {
        store.put("1", new SimpleType("hack"));
      }
      try (QueryableMutableStore<SimpleType> store = simpleTypeStoreFactory.getMutable("42")) {
        store.put("1", new SimpleType("hitchhike"));
        store.put("2", new SimpleType("heart of gold"));
      }

      exporter.exportStores("42", tempDir.toFile());

      assertThat(tempDir).isNotEmptyDirectory();
    }

    @Test
    void shouldExportTypeWithTwoParents(SimpleTypeWithTwoParentsStoreFactory simpleTypeStoreFactory, @TempDir java.nio.file.Path tempDir) {
      try (QueryableMutableStore<SimpleTypeWithTwoParents> store = simpleTypeStoreFactory.getMutable("23", "1")) {
        store.put("1", new SimpleTypeWithTwoParents("hack"));
      }
      try (QueryableMutableStore<SimpleTypeWithTwoParents> store = simpleTypeStoreFactory.getMutable("42", "1")) {
        store.put("1", new SimpleTypeWithTwoParents("hitchhike"));
        store.put("2", new SimpleTypeWithTwoParents("heart of gold"));
      }

      exporter.exportStores("42", tempDir.toFile());

      assertThat(tempDir).isNotEmptyDirectory();
    }
  }

  @Nested
  class ImportStores {

    private File queryableStoreDir;

    @TempDir
    private File tempDir;

    @BeforeEach
    void prepareImportDirectory() throws IOException {
      queryableStoreDir = new File(tempDir, "queryable-store-data");
      Files.createDirectories(queryableStoreDir.toPath());
    }

    @Test
    void shouldImportSimpleType(SimpleTypeStoreFactory simpleTypeStoreFactory) throws IOException {
      try (QueryableMutableStore<SimpleType> store = simpleTypeStoreFactory.getMutable("23")) {
        store.put("1", new SimpleType("hack"));
      }
      URL url = Resources.getResource("sonia/scm/importexport/SimpleType.xml");

      Files.createFile(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"));
      Files.writeString(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"), Resources.toString(url, StandardCharsets.UTF_8));

      exporter.importStores("42", tempDir);

      try (QueryableMutableStore<SimpleType> store = simpleTypeStoreFactory.getMutable("42")) {
        assertThat(store.getAll()).hasSize(2);
      }
    }

    @Test
    void shouldImportTypeWithTwoParents(SimpleTypeWithTwoParentsStoreFactory simpleTypeStoreFactory) throws IOException {
      try (QueryableMutableStore<SimpleTypeWithTwoParents> store = simpleTypeStoreFactory.getMutable("23", "1")) {
        store.put("1", new SimpleTypeWithTwoParents("hack"));
      }
      URL url = Resources.getResource("sonia/scm/importexport/SimpleTypeWithTwoParents.xml");
      Files.writeString(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleTypeWithTwoParents.xml"), Resources.toString(url, StandardCharsets.UTF_8));

      exporter.importStores("42", tempDir);

      try (QueryableMutableStore<SimpleTypeWithTwoParents> store = simpleTypeStoreFactory.getMutable("42", "1")) {
        assertThat(store.getAll()).hasSize(2);
      }
    }

    @Test
    void shouldNotImportWhenFileDoesNotExist(SimpleTypeStoreFactory simpleTypeStoreFactory) {
      try (QueryableMutableStore<SimpleType> store = simpleTypeStoreFactory.getMutable("23")) {
        store.put("1", new SimpleType("hack"));
      }

      File nonExistentFile = queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml").toFile();
      assertThat(nonExistentFile).doesNotExist();

      exporter.importStores("42", tempDir);

      try (QueryableMutableStore<SimpleType> store = simpleTypeStoreFactory.getMutable("42")) {
        assertThat(store.getAll()).isEmpty();
      }
    }

    @Test
    void shouldThrowExceptionForMalformedXML() throws IOException {
      Files.writeString(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"), "<malformed><xml></broken>");

      assertThrows(RuntimeException.class, () -> exporter.importStores("42", tempDir));
    }

    @Test
    void shouldNotImportFromEmptyFile(SimpleTypeStoreFactory simpleTypeStoreFactory) throws IOException {
      try (QueryableMutableStore<SimpleType> store = simpleTypeStoreFactory.getMutable("42")) {
        store.put("1", new SimpleType("existing data"));

        Files.createFile(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"));

        exporter.importStores("42", tempDir);

        SimpleType simpleType = store.get("1");

        assertThat(simpleType)
          .extracting("someField")
          .isEqualTo("existing data");
      }
    }
  }
}

