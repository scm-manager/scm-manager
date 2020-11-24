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

package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class PendingPluginInstallationTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AvailablePlugin plugin;

  @Test
  void shouldDeleteFileOnCancel(@TempDir Path directory) throws IOException {
    Path file = directory.resolve("file");
    Files.write(file, "42".getBytes());

    when(plugin.getDescriptor().getInformation().getName()).thenReturn("scm-awesome-plugin");

    PendingPluginInstallation installation = new PendingPluginInstallation(plugin, file);
    installation.cancel();

    assertThat(file).doesNotExist();
  }

  @Test
  void shouldThrowExceptionIfCancelFailed(@TempDir Path directory) throws IOException {
    Path file = directory.resolve("file");
    Files.createDirectory(file);

    Path makeFileNotDeletable = file.resolve("not_deletable");
    Files.write(makeFileNotDeletable, "42".getBytes());

    when(plugin.getDescriptor().getInformation().getName()).thenReturn("scm-awesome-plugin");

    PendingPluginInstallation installation = new PendingPluginInstallation(plugin, file);
    assertThrows(PluginFailedToCancelInstallationException.class, installation::cancel);
  }

}
