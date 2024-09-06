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

package sonia.scm.repository.api;

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

import java.util.List;

public class ExportFailedException extends ExceptionWithContext {

  private static final String CODE = "67SM3DANZ1";

  public ExportFailedException(List<ContextEntry> context, String message, Exception cause) {
    super(context, message, cause);
  }

  public ExportFailedException(List<ContextEntry> context, String message) {
    super(context, message);
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
