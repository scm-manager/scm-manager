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

package sonia.scm.cache;


import org.junit.Test;

import static org.junit.Assert.*;


public class CacheStatisticsTest
{

   @Test
  public void testCounters()
  {
    CacheStatistics stats = new CacheStatistics("", 12, 3);

    assertEquals(12, stats.getHitCount());
    assertEquals(3, stats.getMissCount());
    assertEquals(15, stats.getRequestCount());
  }

   @Test
  public void testRates()
  {
    CacheStatistics stats = new CacheStatistics("", 12, 3);

    assertEquals(0.8d, stats.getHitRate(), 0.0);
    assertEquals(0.2d, stats.getMissRate(), 0.0);
  }
}
