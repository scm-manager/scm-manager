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

package sonia.scm;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SCMContextProviderTest {

  @Test
  void shouldCreateCorrectDocumentationVersion() {
    SCMContextProvider scmContextProvider = new SCMContextProvider() {
      @Override
      public File getBaseDirectory() {
        return null;
      }

      @Override
      public Path resolve(Path path) {
        return null;
      }

      @Override
      public Stage getStage() {
        return null;
      }

      @Override
      public Throwable getStartupError() {
        return null;
      }

      @Override
      public String getVersion() {
        return "1.17.2";
      }
    };

    assertThat(scmContextProvider.getDocumentationVersion()).isEqualTo("1.17.x");
  }
}
