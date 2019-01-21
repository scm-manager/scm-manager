package sonia.scm.store;

public class InMemoryConfigurationEntryStoreFactory implements ConfigurationEntryStoreFactory {




  private ConfigurationEntryStore store;

  public static ConfigurationEntryStoreFactory create() {
    return new InMemoryConfigurationEntryStoreFactory(new InMemoryConfigurationEntryStore());
  }

  public InMemoryConfigurationEntryStoreFactory() {
  }

  public InMemoryConfigurationEntryStoreFactory(ConfigurationEntryStore store) {
    this.store = store;
  }

  @Override
  public <T> ConfigurationEntryStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    if (store != null) {
      return store;
    }
    return new InMemoryConfigurationEntryStore<>();
  }
}
