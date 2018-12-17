package sonia.scm.store;

/**
 * In memory configuration store factory for testing purposes.
 *
 * @author Sebastian Sdorra
 */
public class InMemoryDataStoreFactory implements DataStoreFactory {

  private InMemoryDataStore store;

  public InMemoryDataStoreFactory() {
  }

  public InMemoryDataStoreFactory(InMemoryDataStore store) {
    this.store = store;
  }

  @Override
  public <T> DataStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    if (store != null) {
      return store;
    }
    return new InMemoryDataStore<>();
  }
}
