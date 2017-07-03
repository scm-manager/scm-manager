/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

/**
 * Result of {@link HealthCheck}.
 *
 * @author Sebastian Sdorra
 * @since 1.36
 */
public final class HealthCheckResult
{

  /** healthy result */
  private static final HealthCheckResult HEALTHY =
    new HealthCheckResult(ImmutableSet.of());

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param failures
   */
  private HealthCheckResult(Set<HealthCheckFailure> failures)
  {
    this.failures = failures;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns a {@link HealthCheckResult} for a healthy repository.
   *
   * @return {@link HealthCheckResult} for a healthy repository
   */
  public static HealthCheckResult healthy()
  {
    return HEALTHY;
  }

  /**
   * Returns a {@link HealthCheckResult} for a unhealthy repository.
   *
   *
   * @param failures failures of failed {@link HealthCheck}s
   *
   * @return {@link HealthCheckResult} for a unhealthy repository
   */
  public static HealthCheckResult unhealthy(
    Iterable<HealthCheckFailure> failures)
  {
    return new HealthCheckResult(ImmutableSet.copyOf(failures));
  }

  /**
   * Returns a {@link HealthCheckResult} for a unhealthy repository.
   *
   *
   * @param failure failure of {@link HealthCheck}
   * @param otherFailures failures of failed {@link HealthCheck}s
   *
   * @return {@link HealthCheckResult} for a unhealthy repository
   */
  public static HealthCheckResult unhealthy(HealthCheckFailure failure,
    HealthCheckFailure... otherFailures)
  {
    //J-
    return new HealthCheckResult( 
      ImmutableSet.<HealthCheckFailure>builder()
        .add(failure)
        .add(otherFailures)
        .build() 
    );
    //J+
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final HealthCheckResult other = (HealthCheckResult) obj;

    return Objects.equal(failures, other.failures);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(failures);
  }

  /**
   * Merge this {@link HealthCheckResult} with another 
   * {@link HealthCheckResult}.
   *
   *
   * @param otherResult result to merge with
   *
   * @return merged {@link HealthCheckResult}
   */
  public HealthCheckResult merge(HealthCheckResult otherResult)
  {
    HealthCheckResult merged;

    if ((otherResult == null) || otherResult.isHealthy())
    {
      merged = this;
    }
    else
    {
      //J-
      merged = new HealthCheckResult(
        ImmutableSet.<HealthCheckFailure>builder()
          .addAll(failures)
          .addAll(otherResult.failures)
          .build()
      );
      //J+
    }

    return merged;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return Objects.toStringHelper(this).add("failures", failures).toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a {@link Set} of {@link HealthCheckFailure}s. The set is empty if 
   * the repository is healthy.
   *
   * @return {@link Set} of {@link HealthCheckFailure}s
   */
  public Set<HealthCheckFailure> getFailures()
  {
    return failures;
  }

  /**
   * Returns {@code true} if the result is healthy.
   *
   * @return {@code true} if the result is healthy
   */
  public boolean isHealthy()
  {
    return failures.isEmpty();
  }

  /**
   * Returns {@code true} if the result is unhealthy
   *
   * @return {@code true} if the result is unhealthy.
   */
  public boolean isUnhealthy()
  {
    return !failures.isEmpty();
  }

  //~--- fields ---------------------------------------------------------------

  /** set of failures */
  private final Set<HealthCheckFailure> failures;
}
