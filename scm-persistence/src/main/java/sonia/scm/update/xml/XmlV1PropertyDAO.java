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

package sonia.scm.update.xml;

import jakarta.inject.Inject;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.update.V1PropertyReader;

import java.util.Map;

public class XmlV1PropertyDAO implements V1PropertyDAO {

  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public XmlV1PropertyDAO(ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  @Override
  public V1PropertyReader.Instance getProperties(V1PropertyReader reader) {
    ConfigurationEntryStore<V1Properties> propertyStore = configurationEntryStoreFactory
      .withType(V1Properties.class)
      .withName(reader.getStoreName())
      .build();
    Map<String, V1Properties> all = propertyStore.getAll();
    return reader.createInstance(all);
  }
}
