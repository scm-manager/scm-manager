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


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Result of {@link HealthCheck}.
 *
 * @since 1.36
 */
public final class HealthCheckResult
{

  private static final HealthCheckResult HEALTHY =
    new HealthCheckResult(ImmutableSet.<HealthCheckFailure>of());

  private final Set<HealthCheckFailure> failures;

  private HealthCheckResult(Set<HealthCheckFailure> failures)
  {
    this.failures = failures;
  }


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


  @Override
  public String toString()
  {
    return MoreObjects.toStringHelper(this).add("failures", failures).toString();
  }


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
   */
  public boolean isHealthy()
  {
    return failures.isEmpty();
  }

  /**
   * Returns {@code true} if the result is unhealthy
   */
  public boolean isUnhealthy()
  {
    return !failures.isEmpty();
  }

}
