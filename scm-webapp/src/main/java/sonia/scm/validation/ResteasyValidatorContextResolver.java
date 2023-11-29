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

import jakarta.inject.Inject;
import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validation;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.spi.validation.GeneralValidator;

@Provider
public class ResteasyValidatorContextResolver implements ContextResolver<GeneralValidator> {

  private final ConstraintValidatorFactory constraintValidatorFactory;

  @Inject
  public ResteasyValidatorContextResolver(ConstraintValidatorFactory constraintValidatorFactory) {
    this.constraintValidatorFactory = constraintValidatorFactory;
  }

  @Override
  public GeneralValidator getContext(Class<?> type) {
    Configuration<?> configuration = Validation.byDefaultProvider().configure();
    BootstrapConfiguration bootstrapConfiguration = configuration.getBootstrapConfiguration();

    return new ResteasyValidator(
      configuration.buildValidatorFactory(),
      constraintValidatorFactory,
      bootstrapConfiguration.isExecutableValidationEnabled(),
      bootstrapConfiguration.getDefaultValidatedExecutableTypes()
    );
  }
}
