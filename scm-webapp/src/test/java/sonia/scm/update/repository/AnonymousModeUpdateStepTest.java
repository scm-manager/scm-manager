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

package sonia.scm.update.repository;

import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static sonia.scm.store.InMemoryConfigurationStoreFactory.create;

@ExtendWith(MockitoExtension.class)
class AnonymousModeUpdateStepTest {

  @Mock
  private SCMContextProvider contextProvider;

  private AnonymousModeUpdateStep updateStep;
  private ConfigurationStore<ScmConfiguration> configurationStore;

  private Path configDir;

  @BeforeEach
  void initUpdateStep(@TempDir Path tempDir) throws IOException {
    when(contextProvider.resolve(any(Path.class))).thenReturn(tempDir.toAbsolutePath());
    configDir = tempDir;
    Files.createDirectories(configDir);
    InMemoryConfigurationStoreFactory inMemoryConfigurationStoreFactory = create();
    configurationStore = inMemoryConfigurationStoreFactory.get("config", null);
    updateStep = new AnonymousModeUpdateStep(contextProvider, inMemoryConfigurationStoreFactory);
  }

  @Test
  void shouldNotUpdateIfConfigFileNotAvailable() throws JAXBException {
    updateStep.doUpdate();

    assertThat(configurationStore.getOptional()).isNotPresent();
  }

  @Test
  void shouldUpdateDisabledAnonymousMode() throws JAXBException, IOException {
    copyTestDatabaseFile(configDir, "config.xml", "config.xml");
    configurationStore.set(new ScmConfiguration());

    updateStep.doUpdate();

    assertThat((configurationStore.get()).getAnonymousMode()).isEqualTo(AnonymousMode.OFF);
  }

  @Test
  void shouldUpdateEnabledAnonymousMode() throws JAXBException, IOException {
    copyTestDatabaseFile(configDir, "config-withAnon.xml", "config.xml");
    configurationStore.set(new ScmConfiguration());

    updateStep.doUpdate();

    assertThat((configurationStore.get()).getAnonymousMode()).isEqualTo(AnonymousMode.PROTOCOL_ONLY);
  }

  private void copyTestDatabaseFile(Path configDir, String sourceFileName, String targetFileName) throws IOException {
    URL url = Resources.getResource("sonia/scm/update/security/" + sourceFileName);
    Files.copy(url.openStream(), configDir.resolve(targetFileName));
  }
}
