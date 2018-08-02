package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.SvnConfig;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.web.SvnVndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * RESTful Web Service Resource to manage the configuration of the svn plugin.
 */
@Path(SvnConfigResource.SVN_CONFIG_PATH_V2)
public class SvnConfigResource {

  static final String SVN_CONFIG_PATH_V2 = "v2/config/svn";
  private final SvnConfigDtoToSvnConfigMapper dtoToConfigMapper;
  private final SvnConfigToSvnConfigDtoMapper configToDtoMapper;
  private final SvnRepositoryHandler repositoryHandler;

  @Inject
  public SvnConfigResource(SvnConfigDtoToSvnConfigMapper dtoToConfigMapper, SvnConfigToSvnConfigDtoMapper configToDtoMapper,
                           SvnRepositoryHandler repositoryHandler) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.repositoryHandler = repositoryHandler;
  }

  /**
   * Returns the svn config.
   */
  @GET
  @Path("")
  @Produces(SvnVndMediaType.SVN_CONFIG)
  @TypeHint(SvnConfigDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the git config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get() {

    SvnConfig config = repositoryHandler.getConfig();

    if (config == null) {
      config = new SvnConfig();
      repositoryHandler.setConfig(config);
    }

    ConfigurationPermissions.read(config).check();

    return Response.ok(configToDtoMapper.map(config)).build();
  }

  /**
   * Modifies the svn config.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @Consumes(SvnVndMediaType.SVN_CONFIG)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to update the git config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(SvnConfigDto configDto) {

    SvnConfig config = dtoToConfigMapper.map(configDto);

    ConfigurationPermissions.write(config).check();

    repositoryHandler.setConfig(config);
    repositoryHandler.storeConfig();

    return Response.noContent().build();
  }

}
