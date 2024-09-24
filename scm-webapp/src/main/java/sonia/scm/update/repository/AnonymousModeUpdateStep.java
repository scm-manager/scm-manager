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

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AnonymousMode;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.version.Version;

import java.nio.file.Path;
import java.nio.file.Paths;

import static sonia.scm.version.Version.parse;

@Extension
public class AnonymousModeUpdateStep implements UpdateStep {

  private final SCMContextProvider contextProvider;
  private final ConfigurationStore<ScmConfiguration> configStore;

  @Inject
  public AnonymousModeUpdateStep(SCMContextProvider contextProvider, ConfigurationStoreFactory configurationStoreFactory) {
    this.contextProvider = contextProvider;
    this.configStore = configurationStoreFactory.withType(ScmConfiguration.class).withName("config").build();
  }

  @Override
  public void doUpdate() throws JAXBException {
    Path configFile = determineConfigDirectory().resolve("config" + StoreConstants.FILE_EXTENSION);

    if (configFile.toFile().exists()) {
      PreUpdateScmConfiguration oldConfig = getPreUpdateScmConfigurationFromOldConfig(configFile);
      ScmConfiguration config = configStore.get();
      if (oldConfig.isAnonymousAccessEnabled()) {
        config.setAnonymousMode(AnonymousMode.PROTOCOL_ONLY);
      } else {
        config.setAnonymousMode(AnonymousMode.OFF);
      }
      configStore.set(config);
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.4.0");
  }

  @Override
  public String getAffectedDataType() {
    return "config.xml";
  }

  private PreUpdateScmConfiguration getPreUpdateScmConfigurationFromOldConfig(Path configFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(AnonymousModeUpdateStep.PreUpdateScmConfiguration.class);
    return (AnonymousModeUpdateStep.PreUpdateScmConfiguration) jaxbContext.createUnmarshaller().unmarshal(configFile.toFile());
  }

  private Path determineConfigDirectory() {
    return contextProvider.resolve(Paths.get(StoreConstants.CONFIG_DIRECTORY_NAME));
  }

  @XmlRootElement(name = "scm-config")
  @XmlAccessorType(XmlAccessType.FIELD)
  @NoArgsConstructor
  @Getter
  static class PreUpdateScmConfiguration {
    private boolean anonymousAccessEnabled;
  }
}
