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
import com.google.common.io.Closeables;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

import java.util.Iterator;

/**
 *
 * @author Sebastian Sdorra
 */
public class EhConfigurationReaderTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testDefaultConfiguration()
  {
    EhCacheConfigurationReader reader = createReader("ehcache.001.xml");
    Configuration c = createConfiguration(reader);

    checkDefaultConfiguration(c);
    checkCacheConfiguration(c, "sonia.test.cache.001", 1000l, 30l, 60l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGlobalAttributes()
  {
    EhCacheConfigurationReader reader = createReader("ehcache.006.xml");
    Configuration c = createConfiguration(reader);

    assertFalse(c.getUpdateCheck());
    assertEquals("512M", c.getMaxBytesLocalDiskAsString());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMergeAndOverride()
  {
    //J-
    EhCacheConfigurationReader reader = createReader(
      "ehcache.001.xml",
      Iterators.forArray("ehcache.002.xml", "ehcache.003.xml"),
      "ehcache.004.xml"
    );
    //J+

    Configuration c = createConfiguration(reader);

    // cache sonia.test.cache.001 override by cache.004.xml
    checkCacheConfiguration(c, "sonia.test.cache.001", 6l, 2l, 8l);
    checkCacheConfiguration(c, "sonia.test.cache.002", 2000l, 60l, 120l);
    checkCacheConfiguration(c, "sonia.test.cache.003", 3000l, 120l, 2400l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMergeWithManualConfiguration()
  {
    EhCacheConfigurationReader reader = createReader("ehcache.001.xml", null,
                                          "ehcache.002.xml");

    Configuration c = createConfiguration(reader);

    checkDefaultConfiguration(c);

    checkCacheConfiguration(c, "sonia.test.cache.001", 1000l, 30l, 60l);
    checkCacheConfiguration(c, "sonia.test.cache.002", 2000l, 60l, 120l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMergeWithModuleConfigurations()
  {
    EhCacheConfigurationReader reader = createReader("ehcache.001.xml",
                                          Iterators.forArray("ehcache.002.xml",
                                            "ehcache.003.xml"));

    Configuration c = createConfiguration(reader);

    checkDefaultConfiguration(c);

    checkCacheConfiguration(c, "sonia.test.cache.001", 1000l, 30l, 60l);
    checkCacheConfiguration(c, "sonia.test.cache.002", 2000l, 60l, 120l);
    checkCacheConfiguration(c, "sonia.test.cache.003", 3000l, 120l, 2400l);
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalStateException.class)
  public void testMissingDefaultConfiguration()
  {
    EhCacheConfigurationReader reader = createReader();

    reader.doRead();
  }

  /**
   * Method description
   *
   */
  @Test
  public void testOverrideDefaultConfiguration()
  {
    //J-
    EhCacheConfigurationReader reader = createReader(
      "ehcache.001.xml",
      Iterators.forArray("ehcache.005.xml")
    );
    //J+
    Configuration c = createConfiguration(reader);

    checkDefaultConfiguration(c, 170l, 18900l);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testOverrideGlobalAttributes()
  {
    EhCacheConfigurationReader reader = createReader("ehcache.006.xml", null,
                                          "ehcache.007.xml");
    Configuration c = createConfiguration(reader);

    assertTrue(c.getUpdateCheck());
    assertEquals("1G", c.getMaxBytesLocalDiskAsString());
  }

  /**
   * Method description
   *
   *
   * @param c
   * @param name
   * @param maxEntriesLocalHeap
   * @param timeToIdleSeconds
   * @param timeToLiveSeconds
   */
  private void checkCacheConfiguration(Configuration c, String name,
    long maxEntriesLocalHeap, long timeToIdleSeconds, long timeToLiveSeconds)
  {
    CacheConfiguration cc = c.getCacheConfigurations().get(name);

    assertNotNull(cc);
    assertEquals(maxEntriesLocalHeap, cc.getMaxEntriesLocalHeap());
    assertEquals(timeToIdleSeconds, cc.getTimeToIdleSeconds());
    assertEquals(timeToLiveSeconds, cc.getTimeToLiveSeconds());
  }

  /**
   * Method description
   *
   *
   * @param c
   */
  private void checkDefaultConfiguration(Configuration c)
  {
    checkDefaultConfiguration(c, 100l, 10000l);
  }

  /**
   * Method description
   *
   *
   * @param c
   * @param maxEntriesLocalHeap
   * @param maxEntriesLocalDisk
   */
  private void checkDefaultConfiguration(Configuration c,
    long maxEntriesLocalHeap, long maxEntriesLocalDisk)
  {
    CacheConfiguration dcc = c.getDefaultCacheConfiguration();

    assertNotNull(dcc);
    assertEquals(maxEntriesLocalHeap, dcc.getMaxEntriesLocalHeap());
    assertEquals(maxEntriesLocalDisk, dcc.getMaxEntriesLocalDisk());
  }

  /**
   * Method description
   *
   *
   * @param reader
   *
   * @return
   */
  private Configuration createConfiguration(EhCacheConfigurationReader reader)
  {
    Configuration config = null;
    InputStream content = null;

    try
    {
      content = reader.doRead();
      config = ConfigurationFactory.parseConfiguration(content);
    }
    finally
    {
      Closeables.closeQuietly(content);
    }

    assertNotNull(config);

    return config;
  }

  /**
   * Method description
   *
   *
   * @param defaultConfiguration
   *
   * @return
   */
  private EhCacheConfigurationReader createReader(String defaultConfiguration)
  {
    return new EhCacheConfigurationReader(
      new CacheConfigurationTestLoader(tempFolder, defaultConfiguration));
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
  private EhCacheConfigurationReader createReader(String defaultConfiguration,
    Iterator<String> moduleConfiguration, String manualConfiguration)
  {
    return new EhCacheConfigurationReader(
      new CacheConfigurationTestLoader(
        tempFolder, defaultConfiguration, moduleConfiguration,
          manualConfiguration));
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
  private EhCacheConfigurationReader createReader(String defaultConfiguration,
    Iterator<String> moduleConfiguration)
  {
    return new EhCacheConfigurationReader(
      new CacheConfigurationTestLoader(
        tempFolder, defaultConfiguration, moduleConfiguration));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private EhCacheConfigurationReader createReader()
  {
    return new EhCacheConfigurationReader(
      new CacheConfigurationTestLoader(tempFolder));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
