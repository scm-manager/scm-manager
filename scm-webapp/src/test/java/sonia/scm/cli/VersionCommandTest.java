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

package sonia.scm.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionCommandTest {

  @Mock
  private CliContext context;
  @Mock
  private TemplateRenderer templateRenderer;

  @Mock
  private SCMContextProvider scmContextProvider;

  @InjectMocks
  private VersionCommand command;

  @Test
  void shouldPrintVersions() {
    when(context.getClient()).thenReturn(new Client("scm-cli", "1.0.0"));
    when(scmContextProvider.getVersion()).thenReturn("2.33.0");

    command.run();

    verify(templateRenderer).renderToStdout(anyString(), argThat(map -> {
      assertThat(map.get("client")).isInstanceOf(Client.class);
      assertThat(((Client)map.get("client")).getName()).isEqualTo("scm-cli");
      assertThat(((Client)map.get("client")).getVersion()).isEqualTo("1.0.0");
      assertThat(map.get("server")).isInstanceOf(VersionCommand.Server.class);
      assertThat(((VersionCommand.Server)map.get("server")).getVersion()).isEqualTo("2.33.0");
      return true;
    }));
  }
}
