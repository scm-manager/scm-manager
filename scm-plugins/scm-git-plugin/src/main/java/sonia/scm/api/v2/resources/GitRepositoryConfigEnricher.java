package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.AbstractRepositoryJsonEnricher;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
public class GitRepositoryConfigEnricher extends AbstractRepositoryJsonEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final RepositoryManager manager;

  @Inject
  public GitRepositoryConfigEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper, RepositoryManager manager) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
    this.manager = manager;
  }

  @Override
  protected void enrichRepositoryNode(JsonNode repositoryNode, String namespace, String name) {
    if (GitRepositoryHandler.TYPE_NAME.equals(manager.get(new NamespaceAndName(namespace, name)).getType())) {
      String repositoryConfigLink = new LinkBuilder(scmPathInfoStore.get().get(), GitConfigResource.class)
        .method("getRepositoryConfig")
        .parameters(namespace, name)
        .href();
      addLink(repositoryNode, "configuration", repositoryConfigLink);
    }
  }
}
