/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
