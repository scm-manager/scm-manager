package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import de.otto.edison.hal.HalRepresentation;
import sonia.scm.SCMContext;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.installer.HgInstallerFactory;
import sonia.scm.installer.HgPackage;
import sonia.scm.installer.HgPackageReader;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgVndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class HgConfigPackageResource {

  private final HgPackageReader pkgReader;
  private final AdvancedHttpClient client;
  private final HgRepositoryHandler handler;
  private final HgConfigPackageCollectionToDtoMapper configPackageCollectionToDtoMapper;

  @Inject
  public HgConfigPackageResource(HgPackageReader pkgReader, AdvancedHttpClient client, HgRepositoryHandler handler,
                                 HgConfigPackageCollectionToDtoMapper configPackageCollectionToDtoMapper) {
    this.pkgReader = pkgReader;
    this.client = client;
    this.handler = handler;
    this.configPackageCollectionToDtoMapper = configPackageCollectionToDtoMapper;
  }

  /**
   * Returns all mercurial packages.
   */
  @GET
  @Path("")
  @Produces(HgVndMediaType.PACKAGES)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:hg\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(HalRepresentation.class)
  public HalRepresentation getPackages() {

    ConfigurationPermissions.read(HgConfig.PERMISSION).check();

    return configPackageCollectionToDtoMapper.map(pkgReader.getPackages().getPackages());
  }

  /**
   * Installs a mercurial package
   *
   * @param id Identifier of the package to install
   */
  @PUT
  @Path("{pkgId}")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:write:hg\" privilege"),
    @ResponseCode(code = 404, condition = "no package found for id"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response installPackage(@PathParam("pkgId") String id) {
    Response response;

    ConfigurationPermissions.write(HgConfig.PERMISSION).check();

    HgPackage pkg = pkgReader.getPackage(id);

    if (pkg != null) {
      if (HgInstallerFactory.createInstaller().installPackage(client, handler,
                                                              SCMContext.getContext().getBaseDirectory(), pkg)) {
        response = Response.noContent().build();
      } else {
        response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } else {
      response = Response.status(Response.Status.NOT_FOUND).build();
    }

    return response;
  }
}
