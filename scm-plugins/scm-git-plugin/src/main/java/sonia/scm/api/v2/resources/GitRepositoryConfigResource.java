package sonia.scm.api.v2.resources;

import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.web.GitVndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitRepositoryConfigResource {

  private final GitRepositoryConfigToGitRepositoryConfigDtoMapper repositoryConfigToDtoMapper;
  private final RepositoryManager repositoryManager;
  private final ConfigurationStoreFactory configurationStoreFactory;

  @Inject
  public GitRepositoryConfigResource(GitRepositoryConfigToGitRepositoryConfigDtoMapper repositoryConfigToDtoMapper, RepositoryManager repositoryManager, ConfigurationStoreFactory configurationStoreFactory) {
    this.repositoryConfigToDtoMapper = repositoryConfigToDtoMapper;
    this.repositoryManager = repositoryManager;
    this.configurationStoreFactory = configurationStoreFactory;
  }

  @GET
  @Path("/")
  @Produces(GitVndMediaType.GIT_REPOSITORY_CONFIG)
  public Response getRepositoryConfig(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      throw notFound(entity(namespaceAndName));
    }

    ConfigurationStore<GitRepositoryConfig> repositoryConfigStore = configurationStoreFactory.withType(GitRepositoryConfig.class).withName("gitConfig").forRepository(repository).build();
    GitRepositoryConfig config = repositoryConfigStore.get();
    if (config == null) {
      config = new GitRepositoryConfig();
    }
    GitRepositoryConfigDto dto = repositoryConfigToDtoMapper.map(config, repository);
    return Response.ok(dto).build();
  }
}
