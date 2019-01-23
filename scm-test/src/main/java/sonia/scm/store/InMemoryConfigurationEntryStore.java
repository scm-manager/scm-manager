package sonia.scm.store;

import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryConfigurationEntryStore<V> implements ConfigurationEntryStore<V> {

  private final Map<String, V> values = new HashMap<>();

  @Override
  public Collection<V> getMatchingValues(Predicate<V> predicate) {
    return values.values().stream().filter(predicate).collect(Collectors.toList());
  }

  @Override
  public String put(V item) {
    String key = UUID.randomUUID().toString();
    values.put(key, item);
    return key;
  }

  @Override
  public void put(String id, V item) {
    values.put(id, item);
  }

  @Override
  public Map<String, V> getAll() {
    return Collections.unmodifiableMap(values);
  }

  @Override
  public void clear() {
    values.clear();
  }

  @Override
  public void remove(String id) {
    values.remove(id);
  }

  @Override
  public V get(String id) {
    return values.get(id);
  }
}
