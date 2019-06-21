package sonia.scm.update;

import java.util.Map;

public class GroupV1PropertyReader implements V1PropertyReader {
  @Override
  public String getStoreName() {
    return "group-properties-v1";
  }

  @Override
  public Instance createInstance(Map<String, V1Properties> all) {
    return new MapBasedPropertyReaderInstance(all);
  }
}
