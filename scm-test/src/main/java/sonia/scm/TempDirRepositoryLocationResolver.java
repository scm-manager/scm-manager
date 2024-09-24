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

package sonia.scm;

import sonia.scm.repository.BasicRepositoryLocationResolver;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class TempDirRepositoryLocationResolver extends BasicRepositoryLocationResolver {
  private final File tempDirectory;

  public TempDirRepositoryLocationResolver(File tempDirectory) {
    super(Path.class);
    this.tempDirectory = tempDirectory;
  }

  @Override
  protected <T> RepositoryLocationResolverInstance<T> create(Class<T> type) {
    return new RepositoryLocationResolverInstance<T>() {
      @Override
      public T getLocation(String repositoryId) {
        return (T) tempDirectory.toPath();
      }

      @Override
      public T createLocation(String repositoryId) {
        return (T) tempDirectory.toPath();
      }

      @Override
      public void setLocation(String repositoryId, T location) {
        throw new UnsupportedOperationException("not implemented for tests");
      }

      @Override
      public void forAllLocations(BiConsumer<String, T> consumer) {
        consumer.accept("id", (T) tempDirectory.toPath());
      }
    };
  }
}
