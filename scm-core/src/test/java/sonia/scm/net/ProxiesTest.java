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



package sonia.scm.net;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;

import org.junit.Test;

import sonia.scm.config.ScmConfiguration;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class ProxiesTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testDisabledWithoutExcludes()
  {
    ScmConfiguration config = createConfiguration(false);

    assertFalse(Proxies.isEnabled(config, "localhost"));
    assertFalse(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testEnabledWithoutExcludes()
  {
    ScmConfiguration config = createConfiguration(true);

    assertTrue(Proxies.isEnabled(config, "localhost"));
    assertTrue(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertTrue(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertTrue(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testWithExcludes()
  {
    ScmConfiguration config = createConfiguration(true, "127.0.0.1",
                                "localhost");

    assertFalse(Proxies.isEnabled(config, "localhost"));
    assertTrue(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testWithGlobExcludes()
  {
    ScmConfiguration config = createConfiguration(true, "127.*", "*host");

    assertFalse(Proxies.isEnabled(config, "localhost"));
    assertTrue(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  /**
   * Method description
   *
   *
   * @param enabled
   * @param excludes
   *
   * @return
   */
  private ScmConfiguration createConfiguration(boolean enabled,
    String... excludes)
  {
    ScmConfiguration configuration = new ScmConfiguration();

    configuration.setEnableProxy(enabled);
    configuration.setProxyExcludes(Sets.newHashSet(excludes));

    return configuration;
  }
}
