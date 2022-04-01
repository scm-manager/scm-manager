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
import sonia.scm.validation.LocaleSpecificMessageInterpolator;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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


}
