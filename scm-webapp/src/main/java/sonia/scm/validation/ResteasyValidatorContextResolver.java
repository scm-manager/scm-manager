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
