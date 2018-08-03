package sonia.scm.it;

import sonia.scm.util.IOUtil;

import java.util.ArrayList;
import java.util.Collection;

class ScmParameterizedIntegrationTestUtil {
  static Collection<String> createParameters() {
    Collection<String> params = new ArrayList<>();

    params.add("git");
    params.add("svn");

    if (IOUtil.search("hg") != null) {
      params.add("hg");
    }

    return params;
  }
}
