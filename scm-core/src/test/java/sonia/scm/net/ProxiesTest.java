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

package sonia.scm.net;

import com.google.common.collect.Sets;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProxiesTest {

  @Test
  public void testDisabledWithoutExcludes() {
    ScmConfiguration config = createConfiguration(false);

    assertFalse(Proxies.isEnabled(config, "localhost"));
    assertFalse(Proxies.isEnabled(config, "localhost:8082"));
    assertFalse(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  @Test
  public void testEnabledWithoutExcludes() {
    ScmConfiguration config = createConfiguration(true);

    assertTrue(Proxies.isEnabled(config, "localhost"));
    assertTrue(Proxies.isEnabled(config, "localhost:8082"));
    assertTrue(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertTrue(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertTrue(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  @Test
  public void testWithExcludes() {
    ScmConfiguration config = createConfiguration(true, "127.0.0.1",
      "localhost");

    assertFalse(Proxies.isEnabled(config, "localhost"));
    assertFalse(Proxies.isEnabled(config, "localhost:8082"));
    assertTrue(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  @Test
  public void testWithGlobExcludes() {
    ScmConfiguration config = createConfiguration(true, "127.*", "*host");

    assertFalse(Proxies.isEnabled(config, "localhost"));
    assertFalse(Proxies.isEnabled(config, "localhost:8082"));
    assertTrue(Proxies.isEnabled(config, "download.scm-manager.org"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1"));
    assertFalse(Proxies.isEnabled(config, "http://127.0.0.1/test/ka"));
  }

  private ScmConfiguration createConfiguration(boolean enabled,
                                               String... excludes) {
    ScmConfiguration configuration = new ScmConfiguration();

    configuration.setEnableProxy(enabled);
    configuration.setProxyExcludes(Sets.newHashSet(excludes));

    return configuration;
  }
}
