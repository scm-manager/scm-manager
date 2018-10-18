package sonia.scm;

import sonia.scm.util.AssertUtil;

public class ContextEntry {
  private final String type;
  private final String id;

  ContextEntry(Class type, String id) {
    this(type.getSimpleName(), id);
  }

  ContextEntry(String type, String id) {
      AssertUtil.assertIsNotEmpty(type);
      AssertUtil.assertIsNotEmpty(id);
      this.type = type;
      this.id = id;
    }

    public String getType () {
      return type;
    }

    public String getId () {
      return id;
    }
  }
