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

import com.google.common.collect.Iterators;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;

/**
 *
 * @author Sebastian Sdorra
 */
public class GuavaConfigurationReaderTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testDefaultConfiguration()
  {
    GuavaCacheConfiguration cfg =
      readConfiguration("gcache.001.xml").getDefaultCache();

    assertCacheValues(cfg, 200l, 1200l, 2400l);
  }

  /**
   * Method description
   *
   */
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
    assertCacheValues(getCache(gcm, "sonia.test.cache.001"), 6l, 2l, 8l);
    assertCacheValues(getCache(gcm, "sonia.test.cache.002"), 1000l, 120l, 60l);
    assertCacheValues(getCache(gcm, "sonia.test.cache.003"), 3000l, 120l,
      2400l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMergeWithManualConfiguration()
  {
    GuavaCacheManagerConfiguration gcm = readConfiguration("gcache.001.xml",
                                           null, "gcache.002.xml");

    // check default

    assertCacheValues(getCache(gcm, "sonia.test.cache.001"), 1000l, 60l, 30l);
    assertCacheValues(getCache(gcm, "sonia.test.cache.002"), 1000l, 120l, 60l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMergeWithModuleConfigurations()
  {
    GuavaCacheManagerConfiguration gcm = readConfiguration("gcache.001.xml",
                                           Iterators.forArray("gcache.002.xml",
                                             "gcache.003.xml"));

    assertCacheValues(getCache(gcm, "sonia.test.cache.001"), 1000l, 60l, 30l);
    assertCacheValues(getCache(gcm, "sonia.test.cache.002"), 1000l, 120l, 60l);
    assertCacheValues(getCache(gcm, "sonia.test.cache.003"), 3000l, 120l,
      2400l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSimpleConfiguration()
  {
    GuavaCacheConfiguration cfg =
      readConfiguration("gcache.001.xml").getCaches().get(0);

    assertCacheValues(cfg, 1000l, 60l, 30l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testWithoutDefaultConfiguration()
  {
    GuavaCacheConfiguration cfg =
      readConfiguration("gcache.002.xml").getCaches().get(0);

    assertCacheValues(cfg, 1000l, 120l, 60l);
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

  /**
   * Method description
   *
   *
   * @param defaultConfiguration
   *
   * @return
   */
  private GuavaCacheManagerConfiguration readConfiguration(
    String defaultConfiguration)
  {
    return readConfiguration(defaultConfiguration, null, null);
  }

  /**
   * Method description
   *
   *
   * @param defaultConfiguration
   * @param moduleConfiguration
   *
   * @return
   */
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
          manualConfiguration)).doRead();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param gcmc
   * @param name
   *
   * @return
   */
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

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
