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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CliProcessorTest {

  @Mock
  private CommandRegistry registry;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CliContext context;


  @Test
  void shouldExecutePingCommand() {
    when(registry.getCommands()).thenReturn(Collections.singleton(PingCommand.class));
    Injector injector = Guice.createInjector();
    CliProcessor cliProcessor = new CliProcessor(registry, injector);

    cliProcessor.execute(context, "ping");

    verify(context.getStdout()).println("pong");
  }


  @Test
  void shouldExecutePingCommandWithAlias() {
    when(registry.getCommands()).thenReturn(Collections.singleton(PingCommand.class));
    Injector injector = Guice.createInjector();
    CliProcessor cliProcessor = new CliProcessor(registry, injector);

    cliProcessor.execute(context, "scmping");

    verify(context.getStdout()).println("pong");
  }

  @Test
  void shouldExecutePingCommandWithExitCode0() {
    when(registry.getCommands()).thenReturn(Collections.singleton(PingCommand.class));
    Injector injector = Guice.createInjector();
    CliProcessor cliProcessor = new CliProcessor(registry, injector);

    int exitCode = cliProcessor.execute(context, "scmping");

    assertThat(exitCode).isZero();
  }
}
