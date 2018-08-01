package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.web.GitVndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * RESTful Web Service Resource to manage the configuration of the git plugin.
 */
@Path(GitConfigResource.GIT_CONFIG_PATH_V2)
public class GitConfigResource {

  static final String GIT_CONFIG_PATH_V2 = "v2/config/git";
  private final GitConfigDtoToGitConfigMapper dtoToConfigMapper;
  private final GitConfigToGitConfigDtoMapper configToDtoMapper;
  private final GitRepositoryHandler repositoryHandler;

  @Inject
  public GitConfigResource(GitConfigDtoToGitConfigMapper dtoToConfigMapper, GitConfigToGitConfigDtoMapper configToDtoMapper, GitRepositoryHandler repositoryHandler) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.repositoryHandler = repositoryHandler;
  }

  /**
   * Returns the git config.
   */
  @GET
  @Path("")
  @Produces(GitVndMediaType.GIT_CONFIG)
  @TypeHint(GitConfigDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the git config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get() {

    GitConfig config = repositoryHandler.getConfig();

    if (config == null) {
      config = new GitConfig();
      repositoryHandler.setConfig(config);
    }

    ConfigurationPermissions.read(config).check();

    return Response.ok(configToDtoMapper.map(config)).build();
  }

  /**
   * Modifies the git config.
   *
   * @param configDto new git configuration as DTO
   */
  @PUT
  @Path("")
  @Consumes(GitVndMediaType.GIT_CONFIG)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to update the git config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(@Context UriInfo uriInfo, GitConfigDto configDto) {

    GitConfig config = dtoToConfigMapper.map(configDto);

    ConfigurationPermissions.write(config).check();

    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }
}
