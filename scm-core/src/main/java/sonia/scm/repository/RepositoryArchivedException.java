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

import static java.lang.String.format;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

@SuppressWarnings("java:S110") // large history is ok for exceptions
public class RepositoryArchivedException extends ReadOnlyException {

  public static final String CODE = "3hSIlptme1";

  public RepositoryArchivedException(Repository repository) {
    super(entity(repository).build(), format("Repository %s is marked as archived and must not be modified", repository));
  }

  public RepositoryArchivedException(String repositoryId) {
    super(
      entity(Repository.class, repositoryId).build(),
      format("Repository with id %s is marked as archived and must not be modified", repositoryId)
    );
  }

  @Override
  public String getCode() {
    return CODE;
  }
}
