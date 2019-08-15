package sonia.scm.repository;

import org.eclipse.jgit.lib.StoredConfig;

import java.io.IOException;

public class GitConfigHelper {

  private static final String CONFIG_SECTION_SCMM = "scmm";
  private static final String CONFIG_KEY_REPOSITORY_ID = "repositoryid";

  public void createScmmConfig(Repository repository, org.eclipse.jgit.lib.Repository gitRepository) throws IOException {
    StoredConfig config = gitRepository.getConfig();
    config.setString(CONFIG_SECTION_SCMM, null, CONFIG_KEY_REPOSITORY_ID, repository.getId());
    config.save();
  }

  public String getRepositoryId(StoredConfig gitConfig) {
    return gitConfig.getString(CONFIG_SECTION_SCMM, null, CONFIG_KEY_REPOSITORY_ID);
  }
}
