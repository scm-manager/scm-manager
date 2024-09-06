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

package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoBlankStringsValidatorTest {

  @Test
  void shouldAcceptNonEmptyElements() {
    assertTrue(new NoBlankStringsValidator().isValid(Arrays.asList("not", "empty"), null));
  }

  @Test
  void shouldFailForEmptyElements() {
    assertFalse(new NoBlankStringsValidator().isValid(Arrays.asList("one", "", "three"), null));
  }

  @Test
  void shouldAcceptEmptyList() {
    assertTrue(new NoBlankStringsValidator().isValid(emptySet(), null));
  }
}
