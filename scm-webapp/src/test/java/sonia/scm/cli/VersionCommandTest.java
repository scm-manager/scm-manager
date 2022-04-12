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
