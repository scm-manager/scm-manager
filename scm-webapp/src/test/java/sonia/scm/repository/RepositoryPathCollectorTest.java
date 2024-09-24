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

package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPathCollectorTest {

  private final NamespaceAndName HEART_OF_GOLD = new NamespaceAndName("hitchhiker", "heart-of-gold");

  @Mock
  private RepositoryServiceFactory factory;

  @Mock
  private RepositoryService service;

  @Mock(answer = Answers.RETURNS_SELF)
  private BrowseCommandBuilder browseCommand;

  @InjectMocks
  private RepositoryPathCollector collector;

  @BeforeEach
  void setUpMocks() {
    when(factory.create(HEART_OF_GOLD)).thenReturn(service);
    when(service.getBrowseCommand()).thenReturn(browseCommand);
  }

  @Test
  void shouldDisableComputeHeavySettings() throws IOException {
    BrowserResult result = new BrowserResult("42", new FileObject());
    when(browseCommand.getBrowserResult()).thenReturn(result);

    collector.collect(HEART_OF_GOLD, "42");
    verify(browseCommand).setDisablePreProcessors(true);
    verify(browseCommand).setDisableSubRepositoryDetection(true);
    verify(browseCommand).setDisableLastCommit(true);
  }

  @Test
  void shouldCollectFiles() throws IOException {
    FileObject root = dir(
      "a",
      file("a/b.txt"),
      dir("a/c",
        file("a/c/d.txt"),
        file("a/c/e.txt")
      )
    );

    BrowserResult result = new BrowserResult("21", root);
    when(browseCommand.getBrowserResult()).thenReturn(result);

    RepositoryPaths paths = collector.collect(HEART_OF_GOLD, "develop");
    assertThat(paths.getRevision()).isEqualTo("21");
    assertThat(paths.getPaths()).containsExactlyInAnyOrder(
      "a/b.txt",
      "a/c/d.txt",
      "a/c/e.txt"
    );
  }

  FileObject dir(String path, FileObject... children) {
    FileObject file = file(path);
    file.setDirectory(true);
    file.setChildren(Arrays.asList(children));
    return file;
  }

  FileObject file(String path) {
    FileObject file = new FileObject();
    file.setPath(path);
    file.setName(path);
    return file;
  }
}
