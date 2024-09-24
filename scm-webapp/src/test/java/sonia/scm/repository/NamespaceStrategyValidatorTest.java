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

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import sonia.scm.ScmConstraintViolationException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceStrategyValidatorTest {

  @Test
  void shouldThrowConstraintValidationException() {
    NamespaceStrategyValidator validator = new NamespaceStrategyValidator(Collections.emptySet());
    assertThrows(ScmConstraintViolationException.class, () -> validator.check("AwesomeStrategy"));
  }

  @Test
  void shouldDoNotThrowAnException() {
    NamespaceStrategyValidator validator = new NamespaceStrategyValidator(Sets.newHashSet(new AwesomeStrategy()));
    validator.check("AwesomeStrategy");
  }

  public static class AwesomeStrategy implements NamespaceStrategy {

    @Override
    public String createNamespace(Repository repository) {
      return null;
    }
  }

}
