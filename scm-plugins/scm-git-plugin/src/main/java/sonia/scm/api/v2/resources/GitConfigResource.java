package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.security.Role;
import sonia.scm.web.GitVndMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(GitConfigResource.GIT_CONFIG_PATH_V2)
public class GitConfigResource {

  static final String GIT_CONFIG_PATH_V2 = "v2/config/repository/git";
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
    Response response;

    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      GitConfig config = repositoryHandler.getConfig();

      if (config == null) {
        config = new GitConfig();
        repositoryHandler.setConfig(config);
      }

      response = Response.ok(configToDtoMapper.map(config)).build();
    } else {
      response = Response.status(Response.Status.FORBIDDEN).build();
    }

    return response;
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
    @ResponseCode(code = 201, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to update the git config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(@Context UriInfo uriInfo, GitConfigDto configDto) {
    Response response;

    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      repositoryHandler.setConfig(dtoToConfigMapper.map(configDto));
      repositoryHandler.storeConfig();
      response = Response.created(uriInfo.getRequestUri()).build();
    } else {
      response = Response.status(Response.Status.FORBIDDEN).build();
    }

    return response;
  }
}
