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
import jakarta.inject.Singleton;
import sonia.scm.event.ScmEventBus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Collections.emptyList;

@Singleton
class RepositoryPostProcessor {

  private final ScmEventBus eventBus;

  private final Map<String, List<HealthCheckFailure>> checkResults = new HashMap<>();

  @Inject
  RepositoryPostProcessor(ScmEventBus eventBus) {
    this.eventBus = eventBus;
  }

  void setCheckResults(Repository repository, Collection<HealthCheckFailure> failures) {
    List<HealthCheckFailure> oldFailures = getCheckResults(repository.getId());
    List<HealthCheckFailure> copyOfFailures = copyOf(failures);
    checkResults.put(repository.getId(), copyOfFailures);
    repository.setHealthCheckFailures(copyOfFailures);
    eventBus.post(new HealthCheckEvent(repository, oldFailures, copyOfFailures));
  }

  void postProcess(Repository repository) {
    repository.setHealthCheckFailures(getCheckResults(repository.getId()));
  }

  private List<HealthCheckFailure> getCheckResults(String repositoryId) {
    return checkResults.getOrDefault(repositoryId, emptyList());
  }
}
