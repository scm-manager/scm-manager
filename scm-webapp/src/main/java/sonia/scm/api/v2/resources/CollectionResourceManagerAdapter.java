package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.PageResult;
import sonia.scm.api.rest.resources.AbstractManagerResource;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Adapter from resource http endpoints to managers.
 *
 * Provides common CRUD operations and DTO to Model Object mapping to keep Resources more DRY.
 *
 * @param <MODEL_OBJECT> The type of the model object, eg. {@link sonia.scm.user.User}.
 * @param <DTO> The corresponding transport object, eg. {@link UserDto}.
 * @param <EXCEPTION> The exception type for the model object, eg. {@link sonia.scm.user.UserException}.
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class CollectionResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation,
                             EXCEPTION extends Exception> extends AbstractManagerResource<MODEL_OBJECT, EXCEPTION> {

  CollectionResourceManagerAdapter(Manager<MODEL_OBJECT, EXCEPTION> manager, Class<MODEL_OBJECT> type) {
    super(manager, type);
  }

  /**
   * Reads all model objects in a paged way, maps them using the given function and returns a corresponding http response.
   * This handles all corner cases, eg. missing privileges.
   */
  public Response getAll(int page, int pageSize, String sortBy, boolean desc, Function<PageResult<MODEL_OBJECT>, CollectionDto> mapToDto) {
    PageResult<MODEL_OBJECT> pageResult = fetchPage(sortBy, desc, page, pageSize);
    return Response.ok(mapToDto.apply(pageResult)).build();
  }

  /**
   * Creates a model object for the given dto and returns a corresponding http response.
   * This handles all corner cases, eg. no conflicts or missing privileges.
   */
  public Response create(DTO dto, Supplier<MODEL_OBJECT> modelObjectSupplier, Function<MODEL_OBJECT, String> uriCreator) throws EXCEPTION {
    if (dto == null) {
      return Response.status(BAD_REQUEST).build();
    }
    MODEL_OBJECT modelObject = modelObjectSupplier.get();
    MODEL_OBJECT created = manager.create(modelObject);
    return Response.created(URI.create(uriCreator.apply(created))).build();
  }

  @Override
  protected GenericEntity<Collection<MODEL_OBJECT>> createGenericEntity(Collection<MODEL_OBJECT> modelObjects) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getId(MODEL_OBJECT item) {
    return item.getId();
  }

  @Override
  protected String getPathPart() {
    throw new UnsupportedOperationException();
  }
}
