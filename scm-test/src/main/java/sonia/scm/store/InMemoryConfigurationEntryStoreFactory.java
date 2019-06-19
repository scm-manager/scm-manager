package sonia.scm.store;

import java.util.HashMap;
import java.util.Map;

public class InMemoryConfigurationEntryStoreFactory implements ConfigurationEntryStoreFactory {

  private final Map<String, InMemoryConfigurationEntryStore> stores = new HashMap<>();

  public static InMemoryConfigurationEntryStoreFactory create() {
    return new InMemoryConfigurationEntryStoreFactory();
  }

  @Override
  public <T> ConfigurationEntryStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    String name = storeParameters.getName();
    return get(name);
  }

  public <T> InMemoryConfigurationEntryStore<T> get(String name) {
    return stores.computeIfAbsent(name, x -> new InMemoryConfigurationEntryStore());
  }
}
