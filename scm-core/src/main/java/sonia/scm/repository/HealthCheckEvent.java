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

import lombok.AllArgsConstructor;
import sonia.scm.event.Event;

import java.util.Collection;

import static java.util.Collections.unmodifiableCollection;

/**
 * This event is triggered whenever a health check was run and either found issues
 * or issues reported earlier are fixed (that is, health has changed).
 *
 * @since 2.17.0
 */
@Event
@AllArgsConstructor
public class HealthCheckEvent {

  private final Repository repository;
  private final Collection<HealthCheckFailure> previousFailures;
  private final Collection<HealthCheckFailure> currentFailures;

  public Repository getRepository() {
    return repository;
  }

  public Collection<HealthCheckFailure> getPreviousFailures() {
    return unmodifiableCollection(previousFailures);
  }

  public Collection<HealthCheckFailure> getCurrentFailures() {
    return unmodifiableCollection(currentFailures);
  }
}
