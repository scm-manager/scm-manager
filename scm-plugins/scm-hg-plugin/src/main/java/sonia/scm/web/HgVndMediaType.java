package sonia.scm.web;


public class HgVndMediaType {
  private static final String PREFIX = VndMediaType.PREFIX + "hgConfig";

  public static final String CONFIG = PREFIX + VndMediaType.SUFFIX;
  public static final String PACKAGES = PREFIX + "-packages" + VndMediaType.SUFFIX;
  public static final String INSTALLATIONS = PREFIX + "-installation" + VndMediaType.SUFFIX;

  private HgVndMediaType() {
  }
}
