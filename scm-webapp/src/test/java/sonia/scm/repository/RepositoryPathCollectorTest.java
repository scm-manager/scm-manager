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
