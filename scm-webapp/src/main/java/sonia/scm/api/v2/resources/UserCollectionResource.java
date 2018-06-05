package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.api.rest.resources.AbstractManagerResource;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;
import static sonia.scm.api.v2.resources.ScmMediaType.USER;

@Singleton
@Produces(USER)
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
  public Response getAll(@Context Request request, @Context UriInfo uriInfo, @DefaultValue("0")
  @QueryParam("page") int page, @DefaultValue("10")
  @QueryParam("pageSize") int pageSize, @QueryParam("sortby") String sortby,
    @DefaultValue("false")
    @QueryParam("desc") boolean desc) {
    Collection<User> items = fetchItems(sortby, desc, page * pageSize, pageSize);

    LinkBuilder collectionLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserCollectionResource.class);
    String baseUrl = collectionLinkBuilder.method("getUserCollectionResource").parameters().method("create").parameters().href();

    List<UserDto> dtos = items.stream().map(user -> userToDtoMapper.userToUserDto(user, uriInfo)).collect(Collectors.toList());

    return Response.ok(new UserCollectionDto(baseUrl, zeroBasedNumberedPaging(page, pageSize, true), dtos)).build();
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
  public Response create(@Context UriInfo uriInfo, UserDto userDto) throws IOException, UserException {
    User user = dtoToUserMapper.userDtoToUser(userDto, "");
    manager.create(user);

    LinkBuilder builder = new LinkBuilder(uriInfo, UserV2Resource.class, UserSubResource.class);
    return Response.created(builder.method("getUserSubResource").parameters(user.getName()).method("get").parameters().create()).build();
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
