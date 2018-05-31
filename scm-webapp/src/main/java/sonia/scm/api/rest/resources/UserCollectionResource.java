package sonia.scm.api.rest.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class UserCollectionResource extends AbstractManagerResource<User, UserException> {
  private final UserDto2UserMapper dtoToUserMapper;
  private final User2UserDtoMapper userToDtoMapper;

  @Inject
  public UserCollectionResource(UserManager manager, UserDto2UserMapper dtoToUserMapper, User2UserDtoMapper userToDtoMapper) {
    super(manager);
    this.dtoToUserMapper = dtoToUserMapper;
    this.userToDtoMapper = userToDtoMapper;
  }

  /**
   * Returns all users. <strong>Note:</strong> This method requires admin privileges.
   *
   * @param request the current request
   * @param start   the start value for paging
   * @param limit   the limit value for paging
   * @param sortby  sort parameter
   * @param desc    sort direction desc or aesc
   * @return
   */
  @GET
  @Path("")
  @TypeHint(User[].class)
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response getAll(@Context Request request, @Context UriInfo uriInfo, @DefaultValue("0")
  @QueryParam("start") int start, @DefaultValue("-1")
  @QueryParam("limit") int limit, @QueryParam("sortby") String sortby,
    @DefaultValue("false")
    @QueryParam("desc") boolean desc) {
    Collection<User> items = fetchItems(sortby, desc, start, limit);
    List<UserDto> collect = items.stream().map(user -> userToDtoMapper.userToUserDto(user, uriInfo)).collect(Collectors.toList());
    return Response.ok(new GenericEntity<Collection<UserDto>>(collect) {}).build();
  }

  @POST
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 201, condition = "create success", additionalHeaders = {
      @ResponseHeader(name = "Location", description = "uri to the created group")
    }),
    @ResponseCode(code = 403, condition = "forbidden, the current user has no admin privileges"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response create(@Context UriInfo uriInfo, UserDto userDto) throws IOException, UserException {
    User user = dtoToUserMapper.userDtoToUser(userDto, "");
    manager.create(user);

    LinkBuilder builder = new LinkBuilder(uriInfo, UserNewResource.class, UserSubResource.class);
    return Response.created(builder.method("getUserSubResource").parameters(user.getName()).method("get").parameters().create().getHref()).build();
  }

  @Override
  protected GenericEntity<Collection<User>> createGenericEntity(Collection<User> items) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getId(User item) {
    return item.getName();
  }

  @Override
  protected String getPathPart() {
    throw new UnsupportedOperationException();
  }
}
