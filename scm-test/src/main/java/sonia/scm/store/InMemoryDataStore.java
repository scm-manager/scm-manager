package sonia.scm.store;

import sonia.scm.security.KeyGenerator;
import sonia.scm.security.UUIDKeyGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In memory store implementation of {@link DataStore}.
 *
 * @author Sebastian Sdorra
 *
 * @param <T> type of stored object
 */
public class InMemoryDataStore<T> implements DataStore<T> {

  private final Map<String, T> store = new HashMap<>();
  private KeyGenerator generator = new UUIDKeyGenerator();

  @Override
  public String put(T item) {
    String key = generator.createKey();
    store.put(key, item);
    return key;
  }

  @Override
  public void put(String id, T item) {
    store.put(id, item);
  }

  @Override
  public Map<String, T> getAll() {
    return Collections.unmodifiableMap(store);
  }

  @Override
  public void clear() {
    store.clear();
  }

  @Override
  public void remove(String id) {
    store.remove(id);
  }

  @Override
  public T get(String id) {
    return store.get(id);
  }
}
