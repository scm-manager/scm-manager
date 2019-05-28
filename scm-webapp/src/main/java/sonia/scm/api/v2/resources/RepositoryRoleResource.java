package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class RepositoryRoleResource {

  private final RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper;
  private final RepositoryRoleToRepositoryRoleDtoMapper repositoryRoleToDtoMapper;

  private final IdResourceManagerAdapter<RepositoryRole, RepositoryRoleDto> adapter;

  @Inject
  public RepositoryRoleResource(
    RepositoryRoleDtoToRepositoryRoleMapper dtoToRepositoryRoleMapper,
    RepositoryRoleToRepositoryRoleDtoMapper repositoryRoleToDtoMapper,
    RepositoryRoleManager manager) {
    this.dtoToRepositoryRoleMapper = dtoToRepositoryRoleMapper;
    this.repositoryRoleToDtoMapper = repositoryRoleToDtoMapper;
    this.adapter = new IdResourceManagerAdapter<>(manager, RepositoryRole.class);
  }

  /**
   * Returns a repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param name the id/name of the repository role
   */
  @GET
  @Path("")
  @Produces(VndMediaType.REPOSITORY_ROLE)
  @TypeHint(RepositoryRoleDto.class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the repository role"),
    @ResponseCode(code = 404, condition = "not found, no repository role with the specified name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public Response get(@PathParam("name") String name) {
    return adapter.get(name, repositoryRoleToDtoMapper::map);
  }

  /**
   * Deletes a repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param name the name of the repository role to delete.
   */
  @DELETE
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success or nothing to delete"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repositoryRole\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response delete(@PathParam("name") String name) {
    return adapter.delete(name);
  }

  /**
   * Modifies the given repository role.
   *
   * <strong>Note:</strong> This method requires "repositoryRole" privilege.
   *
   * @param name    name of the repository role to be modified
   * @param repositoryRole repository role object to modify
   */
  @PUT
  @Path("")
  @Consumes(VndMediaType.REPOSITORY_ROLE)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 400, condition = "Invalid body, e.g. illegal change of repository role name"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"repositoryRole\" privilege"),
    @ResponseCode(code = 404, condition = "not found, no repository role with the specified name available"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(@PathParam("name") String name, @Valid RepositoryRoleDto repositoryRole) {
    return adapter.update(name, existing -> dtoToRepositoryRoleMapper.map(repositoryRole));
  }
}
