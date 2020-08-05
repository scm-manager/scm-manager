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

import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.migration.UpdateStep;
import sonia.scm.security.AnonymousMode;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.StoreConstants;
import sonia.scm.version.Version;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.nio.file.Path;

import static sonia.scm.version.Version.parse;

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
      ScmConfiguration config = configStore.get();
      if (getPreUpdateScmConfigurationFromOldConfig(configFile).isAnonymousAccessEnabled()) {
        config.setAnonymousMode(AnonymousMode.PROTOCOL_ONLY);
      } else {
        config.setAnonymousMode(AnonymousMode.OFF);
      }
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
    return new File(contextProvider.getBaseDirectory(), StoreConstants.CONFIG_DIRECTORY_NAME).toPath();
  }

  @XmlRootElement(name = "scm-config")
  @XmlAccessorType(XmlAccessType.FIELD)
  @NoArgsConstructor
  @Getter
  static class PreUpdateScmConfiguration {
    private boolean anonymousAccessEnabled;
  }
}
