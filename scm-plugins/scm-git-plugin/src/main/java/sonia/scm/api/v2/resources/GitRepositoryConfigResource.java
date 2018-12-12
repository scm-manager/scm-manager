package sonia.scm.api.v2.resources;

import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.web.GitVndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitRepositoryConfigResource {

  private final GitRepositoryConfigMapper repositoryConfigMapper;
  private final RepositoryManager repositoryManager;
  private final ConfigurationStoreFactory configurationStoreFactory;

  @Inject
  public GitRepositoryConfigResource(GitRepositoryConfigMapper repositoryConfigMapper, RepositoryManager repositoryManager, ConfigurationStoreFactory configurationStoreFactory) {
    this.repositoryConfigMapper = repositoryConfigMapper;
    this.repositoryManager = repositoryManager;
    this.configurationStoreFactory = configurationStoreFactory;
  }

  @GET
  @Path("/")
  @Produces(GitVndMediaType.GIT_REPOSITORY_CONFIG)
  public Response getRepositoryConfig(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = getRepository(namespace, name);
    ConfigurationStore<GitRepositoryConfig> repositoryConfigStore = getStore(repository);
    GitRepositoryConfig config = repositoryConfigStore.get();
    if (config == null) {
      config = new GitRepositoryConfig();
    }
    GitRepositoryConfigDto dto = repositoryConfigMapper.map(config, repository);
    return Response.ok(dto).build();
  }

  @PUT
  @Path("/")
  @Consumes(GitVndMediaType.GIT_REPOSITORY_CONFIG)
  public Response setRepositoryConfig(@PathParam("namespace") String namespace, @PathParam("name") String name, GitRepositoryConfigDto dto) {
    Repository repository = getRepository(namespace, name);
    ConfigurationStore<GitRepositoryConfig> repositoryConfigStore = getStore(repository);
    GitRepositoryConfig config = repositoryConfigMapper.map(dto);
    repositoryConfigStore.set(config);
    return Response.noContent().build();
  }

  private Repository getRepository(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      throw notFound(entity(namespaceAndName));
    }
    return repository;
  }

  private ConfigurationStore<GitRepositoryConfig> getStore(Repository repository) {
    return configurationStoreFactory.withType(GitRepositoryConfig.class).withName("gitConfig").forRepository(repository).build();
  }
}
