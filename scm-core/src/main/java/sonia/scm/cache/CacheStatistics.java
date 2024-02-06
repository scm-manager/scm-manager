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

package sonia.scm.cache;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Statistics about the performance of a {@link Cache}.
 * Instances of this class are immutable.
 *
 * @since 2.0.0
 */
public final class CacheStatistics {

  private final long hitCount;

  private final long missCount;

  /**
   * name of cache
   */
  private final String name;

  /**
   * Constructs a new performance statistic for a {@link Cache}.
   *
   * @param name      name of the cache
   * @param hitCount  hit count
   * @param missCount miss count
   */
  public CacheStatistics(String name, long hitCount, long missCount) {
    this.name = name;
    this.hitCount = hitCount;
    this.missCount = missCount;
  }


 
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final CacheStatistics other = (CacheStatistics) obj;

    return Objects.equal(name, other.name)
      && Objects.equal(hitCount, other.hitCount)
      && Objects.equal(missCount, other.missCount);
  }

 
  @Override
  public int hashCode() {
    return Objects.hashCode(name, hitCount, missCount);
  }

 
  @Override
  public String toString() {
    //J-
    return MoreObjects.toStringHelper(this)
      .add("name", name)
      .add("hitCount", hitCount)
      .add("missCount", missCount)
      .toString();
    //J+
  }


  /**
   * Returns number of times requested elements were found in the cache.
   *
   * @return number of cache hits
   */
  public long getHitCount() {
    return hitCount;
  }

  /**
   * Returns the ratio of cache requests which were hits.
   */
  public double getHitRate() {
    return ratio(hitCount);
  }

  /**
   * Returns number of times a requested element was not found in the cache.
   */
  public long getMissCount() {
    return missCount;
  }

  /**
   * Returns the ratio of cache requests which were misses.
   */
  public double getMissRate() {
    return ratio(missCount);
  }

  /**
   * Returns name of the cache.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the total number of requests, this includes hits and misses.
   */
  public long getRequestCount() {
    return hitCount + missCount;
  }

  /**
   * Calculates the ratio of a counter.
   *
   * @param counter counter
   * @return rate of counter
   */
  private double ratio(long counter) {
    long requestCount = getRequestCount();

    return (requestCount == 0)
      ? 1.0
      : (double) counter / requestCount;
  }


}
