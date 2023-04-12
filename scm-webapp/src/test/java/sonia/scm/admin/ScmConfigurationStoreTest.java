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

package sonia.scm.admin;

import org.junit.jupiter.api.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ScmConfigurationStoreTest {

  @Test
  void shouldGetDefaultConfig() {
    ScmConfiguration scmConfiguration = new ScmConfigurationStore(new InMemoryConfigurationStoreFactory(), new ScmConfiguration()).get();

    assertThat(scmConfiguration.getPluginUrl()).isEqualTo("https://plugin-center-api.scm-manager.org/api/v1/plugins/{version}?os={os}&arch={arch}&jre={jre}");
  }

  @Test
  void shouldUpdateConfig() {
    ScmConfigurationStore store = new ScmConfigurationStore(new InMemoryConfigurationStoreFactory(), new ScmConfiguration());

    ScmConfiguration config = new ScmConfiguration();
    config.setBaseUrl("test.de");
    config.setProxyPassword("secret");
    store.store(config);

    ScmConfiguration scmConfiguration = store.get();

    assertThat(scmConfiguration.getBaseUrl()).isEqualTo("test.de");
    assertThat(scmConfiguration.getProxyPassword()).isEqualTo("secret");
  }

}
