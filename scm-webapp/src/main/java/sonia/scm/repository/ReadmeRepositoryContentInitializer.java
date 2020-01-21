package sonia.scm.repository;

import com.google.common.base.Strings;
import sonia.scm.Priority;
import sonia.scm.plugin.Extension;

import java.io.IOException;

@Extension
@Priority(1) // should always be the first, so that plugins can overwrite the readme.md
public class ReadmeRepositoryContentInitializer implements RepositoryContentInitializer {
  @Override
  public void initialize(InitializerContext context) throws IOException {
    Repository repository = context.getRepository();

    String content = "# " + repository.getName();
    String description = repository.getDescription();
    if (!Strings.isNullOrEmpty(description)) {
      content += "\n\n" + description;
    }
    context.create("README.md").from(content);
  }
}
