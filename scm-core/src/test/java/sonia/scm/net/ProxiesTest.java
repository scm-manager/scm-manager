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
