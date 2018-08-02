package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.repository.HgConfig;
import sonia.scm.web.HgVndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class HgConfigInstallationsResource {

  private final HgConfigInstallationsToDtoMapper hgConfigInstallationsToDtoMapper;

  @Inject
  public HgConfigInstallationsResource(HgConfigInstallationsToDtoMapper hgConfigInstallationsToDtoMapper) {
    this.hgConfigInstallationsToDtoMapper = hgConfigInstallationsToDtoMapper;
  }

  /**
   * Returns the hg installations.
   */
  @GET
  @Path("hg")
  @Produces(HgVndMediaType.INSTALLATIONS)
  @TypeHint(HalRepresentation.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:hg\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public HalRepresentation getHgInstallations() {

    ConfigurationPermissions.read(HgConfig.PERMISSION).check();

    return hgConfigInstallationsToDtoMapper.map(HgInstallerFactory.createInstaller().getHgInstallations());
  }

  /**
   * Returns the python installations.
   */
  @GET
  @Path("python")
  @Produces(HgVndMediaType.INSTALLATIONS)
  @TypeHint(HalRepresentation.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:hg\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public HalRepresentation getPythonInstallations() {

    ConfigurationPermissions.read(HgConfig.PERMISSION).check();

    return hgConfigInstallationsToDtoMapper.map(HgInstallerFactory.createInstaller().getPythonInstallations());
  }
}
