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

package sonia.scm.lifecycle.modules;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import sonia.scm.ConfigurationException;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;

import java.io.File;
public final class ScmConfigurationLoader {

  private final JAXBContext context;

  private final File file;

  ScmConfigurationLoader() {
    try {
      context = JAXBContext.newInstance(ScmConfiguration.class);
      file = new File(SCMContext.getContext().getBaseDirectory(),
        ScmConfiguration.PATH);
    } catch (JAXBException ex) {
      throw new ConfigurationException(ex);
    }
  }

  public ScmConfiguration load() {
    if (file.exists()) {
      try {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (ScmConfiguration) unmarshaller.unmarshal(file);
      } catch (Exception ex) {
        throw new ConfigurationException("could not load config", ex);
      }
    }
    return new ScmConfiguration();
  }
}
