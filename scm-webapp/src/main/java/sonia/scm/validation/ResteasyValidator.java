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

package sonia.scm.validation;

import org.jboss.resteasy.plugins.validation.GeneralValidatorImpl;
import org.jboss.resteasy.spi.HttpRequest;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableType;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ResteasyValidator extends GeneralValidatorImpl {

  private final ValidatorFactory validatorFactory;
  private final ConstraintValidatorFactory constraintValidatorFactory;

  ResteasyValidator(ValidatorFactory validatorFactory, ConstraintValidatorFactory constraintValidatorFactory, boolean isExecutableValidationEnabled, Set<ExecutableType> defaultValidatedExecutableTypes) {
    super(Validation.buildDefaultValidatorFactory(), isExecutableValidationEnabled, defaultValidatedExecutableTypes);
    this.validatorFactory = validatorFactory;
    this.constraintValidatorFactory = constraintValidatorFactory;
  }

  @Override
  protected Validator getValidator(HttpRequest request) {
    Validator v = Validator.class.cast(request.getAttribute(Validator.class.getName()));
    if (v == null) {
      Locale locale = getLocaleFrom(request);
      v = createValidator(locale);
      request.setAttribute(Validator.class.getName(), v);
    }
    return v;
  }

  private Validator createValidator(Locale locale) {
    MessageInterpolator interpolator = new LocaleSpecificMessageInterpolator(validatorFactory.getMessageInterpolator(), locale);
    return validatorFactory.usingContext()
      .constraintValidatorFactory(constraintValidatorFactory)
      .messageInterpolator(interpolator)
      .getValidator();
  }

  private Locale getLocaleFrom(HttpRequest request) {
    if (request == null) {
      return null;
    }
    List<Locale> locales = request.getHttpHeaders().getAcceptableLanguages();
    return locales == null || locales.isEmpty() ? Locale.ENGLISH : locales.get(0);
  }

}
