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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.constraints.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandValidatorTest {

  @Mock
  private CliContext context;

  @Test
  void shouldValidateCommand() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("sonia.scm.cli.i18n", Locale.ENGLISH, new ResourceBundle.Control() {
      @Override
      public Locale getFallbackLocale(String baseName, Locale locale) {
        return Locale.ROOT;
      }
    });
    when(context.getLocale()).thenReturn(Locale.ENGLISH);
    CommandLine commandLine = new CommandLine(Command.class, new TestingCommandFactory());
    commandLine.setResourceBundle(resourceBundle);
    StringWriter stringWriter = new StringWriter();
    commandLine.setErr(new PrintWriter(stringWriter));

    commandLine.execute("--mail=test");

    assertThat(stringWriter.toString()).contains("ERROR: must be a well-formed email address");
  }

  @Test
  void shouldValidateCommandWithGermanLocale() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("sonia.scm.cli.i18n", Locale.GERMAN, new ResourceBundle.Control() {
      @Override
      public Locale getFallbackLocale(String baseName, Locale locale) {
        return Locale.ROOT;
      }
    });
    when(context.getLocale()).thenReturn(Locale.GERMAN);
    CommandLine commandLine = new CommandLine(Command.class, new TestingCommandFactory());
    commandLine.setResourceBundle(resourceBundle);
    StringWriter stringWriter = new StringWriter();
    commandLine.setErr(new PrintWriter(stringWriter));

    commandLine.execute("--mail=test");

    assertThat(stringWriter.toString()).contains("FEHLER: muss eine korrekt formatierte E-Mail-Adresse sein");
  }

  @CommandLine.Command
  public static class Command implements Runnable {

    @CommandLine.Mixin
    private final CommandValidator commandValidator;

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
