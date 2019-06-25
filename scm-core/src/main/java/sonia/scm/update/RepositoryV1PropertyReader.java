package sonia.scm.update;

import java.util.Map;

public class RepositoryV1PropertyReader implements V1PropertyReader {
  @Override
  public String getStoreName() {
    return "repository-properties-v1";
  }

  @Override
  public Instance createInstance(Map<String, V1Properties> all) {
    return new MapBasedPropertyReaderInstance(all);
  }

}
