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

package sonia.scm.validation;

import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableType;
import org.jboss.resteasy.plugins.validation.GeneralValidatorImpl;
import org.jboss.resteasy.spi.HttpRequest;

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
