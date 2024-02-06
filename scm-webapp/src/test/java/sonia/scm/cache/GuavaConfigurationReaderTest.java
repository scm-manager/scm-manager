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


import com.google.common.collect.Iterators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

import java.util.Iterator;


public class GuavaConfigurationReaderTest
{

   @Test
  public void testDefaultConfiguration()
  {
    GuavaCacheConfiguration cfg =
      readConfiguration("gcache.001.xml").getDefaultCache();

    assertCacheValues(cfg, 200L, 1200L, 2400L);
  }

   @Test
  public void testMergeAndOverride()
  {
    //J-
    GuavaCacheManagerConfiguration gcm = readConfiguration(
      "gcache.001.xml",
      Iterators.forArray("gcache.002.xml", "gcache.003.xml"),
      "gcache.004.xml"
    );
    //J+

    // cache sonia.test.cache.001 override by cache.004.xml
    assertCacheValues(getCache(gcm, "sonia.test.cache.001"), 6L, 2L, 8L);
    assertCacheValues(getCache(gcm, "sonia.test.cache.002"), 1000L, 120L, 60L);
    assertCacheValues(getCache(gcm, "sonia.test.cache.003"), 3000L, 120L,
                      2400L);
  }

   @Test
  public void testMergeWithManualConfiguration()
  {
    GuavaCacheManagerConfiguration gcm = readConfiguration("gcache.001.xml",
                                           null, "gcache.002.xml");

    // check default

    assertCacheValues(getCache(gcm, "sonia.test.cache.001"), 1000L, 60L, 30L);
    assertCacheValues(getCache(gcm, "sonia.test.cache.002"), 1000L, 120L, 60L);
  }

   @Test
  public void testMergeWithModuleConfigurations()
  {
    GuavaCacheManagerConfiguration gcm = readConfiguration("gcache.001.xml",
                                           Iterators.forArray("gcache.002.xml",
                                             "gcache.003.xml"));

    assertCacheValues(getCache(gcm, "sonia.test.cache.001"), 1000L, 60L, 30L);
    assertCacheValues(getCache(gcm, "sonia.test.cache.002"), 1000L, 120L, 60L);
    assertCacheValues(getCache(gcm, "sonia.test.cache.003"), 3000L, 120L,
                      2400L);
  }

   @Test
  public void testSimpleConfiguration()
  {
    GuavaCacheConfiguration cfg =
      readConfiguration("gcache.001.xml").getCaches().get(0);

    assertCacheValues(cfg, 1000L, 60L, 30L);
  }

   @Test
  public void testWithoutDefaultConfiguration()
  {
    GuavaCacheConfiguration cfg =
      readConfiguration("gcache.002.xml").getCaches().get(0);

    assertCacheValues(cfg, 1000L, 120L, 60L);
  }

  /**
   * Method description
   *
   *
   * @param cfg
   * @param maximumSize
   * @param expireAfterAccess
   * @param expireAfterWrite
   */
  private void assertCacheValues(GuavaCacheConfiguration cfg, long maximumSize,
    long expireAfterAccess, long expireAfterWrite)
  {
    assertEquals(Long.valueOf(maximumSize), cfg.getMaximumSize());
    assertEquals(Long.valueOf(expireAfterAccess), cfg.getExpireAfterAccess());
    assertEquals(Long.valueOf(expireAfterWrite), cfg.getExpireAfterWrite());
  }


  private GuavaCacheManagerConfiguration readConfiguration(
    String defaultConfiguration)
  {
    return readConfiguration(defaultConfiguration, null, null);
  }


  private GuavaCacheManagerConfiguration readConfiguration(
    String defaultConfiguration, Iterator<String> moduleConfiguration)
  {
    return readConfiguration(defaultConfiguration, moduleConfiguration, null);
  }

  /**
   * Method description
   *
   *
   * @param defaultConfiguration
   * @param moduleConfiguration
   * @param manualConfiguration
   *
   * @return
   */
  private GuavaCacheManagerConfiguration readConfiguration(
    String defaultConfiguration, Iterator<String> moduleConfiguration,
    String manualConfiguration)
  {
    return new GuavaCacheConfigurationReader(
      new CacheConfigurationTestLoader(
        tempFolder, defaultConfiguration, moduleConfiguration,
          manualConfiguration)).read();
  }



  private GuavaNamedCacheConfiguration getCache(
    GuavaCacheManagerConfiguration gcmc, String name)
  {
    GuavaNamedCacheConfiguration cache = null;

    for (GuavaNamedCacheConfiguration gncc : gcmc.getCaches())
    {
      if (name.equals(gncc.getName()))
      {
        cache = gncc;
      }
    }

    return cache;
  }

  //~--- fields ---------------------------------------------------------------

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
