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

import sonia.scm.ContextEntry;
import sonia.scm.ExceptionWithContext;

import java.util.List;

public class InvalidRepositoryTypeException extends ExceptionWithContext {

  private static final String CODE = "8OT4gBVvp1";

  public static InvalidRepositoryTypeException create(Repository repository) {
    return new InvalidRepositoryTypeException(ContextEntry.ContextBuilder.entity(repository).build());
  }

  private InvalidRepositoryTypeException(List<ContextEntry> context) {
    super(context, "The repository type is not supported");
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
