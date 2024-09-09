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

package sonia.scm.repository.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgTestUtil;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HgVersionCommandTest {

  @Test
  void shouldReturnVersion(@TempDir Path temp) {
    HgRepositoryHandler handler = HgTestUtil.createHandler(temp.toFile());
    HgTestUtil.checkForSkip(handler);

    HgVersionCommand command = new HgVersionCommand(handler.getConfig());
    assertThat(command.get())
      .contains("python/")
      .contains("mercurial/")
      .isNotEqualTo(HgVersionCommand.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownForIOException() {
    HgVersionCommand command = new HgVersionCommand(new HgGlobalConfig(), "/i/dont/know", cmd -> {
      throw new IOException("failed");
    });

    assertThat(command.get()).isEqualTo(HgVersionCommand.UNKNOWN);
  }

}
