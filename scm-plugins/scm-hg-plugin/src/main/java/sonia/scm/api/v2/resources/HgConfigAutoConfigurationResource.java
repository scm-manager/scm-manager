package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public class HgConfigAutoConfigurationResource {

  private final HgRepositoryHandler repositoryHandler;

  @Inject
  public HgConfigAutoConfigurationResource(HgRepositoryHandler repositoryHandler) {
    this.repositoryHandler = repositoryHandler;
  }

  /**
   * Sets the default hg config and installs the hg binary.
   */
  @PUT
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:write:hg\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response autoConfiguration() {
    return autoConfiguration(null);
  }

  /**
   * Modifies the hg config and installs the hg binary.
   *
   * @param configDto new configuration object
   */
  @PUT
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:write:hg\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response autoConfiguration(HgConfigDto configDto) {
    HgConfig config = repositoryHandler.getConfig();

    if (config == null) {
      config = new HgConfig();
      repositoryHandler.setConfig(config);
    }

    ConfigurationPermissions.write(config).check();

    repositoryHandler.doAutoConfiguration(config);

    return Response.noContent().build();
  }
}
