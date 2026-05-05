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

import sonia.scm.BadRequestException;
import sonia.scm.ContextEntry;

public class MalformedLocalDateException extends BadRequestException {

  private static final String CODE = "pFV4DjjWc";

  public MalformedLocalDateException(String value) {
    super(
      ContextEntry.ContextBuilder.noContext(),
      String.format("'%s' is an invalid string for a local date", value)
    );
  }

  public String getCode() {
    return CODE;
  }
}
