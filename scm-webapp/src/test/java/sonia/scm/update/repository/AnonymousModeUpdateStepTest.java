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

package sonia.scm.update.repository;

import com.google.common.io.Resources;
import jakarta.xml.bind.JAXBException;
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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
