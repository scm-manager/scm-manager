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

import static java.util.Collections.emptyList;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NoCommonHistoryException extends BadRequestException {

  public NoCommonHistoryException() {
    this("no common history");
  }

  public NoCommonHistoryException(String message) {
    super(emptyList(), message);
  }

  @Override
  public String getCode() {
    return "4iRct4avG1";
  }
}
