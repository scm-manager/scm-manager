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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryLocationResolver.RepositoryLocationResolverInstance;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemainingQueryableStoreImporterTest {

  @TempDir
  private File tempDir;

  @Mock
  private PathBasedRepositoryLocationResolver repositoryLocationResolver;
  @Mock
  private RepositoryLocationResolverInstance<Path> repositoryLocationResolverInstance;
  @Mock
  private RepositoryQueryableStoreExporter queryableStoreExporter;

  private RemainingQueryableStoreImporter listener;

  @BeforeEach
  void setUp() throws IOException {
    when(repositoryLocationResolver.create(Path.class)).thenReturn(repositoryLocationResolverInstance);

    listener = new RemainingQueryableStoreImporter(repositoryLocationResolver, queryableStoreExporter);

    File queryableStoreDir = new File(tempDir, "queryable-store-data");
    queryableStoreDir.mkdirs();

    createXmlFile(new File(queryableStoreDir, "sonia.scm.importexport.SimpleType.xml"));
    createXmlFile(new File(queryableStoreDir, "sonia.scm.importexport.SimpleTypeWithTwoParents.xml"));
  }

  @Test
  void shouldImportXmlFilesIfExist() {
    Path repoPath = tempDir.toPath();

    doAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      BiConsumer<String, Path> consumer = invocation.getArgument(0);
      consumer.accept("test-repo", repoPath);
      return null;
    }).when(repositoryLocationResolverInstance).forAllLocations(any());

    listener.onInitializationCompleted();

    verify(queryableStoreExporter).importStores("test-repo", tempDir);
  }

  @Test
  void shouldNotImportIfNoXmlFiles() {
    File emptyRepoDir = new File(tempDir, "empty-repo");
    emptyRepoDir.mkdirs();
    Path emptyRepoPath = emptyRepoDir.toPath();

    doAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      BiConsumer<String, Path> consumer = invocation.getArgument(0);
      consumer.accept("empty-repo", emptyRepoPath);
      return null;
    }).when(repositoryLocationResolverInstance).forAllLocations(any());

    listener.onInitializationCompleted();

    verify(queryableStoreExporter, never()).importStores(anyString(), any(File.class));
  }

  private void createXmlFile(File file) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write("<root><test>data</test></root>");
    }
  }
}
