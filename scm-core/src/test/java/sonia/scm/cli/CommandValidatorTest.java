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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.constraints.Email;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandValidatorTest {

  @Mock
  private CliContext context;

  @Test
  void shouldValidateCommand() {
    when(context.getLocale()).thenReturn(Locale.ENGLISH);
    CommandLine commandLine = new CommandLine(Command.class, new TestingCommandFactory());
    StringWriter stringWriter = new StringWriter();
    commandLine.setErr(new PrintWriter(stringWriter));

    commandLine.execute("--mail=test");

    assertThat(stringWriter.toString()).contains("ERROR: must be a well-formed email address");
  }

  @Test
  void shouldValidateCommandWithGermanLocale() {
    when(context.getLocale()).thenReturn(Locale.GERMAN);
    CommandLine commandLine = new CommandLine(Command.class, new TestingCommandFactory());
    StringWriter stringWriter = new StringWriter();
    commandLine.setErr(new PrintWriter(stringWriter));

    commandLine.execute("--mail=test");

    assertThat(stringWriter.toString()).contains("FEHLER: muss eine korrekt formatierte E-Mail-Adresse sein");
  }

  @CommandLine.Command
  public static class Command implements Runnable {

    @CommandLine.Mixin
    private CommandValidator commandValidator;

    @Email
    @CommandLine.Option(names = "--mail")
    private String mail;

    public Command(CliContext context) {
      commandValidator = new CommandValidator(context, new TestingConstraintValidatorFactory());
    }

    @Override
    public void run() {
      commandValidator.validate();
    }
  }

  class TestingCommandFactory implements CommandLine.IFactory {

    @Override
    public <K> K create(Class<K> cls) throws Exception {
      try {
        return cls.getConstructor(CliContext.class).newInstance(context);
      } catch (Exception e) {
        return CommandLine.defaultFactory().create(cls);
      }
    }
  }

  static class TestingConstraintValidatorFactory implements ConstraintValidatorFactory {

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
      try {
        return key.getConstructor().newInstance();
      } catch (Exception e) {
        throw new IllegalStateException("Failed to create constraint validator");
      }
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {

    }
  }
}
