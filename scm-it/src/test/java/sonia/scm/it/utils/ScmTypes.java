package sonia.scm.it.utils;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import sonia.scm.util.IOUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class ScmTypes implements ArgumentsProvider {
  public static Collection<String> availableScmTypes() {
    Collection<String> params = new ArrayList<>();

    params.add("git");
    params.add("svn");

    if (IOUtil.search("hg") != null) {
      params.add("hg");
    }

    return params;
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return availableScmTypes().stream().map(Arguments::of);
  }
}
