/*
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

package sonia.scm.repository;

import jakarta.inject.Inject;
import sonia.scm.plugin.Extension;

import java.nio.file.Files;
import java.nio.file.Path;

@Extension
public final class MetadataHealthCheck implements HealthCheck {

  public static final HealthCheckFailure REPOSITORY_Directory_NOT_WRITABLE =
    new HealthCheckFailure("9cSV1eaVF1",
      "repository directory not writable",
      "The system user has no permissions to create or delete files in the repository directory.");
  public static final HealthCheckFailure METADATA_NOT_WRITABLE =
    new HealthCheckFailure("6bSUg4dZ41",
      "metadata file not writable",
      "The system user has no permissions to modify the metadata file for the repository.");
  private final RepositoryLocationResolver locationResolver;

  @Inject
  public MetadataHealthCheck(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public HealthCheckResult check(Repository repository) {
    Path repositoryLocation = locationResolver.forClass(Path.class).getLocation(repository.getId());
    if (!Files.isWritable(repositoryLocation)) {
      return HealthCheckResult.unhealthy(REPOSITORY_Directory_NOT_WRITABLE);
    }
    Path metadata = repositoryLocation.resolve("metadata.xml");
    if (!Files.isWritable(metadata)) {
      return HealthCheckResult.unhealthy(METADATA_NOT_WRITABLE);
    }
    return HealthCheckResult.healthy();
  }
}
