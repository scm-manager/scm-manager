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

  @BeforeEach
  void initMetaDataProvider() {
    lenient().when(storeMetaDataProvider.getTypesWithParent(Repository.class)).thenReturn(List.of(SimpleType.class, SimpleTypeWithTwoParents.class));
  }

  @Nested
  class ExportStores {
    @Test
    void shouldExportSimpleType(QueryableStoreFactory storeFactory, SimpleTypeStoreFactory simpleTypeStoreFactory, @TempDir java.nio.file.Path tempDir) {
      simpleTypeStoreFactory.getMutable("23").put("1", new SimpleType("hack"));
      simpleTypeStoreFactory.getMutable("42").put("1", new SimpleType("hitchhike"));
      simpleTypeStoreFactory.getMutable("42").put("2", new SimpleType("heart of gold"));

      RepositoryQueryableStoreExporter exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);

      exporter.exportStores("42", tempDir.toFile());

      assertThat(tempDir).isNotEmptyDirectory();
    }

    @Test
    void shouldExportTypeWithTwoParents(QueryableStoreFactory storeFactory, SimpleTypeWithTwoParentsStoreFactory simpleTypeStoreFactory, @TempDir java.nio.file.Path tempDir) {
      simpleTypeStoreFactory.getMutable("23", "1").put("1", new SimpleTypeWithTwoParents("hack"));
      simpleTypeStoreFactory.getMutable("42", "1").put("1", new SimpleTypeWithTwoParents("hitchhike"));
      simpleTypeStoreFactory.getMutable("42", "1").put("2", new SimpleTypeWithTwoParents("heart of gold"));

      RepositoryQueryableStoreExporter exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);

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
    void shouldImportSimpleType(QueryableStoreFactory storeFactory, SimpleTypeStoreFactory simpleTypeStoreFactory) throws IOException {
      simpleTypeStoreFactory.getMutable("23").put("1", new SimpleType("hack"));
      URL url = Resources.getResource("sonia/scm/importexport/SimpleType.xml");

      Files.createFile(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"));
      Files.writeString(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"), Resources.toString(url, StandardCharsets.UTF_8));

      RepositoryQueryableStoreExporter exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);

      exporter.importStores("42", tempDir);

      assertThat(simpleTypeStoreFactory.getMutable("42").getAll()).hasSize(2);
    }

    @Test
    void shouldImportTypeWithTwoParents(QueryableStoreFactory storeFactory, SimpleTypeWithTwoParentsStoreFactory simpleTypeStoreFactory) throws IOException {
      simpleTypeStoreFactory.getMutable("23", "1").put("1", new SimpleTypeWithTwoParents("hack"));
      URL url = Resources.getResource("sonia/scm/importexport/SimpleTypeWithTwoParents.xml");
      Files.writeString(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleTypeWithTwoParents.xml"), Resources.toString(url, StandardCharsets.UTF_8));

      RepositoryQueryableStoreExporter exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);
      exporter.importStores("42", tempDir);

      assertThat(simpleTypeStoreFactory.getMutable("42", "1").getAll()).hasSize(2);
    }

    @Test
    void shouldNotImportWhenFileDoesNotExist(QueryableStoreFactory storeFactory, SimpleTypeStoreFactory simpleTypeStoreFactory) {
      simpleTypeStoreFactory.getMutable("23").put("1", new SimpleType("hack"));

      File nonExistentFile = queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml").toFile();
      assertThat(nonExistentFile).doesNotExist();

      RepositoryQueryableStoreExporter exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);
      exporter.importStores("42", tempDir);

      assertThat(simpleTypeStoreFactory.getMutable("42").getAll()).isEmpty();
    }

    @Test
    void shouldThrowExceptionForMalformedXML(QueryableStoreFactory storeFactory) throws IOException {
      Files.writeString(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"), "<malformed><xml></broken>");

      RepositoryQueryableStoreExporter exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);

      assertThrows(RuntimeException.class, () -> exporter.importStores("42", tempDir));
    }

    @Test
    void shouldNotImportFromEmptyFile(QueryableStoreFactory storeFactory, SimpleTypeStoreFactory simpleTypeStoreFactory) throws IOException {
      simpleTypeStoreFactory.getMutable("42").put("1", new SimpleType("existing data"));

      Files.createFile(queryableStoreDir.toPath().resolve("sonia.scm.importexport.SimpleType.xml"));

      RepositoryQueryableStoreExporter exporter = new RepositoryQueryableStoreExporter(storeMetaDataProvider, storeFactory);
      exporter.importStores("42", tempDir);

      SimpleType simpleType = simpleTypeStoreFactory.getMutable("42").get("1");

      assertThat(simpleType)
        .extracting("someField")
        .isEqualTo("existing data");
    }
  }
}

