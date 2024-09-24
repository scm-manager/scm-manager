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

package sonia.scm.repository;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates the name of a repository.
 * @since 2.33.0
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RepositoryNameConstrainValidator.class)
public @interface RepositoryName {

  String message() default "{sonia.scm.repository.RepositoryName.message}";
  Class<?>[] groups() default { };
  Class<? extends Payload>[] payload() default { };

  /**
   * Specify namespace prefix validation. Default is {@link Namespace#NONE}.
   *
   * @return namespace validation
   */
  Namespace namespace() default Namespace.NONE;

  /**
   * Options to control the namespace prefix validation.
   */
  enum Namespace {
    /**
     * The repository name does not contain a namespace prefix.
     */
    NONE,

    /**
     * The repository name can contain a namespace prefix.
     */
    OPTIONAL,

    /**
     * The repository name must start with a namespace prefix.
     */
    REQUIRED;
  }
}
