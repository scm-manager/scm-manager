package sonia.scm.update;

import java.util.Map;
import java.util.function.BiConsumer;

public class RepositoryV1PropertyReader implements V1PropertyReader {
  @Override
  public String getStoreName() {
    return "repository-properties-v1";
  }

  @Override
  public Instance createInstance(Map<String, V1Properties> all) {
    return propertiesForNameConsumer ->
      all
        .entrySet()
        .forEach(e -> call(e.getKey(), e.getValue(), propertiesForNameConsumer));
  }

  private void call(String repositoryId, V1Properties properties, BiConsumer<String, V1Properties> propertiesForNameConsumer) {
    propertiesForNameConsumer.accept(repositoryId, properties);
  }
}
