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

import picocli.CommandLine;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Locale;
import java.util.Set;

@CommandLine.Command(name = "validator")
public final class CommandValidator {

  private final CliContext context;

  @CommandLine.Spec(CommandLine.Spec.Target.MIXEE)
  private CommandLine.Model.CommandSpec spec;

  @Inject
  public CommandValidator(CliContext context) {
    this.context = context;
  }

  public void validate() {
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    Validator validator = validatorFactory.usingContext()
      .messageInterpolator(new LocaleSpecificMessageInterpolator(validatorFactory.getMessageInterpolator(), context.getLocale()))
      .getValidator();
    Set<ConstraintViolation<Object>> violations = validator.validate(spec.userObject());

    if (!violations.isEmpty()) {
      StringBuilder errorMsg = new StringBuilder();
      for (ConstraintViolation<Object> violation : violations) {
        errorMsg.append(evaluateErrorTemplate()).append(violation.getMessage()).append("\n");
      }
      throw new CommandLine.ParameterException(spec.commandLine(), errorMsg.toString());
    }
  }

  private String evaluateErrorTemplate() {
    //TODO Find better solution
    if (context.getLocale().toString().toLowerCase().startsWith("de")) {
      return "FEHLER: ";
    }
    return "ERROR: ";
  }

  private static class LocaleSpecificMessageInterpolator implements MessageInterpolator {

    private final MessageInterpolator defaultMessageInterpolator;
    private final Locale defaultLocale;

    private LocaleSpecificMessageInterpolator(MessageInterpolator defaultMessageInterpolator, Locale defaultLocale) {
      this.defaultMessageInterpolator = defaultMessageInterpolator;
      this.defaultLocale = defaultLocale;
    }

    @Override
    public String interpolate(String messageTemplate, Context context) {
      return interpolate(messageTemplate, context, defaultLocale);
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
      return defaultMessageInterpolator.interpolate(messageTemplate, context, locale);
    }
  }

}
