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
