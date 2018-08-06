package sonia.scm.it;

import sonia.scm.util.IOUtil;

import java.util.ArrayList;
import java.util.Collection;

class ScmTypes {
  static Collection<String> availableScmTypes() {
    Collection<String> params = new ArrayList<>();

    params.add("git");
    params.add("svn");

    if (IOUtil.search("hg") != null) {
      params.add("hg");
    }

    return params;
  }
}
