package sonia.scm.api.v2.resources;

public class ScmMediaType {
  private static final String VERSION = "2";
  private static final String PREFIX = "application/vnd.scmm-";
  private static final String SUFFIX = "+json;v=" + VERSION;

  public static final String USER = PREFIX + "user" + SUFFIX;
}
