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

package sonia.scm.update.store;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static com.google.common.io.Resources.copy;
import static com.google.common.io.Resources.getResource;
import static java.nio.file.Files.newOutputStream;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("UnstableApiUsage")
class DifferentiateBetweenConfigAndConfigEntryUpdateStepTest {

  @Test
  void shouldNotModifyConfigFile(@TempDir Path temp) throws IOException, URISyntaxException {
    Path configFile = temp.resolve("some.store.xml");

    try (OutputStream os = newOutputStream(configFile)) {
      copy(getResource("sonia/scm/update/store/config_file.xml.content"), os);

      new DifferentiateBetweenConfigAndConfigEntryUpdateStep() {}.updateAllInDirectory(temp);

      assertContent(configFile, "sonia/scm/update/store/config_file.xml.content");
    }
  }

  @Test
  void shouldNotModifySingleLineFile(@TempDir Path temp) throws IOException {
    String singleLineContent = "<data><empty /></data>";
    Path configFile = temp.resolve("some.store.xml");
    Files.write(configFile, Collections.singletonList(singleLineContent));

    new DifferentiateBetweenConfigAndConfigEntryUpdateStep() {}.updateAllInDirectory(temp);

    assertThat(configFile).hasContent(singleLineContent);
  }

  @Test
  @SuppressWarnings("java:S2699") // we just want to make sure no exception it thrown
  void shouldNotConsiderDirectories(@TempDir Path temp) throws IOException {
    Path configFile = temp.resolve("some.store.xml");
    Files.createDirectories(configFile);

    new DifferentiateBetweenConfigAndConfigEntryUpdateStep() {}.updateAllInDirectory(temp);

    // no exception expected
  }

  @Test
  void shouldIgnoreFilesWithoutXmlSuffix(@TempDir Path temp) throws IOException, URISyntaxException {
    Path otherFile = temp.resolve("some.other.file");

    try (OutputStream os = newOutputStream(otherFile)) {
      copy(getResource("sonia/scm/update/store/config_entry_file_without_mark.xml.content"), os);

      new DifferentiateBetweenConfigAndConfigEntryUpdateStep() {}.updateAllInDirectory(temp);

      assertContent(otherFile, "sonia/scm/update/store/config_entry_file_without_mark.xml.content");
    }
  }

  @Test
  void shouldHandleConfigEntryFile(@TempDir Path temp) throws IOException, URISyntaxException {
    Path configFile = temp.resolve("some.store.xml");

    try (OutputStream os = newOutputStream(configFile)) {
      copy(getResource("sonia/scm/update/store/config_entry_file_without_mark.xml.content"), os);

      new DifferentiateBetweenConfigAndConfigEntryUpdateStep() {}.updateAllInDirectory(temp);

      assertThat(Files.readAllLines(configFile))
        .areAtLeastOne(new Condition<>(line -> line.contains("<configuration type=\"config-entry\">"), "line containing start tag with attribute"))
        .endsWith(Files.readAllLines(Paths.get(getResource("sonia/scm/update/store/config_entry_file.xml.content").toURI())).toArray(new String[9]));
    }
  }

  private void assertContent(Path configFile, String expectedContentResource) throws URISyntaxException {
    assertThat(configFile)
      .hasSameTextualContentAs(Paths.get(getResource(expectedContentResource).toURI()));
  }
}
