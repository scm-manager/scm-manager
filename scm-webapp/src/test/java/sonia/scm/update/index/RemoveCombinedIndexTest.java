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

package sonia.scm.update.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveCombinedIndexTest {

  private Path home;

  @Mock
  private SCMContextProvider contextProvider;

  @InjectMocks
  private RemoveCombinedIndex updateStep;

  @BeforeEach
  void setUp(@TempDir Path home) {
    this.home = home;
    when(contextProvider.resolve(any())).then(
      ic -> home.resolve(ic.getArgument(0, Path.class))
    );
  }

  @Test
  void shouldRemoveIndexDirectory() throws IOException {
    Path indexDirectory = home.resolve("index");
    Path specificIndexDirectory = indexDirectory.resolve("repository").resolve("default");
    Files.createDirectories(specificIndexDirectory);
    Path helloTxt = specificIndexDirectory.resolve("hello.txt");
    Files.write(helloTxt, "hello".getBytes(StandardCharsets.UTF_8));

    updateStep.doUpdate();

    assertThat(helloTxt).doesNotExist();
    assertThat(indexDirectory).doesNotExist();
  }

  @Test
  void shouldRemoveIndexLogDirectory() throws IOException {
    Path logDirectory = home.resolve("var").resolve("data").resolve("index-log");
    Files.createDirectories(logDirectory);
    Path helloXml = logDirectory.resolve("hello.xml");
    Files.write(helloXml, "<hello>world</hello>".getBytes(StandardCharsets.UTF_8));

    updateStep.doUpdate();

    assertThat(helloXml).doesNotExist();
    assertThat(logDirectory).doesNotExist();
  }

}
