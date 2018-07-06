package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.shiro.SecurityUtils;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.Role;
import sonia.scm.util.ScmConfigurationUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(GlobalConfigResource.GLOBAL_CONFIG_PATH_V2)
public class GlobalConfigResource {

  static final String GLOBAL_CONFIG_PATH_V2 = "v2/config/global";
  private final GlobalConfigDtoToScmConfigurationMapper dtoToConfigMapper;
  private final ScmConfigurationToGlobalConfigDtoMapper configToDtoMapper;
  private final ScmConfiguration configuration;

  @Inject
  public GlobalConfigResource(GlobalConfigDtoToScmConfigurationMapper dtoToConfigMapper, ScmConfigurationToGlobalConfigDtoMapper configToDtoMapper, ScmConfiguration configuration) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.configuration = configuration;
  }

  /**
   * Returns the global scm config.
   */
  @GET
  @Path("")
  @Produces(VndMediaType.GLOBAL_CONFIG)
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the global config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get() {
    Response response;

    // TODO ConfigPermisions?
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      response = Response.ok(configToDtoMapper.map(configuration)).build();
    } else {
      response = Response.status(Response.Status.FORBIDDEN).build();
    }

    return response;
  }

  /**
   * Modifies the global scm config.
   *
   * @param configDto new global scm configuration as DTO
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.GLOBAL_CONFIG)
  @StatusCodes({
    @ResponseCode(code = 201, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to update the global config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(GlobalConfigDto configDto, @Context UriInfo uriInfo) {
    Response response;

    // TODO ConfigPermisions?
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      ScmConfiguration config = dtoToConfigMapper.map(configDto);
      configuration.load(config);
      synchronized (ScmConfiguration.class) {
        ScmConfigurationUtil.getInstance().store(configuration);
      }
      response = Response.created(uriInfo.getRequestUri()).build();
    } else {
      response = Response.status(Response.Status.FORBIDDEN).build();
    }

    return response;
  }
}
