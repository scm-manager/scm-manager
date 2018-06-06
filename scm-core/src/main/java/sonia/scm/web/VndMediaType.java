package sonia.scm.web;

import javax.ws.rs.core.MediaType;

public class VndMediaType {
  private static final String VERSION = "2";
  private static final String TYPE = "application";
  private static final String SUBTYPE_PREFIX = "vnd.scmm-";
  private static final String PREFIX = TYPE + "/" + SUBTYPE_PREFIX;
  private static final String SUFFIX = "+json;v=" + VERSION;

  public static final String USER = PREFIX + "user" + SUFFIX;

  private VndMediaType() {
  }

  public static MediaType jsonType(String resource) {
    return MediaType.valueOf(json(resource));
  }

  public static String json(String resource) {
    return PREFIX + resource + SUFFIX;// ".v2+json";
  }

  public static boolean isVndType(MediaType type) {
    return type.getType().equals(TYPE) && type.getSubtype().startsWith(SUBTYPE_PREFIX);
  }
}
