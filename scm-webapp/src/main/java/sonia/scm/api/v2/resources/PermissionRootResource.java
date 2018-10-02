package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Permission;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

import static sonia.scm.api.v2.resources.PermissionDto.GROUP_PREFIX;

@Slf4j
public class PermissionRootResource {


  private PermissionDtoToPermissionMapper dtoToModelMapper;
  private PermissionToPermissionDtoMapper modelToDtoMapper;
  private PermissionCollectionToDtoMapper permissionCollectionToDtoMapper;
  private ResourceLinks resourceLinks;
  private final RepositoryManager manager;


  @Inject
  public PermissionRootResource(PermissionDtoToPermissionMapper dtoToModelMapper, PermissionToPermissionDtoMapper modelToDtoMapper, PermissionCollectionToDtoMapper permissionCollectionToDtoMapper, ResourceLinks resourceLinks, RepositoryManager manager) {
    this.dtoToModelMapper = dtoToModelMapper;
    this.modelToDtoMapper = modelToDtoMapper;
    this.permissionCollectionToDtoMapper = permissionCollectionToDtoMapper;
    this.resourceLinks = resourceLinks;
    this.manager = manager;
  }


  /**
   * Adds a new permission to the user or group managed by the repository
   *
   * @param permission permission to add
   * @return a web response with the status code 201 and the url to GET the added permission
   */
  @POST
  @StatusCodes({
    @ResponseCode(code = 201, condition = "creates", additionalHeaders = {
      @ResponseHeader(name = "Location", description = "uri of the  created permission")
    }),
    @ResponseCode(code = 500, condition = "internal server error"),
    @ResponseCode(code = 404, condition = "not found"),
    @ResponseCode(code = 409, condition = "conflict")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes(VndMediaType.PERMISSION)
  @Path("")
  public Response create(@PathParam("namespace") String namespace, @PathParam("name") String name, PermissionDto permission) throws Exception {
    log.info("try to add new permission: {}", permission);
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionWrite(repository).check();
    checkPermissionAlreadyExists(permission, repository);
    repository.getPermissions().add(dtoToModelMapper.map(permission));
    manager.modify(repository);
    return Response.created(URI.create(resourceLinks.permission().self(namespace, name, permission.getName()))).build();
  }


  /**
   * Get the searched permission with permission name related to a repository
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @return the http response with a list of permissionDto objects
   * @throws NotFoundException if the repository does not exists
   */
  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "ok"),
    @ResponseCode(code = 404, condition = "not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.PERMISSION)
  @TypeHint(PermissionDto.class)
  @Path("{permission-name}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("permission-name") String permissionName) throws NotFoundException {
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionRead(repository).check();
    return Response.ok(
      repository.getPermissions()
        .stream()
        .filter(filterPermission(permissionName))
        .map(permission -> modelToDtoMapper.map(permission, repository))
        .findFirst()
        .orElseThrow(NotFoundException::new)
    ).build();
  }


  /**
   * Get all permissions related to a repository
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @return the http response with a list of permissionDto objects
   * @throws NotFoundException if the repository does not exists
   */
  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "ok"),
    @ResponseCode(code = 404, condition = "not found"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.PERMISSION)
  @TypeHint(PermissionDto.class)
  @Path("")
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) throws RepositoryNotFoundException {
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionRead(repository).check();
    return Response.ok(permissionCollectionToDtoMapper.map(repository)).build();
  }


  /**
   * Update a permission to the user or group managed by the repository
   * ignore the user input for groupPermission and take it from the path parameter (if the group prefix (@) exists it is a group permission)
   *
   * @param permission     permission to modify
   * @param permissionName permission to modify
   * @return a web response with the status code 204
   */
  @PUT
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Consumes(VndMediaType.PERMISSION)
  @Path("{permission-name}")
  public Response update(@PathParam("namespace") String namespace,
                         @PathParam("name") String name,
                         @PathParam("permission-name") String permissionName,
                         PermissionDto permission) throws NotFoundException, AlreadyExistsException {
    log.info("try to update the permission with name: {}. the modified permission is: {}", permissionName, permission);
    Repository repository = load(namespace, name);
    RepositoryPermissions.permissionWrite(repository).check();
    String extractedPermissionName = getPermissionName(permissionName);
    if (!isPermissionExist(new PermissionDto(extractedPermissionName, isGroupPermission(permissionName)), repository)) {
      throw new NotFoundException("permission", extractedPermissionName);
    }
    permission.setGroupPermission(isGroupPermission(permissionName));
    if (!extractedPermissionName.equals(permission.getName())) {
      checkPermissionAlreadyExists(permission, repository, "target permission " + permission.getName() + " already exists");
    }
    Permission existingPermission = repository.getPermissions()
      .stream()
      .filter(filterPermission(permissionName))
      .findFirst()
      .orElseThrow(NotFoundException::new);
    dtoToModelMapper.modify(existingPermission, permission);
    manager.modify(repository);
    log.info("the permission with name: {} is updated.", permissionName);
    return Response.noContent().build();
  }

  /**
   * Update a permission to the user or group managed by the repository
   *
   * @param permissionName permission to delete
   * @return a web response with the status code 204
   */
  @DELETE
  @StatusCodes({
    @ResponseCode(code = 204, condition = "delete success or nothing to delete"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Path("{permission-name}")
  public Response delete(@PathParam("namespace") String namespace,
                         @PathParam("name") String name,
                         @PathParam("permission-name") String permissionName) throws NotFoundException {
    log.info("try to delete the permission with name: {}.", permissionName);
    Repository repository = load(namespace, name);
    RepositoryPermissions.modify(repository).check();
    repository.getPermissions()
      .stream()
      .filter(filterPermission(permissionName))
      .findFirst()
      .ifPresent(p -> repository.getPermissions().remove(p))
    ;
    manager.modify(repository);
    log.info("the permission with name: {} is updated.", permissionName);
    return Response.noContent().build();
  }

  Predicate<Permission> filterPermission(String permissionName) {
    return permission -> getPermissionName(permissionName).equals(permission.getName())
      &&
      permission.isGroupPermission() == isGroupPermission(permissionName);
  }

  private String getPermissionName(String permissionName) {
    return Optional.of(permissionName)
      .filter(p -> !isGroupPermission(permissionName))
      .orElse(permissionName.substring(1));
  }

  private boolean isGroupPermission(String permissionName) {
    return permissionName.startsWith(GROUP_PREFIX);
  }


  /**
   * check if the actual user is permitted to manage the repository permissions
   * return the repository if the user is permitted
   *
   * @param namespace the repository namespace
   * @param name      the repository name
   * @return the repository if the user is permitted
   * @throws RepositoryNotFoundException if the repository does not exists
   */
  private Repository load(String namespace, String name) throws RepositoryNotFoundException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    return Optional.ofNullable(manager.get(namespaceAndName))
      .orElseThrow(() -> new RepositoryNotFoundException(namespaceAndName));
  }

  /**
   * check if the permission already exists in the repository
   *
   * @param permission the searched permission
   * @param repository the repository to be inspected
   * @param errorMessage error message
   * @throws AlreadyExistsException if the permission already exists in the repository
   */
  private void checkPermissionAlreadyExists(PermissionDto permission, Repository repository, String errorMessage) throws AlreadyExistsException {
    if (isPermissionExist(permission, repository)) {
      throw new AlreadyExistsException(errorMessage);
    }
  }

  private boolean isPermissionExist(PermissionDto permission, Repository repository) {
    return repository.getPermissions()
      .stream()
      .anyMatch(p -> p.getName().equals(permission.getName()) && p.isGroupPermission() == permission.isGroupPermission());
  }

  private void checkPermissionAlreadyExists(PermissionDto permission, Repository repository) throws AlreadyExistsException {
    checkPermissionAlreadyExists(permission, repository, "the permission " + permission.getName() + " already exist.");
  }
}


