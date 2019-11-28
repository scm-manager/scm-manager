package sonia.scm.update.xml;

import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.update.V1PropertyReader;

import javax.inject.Inject;
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
