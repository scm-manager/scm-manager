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

import sonia.scm.BadRequestException;

import java.util.Collections;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class MultipleParentsNotAllowedException extends BadRequestException {
  public MultipleParentsNotAllowedException(String changeset) {
    super(
      Collections.emptyList(),
      String.format("%s has more than one parent changeset, which is not allowed with this request.", changeset));
  }

  @Override
  public String getCode() {
    return "3a47Hzu1e3";
  }
}
