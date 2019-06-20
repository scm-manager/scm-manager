package sonia.scm.update;

import java.util.Map;
import java.util.function.BiConsumer;

public interface V1PropertyReader {

  String getStoreName();

  Instance createInstance(Map<String, V1Properties> all);

  interface Instance {
    void forEachEntry(BiConsumer<String, V1Properties> propertiesForNameConsumer);
  }
}
