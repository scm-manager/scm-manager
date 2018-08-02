package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.ScmConfigurationUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path(ConfigResource.CONFIG_PATH_V2)
public class ConfigResource {

  static final String CONFIG_PATH_V2 = "v2/config";
  private final ConfigDtoToScmConfigurationMapper dtoToConfigMapper;
  private final ScmConfigurationToConfigDtoMapper configToDtoMapper;
  private final ScmConfiguration configuration;

  @Inject
  public ConfigResource(ConfigDtoToScmConfigurationMapper dtoToConfigMapper, ScmConfigurationToConfigDtoMapper configToDtoMapper, ScmConfiguration configuration) {
    this.dtoToConfigMapper = dtoToConfigMapper;
    this.configToDtoMapper = configToDtoMapper;
    this.configuration = configuration;
  }

  /**
   * Returns the global scm config.
   */
  @GET
  @Path("")
  @Produces(VndMediaType.CONFIG)
  @TypeHint(UserDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the global config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get() {

    // We do this permission check in Resource and not in ScmConfiguration, because it must be available for reading
    // from within the code (plugins, etc.), but not for the whole anonymous world outside.
    ConfigurationPermissions.read(configuration).check();

    return Response.ok(configToDtoMapper.map(configuration)).build();
  }

  /**
   * Modifies the global scm config.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.CONFIG)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to update the global config"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(ConfigDto configDto) {

    // This *could* be moved to ScmConfiguration or ScmConfigurationUtil classes.
    // But to where to check? load() or store()? Leave it for now, SCMv1 legacy that can be cleaned up later.
    ConfigurationPermissions.write(configuration).check();

    ScmConfiguration config = dtoToConfigMapper.map(configDto);
    synchronized (ScmConfiguration.class) {
      configuration.load(config);
      ScmConfigurationUtil.getInstance().store(configuration);
    }

    return Response.noContent().build();
  }
}
