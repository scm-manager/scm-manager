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

public class DefaultHealthCheckService implements HealthCheckService {

  private final HealthChecker healthChecker;

  @Inject
  public DefaultHealthCheckService(HealthChecker healthChecker) {
    this.healthChecker = healthChecker;
  }

  @Override
  public void fullCheck(String id) {
    RepositoryPermissions.healthCheck(id).check();
    healthChecker.fullCheck(id);
  }

  @Override
  public void fullCheck(Repository repository) {
    RepositoryPermissions.healthCheck(repository).check();
    healthChecker.fullCheck(repository);
  }

  @Override
  public boolean checkRunning(String repositoryId) {
    return healthChecker.checkRunning(repositoryId);
  }

  @Override
  public boolean checkRunning(Repository repository) {
    return healthChecker.checkRunning(repository.getId());
  }
}
