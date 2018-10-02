package sonia.scm.it.utils;

import sonia.scm.util.IOUtil;

import java.util.ArrayList;
import java.util.Collection;

public class ScmTypes {
  public static Collection<String> availableScmTypes() {
    Collection<String> params = new ArrayList<>();

    params.add("git");
    params.add("svn");

    if (IOUtil.search("hg") != null) {
      params.add("hg");
    }

    return params;
  }
}
