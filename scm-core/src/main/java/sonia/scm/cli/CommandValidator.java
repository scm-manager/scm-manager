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

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import picocli.CommandLine;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * This the command validator which should be used to validate CLI commands with Bean validation.
 * @see <a href="https://beanvalidation.org/2.0/spec/">Bean validation spec</a>
 * @since 2.33.0
 */
// We need to hide this because it is not a real command but a mixin.
// The command annotation is required for picocli to resolve this properly.
public final class CommandValidator {

  private final CliContext context;
  private final Validator validator;

  @CommandLine.Spec(CommandLine.Spec.Target.MIXEE)
  private CommandLine.Model.CommandSpec spec;

  // We need an option to trick PicoCli into accepting our mixin
  @CommandLine.Option(names = "--hidden-flag", hidden = true)
  private boolean hiddenFlag ;

  @Inject
  public CommandValidator(CliContext context, ConstraintValidatorFactory constraintValidatorFactory) {
    this.context = context;
    this.validator = createValidator(constraintValidatorFactory);
  }

  private Validator createValidator(ConstraintValidatorFactory constraintValidatorFactory) {
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    return validatorFactory.usingContext()
      .constraintValidatorFactory(constraintValidatorFactory)
      .messageInterpolator(new LocaleSpecificMessageInterpolator(validatorFactory.getMessageInterpolator(), context.getLocale()))
      .getValidator();
  }

  /**
   * Execute validation and exit the command on validation failure
   */
  public void validate() {
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
    ResourceBundle bundle = spec.resourceBundle();
    if (bundle != null && bundle.containsKey("errorLabel")) {
        return bundle.getString("errorLabel") + ": ";
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
