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



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

/**
 * Statistics about the performance of a {@link Cache}.
 * Instances of this class are immutable.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class CacheStatistics
{

  /**
   * Constructs a new performance statistic for a {@link Cache}.
   *
   *
   * @param name name of the cache
   * @param hitCount hit count
   * @param missCount miss count
   */
  public CacheStatistics(String name, long hitCount, long missCount)
  {
    this.name = name;
    this.hitCount = hitCount;
    this.missCount = missCount;
  }

  //~--- methods --------------------------------------------------------------

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

    final CacheStatistics other = (CacheStatistics) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(hitCount, other.hitCount)
      && Objects.equal(missCount, other.missCount);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(name, hitCount, missCount);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
                  .add("name", name)
                  .add("hitCount", hitCount)
                  .add("missCount", missCount)
                  .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns number of times requested elements were found in the cache.
   *
   *
   * @return number of cache hits
   */
  public long getHitCount()
  {
    return hitCount;
  }

  /**
   * Returns the ratio of cache requests which were hits.
   *
   *
   * @return ratio of cache hits
   */
  public double getHitRate()
  {
    return ratio(hitCount);
  }

  /**
   * Returns number of times a requested element was not found in the cache.
   *
   *
   * @return number of cache misses
   */
  public long getMissCount()
  {
    return missCount;
  }

  /**
   * Returns the ratio of cache requests which were misses.
   *
   *
   * @return ratio of cache misses
   */
  public double getMissRate()
  {
    return ratio(missCount);
  }

  /**
   * Returns name of the cache.
   *
   *
   * @return name of the cache
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the total number of requests, this includes hits and misses.
   *
   *
   * @return numer of requests
   */
  public long getRequestCount()
  {
    return hitCount + missCount;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Calculates the ratio of a counter.
   *
   *
   * @param counter counter
   *
   * @return rate of counter
   */
  private double ratio(long counter)
  {
    long requestCount = getRequestCount();

    return (requestCount == 0)
      ? 1.0
      : (double) counter / requestCount;
  }

  //~--- fields ---------------------------------------------------------------

  /** hit count */
  private final long hitCount;

  /** miss count */
  private final long missCount;

  /** name of cache */
  private final String name;
}
