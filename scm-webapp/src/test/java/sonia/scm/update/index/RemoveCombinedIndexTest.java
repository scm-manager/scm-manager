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
