package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.api.rest.resources.AbstractManagerResource;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Adapter from resource http endpoints to managers, for Single resources (e.g. {@code /user/name}).
 *
 * Provides common CRUD operations and DTO to Model Object mapping to keep Resources more DRY.
 *
 * @param <MODEL_OBJECT> The type of the model object, eg. {@link sonia.scm.user.User}.
 * @param <DTO> The corresponding transport object, eg. {@link UserDto}.
 * @param <EXCEPTION> The exception type for the model object, eg. {@link sonia.scm.user.UserException}.
 *
 * @see CollectionResourceManagerAdapter
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class SingleResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation,
                             EXCEPTION extends Exception> extends AbstractManagerResource<MODEL_OBJECT, EXCEPTION> {

  SingleResourceManagerAdapter(Manager<MODEL_OBJECT, EXCEPTION> manager, Class<MODEL_OBJECT> type) {
    super(manager, type);
  }

  /**
   * Reads the model object for the given id, transforms it to a dto and returns a corresponding http response.
   * This handles all corner cases, eg. no matching object for the id or missing privileges.
   */
  Response get(Supplier<Optional<MODEL_OBJECT>> reader, Function<MODEL_OBJECT, DTO> mapToDto) {
    return reader.get()
      .map(mapToDto)
      .map(Response::ok)
      .map(Response.ResponseBuilder::build)
      .orElse(Response.status(Response.Status.NOT_FOUND).build());
  }

  /**
   * Update the model object for the given id according to the given function and returns a corresponding http response.
   * This handles all corner cases, eg. no matching object for the id or missing privileges.
   */
  public Response update(Supplier<Optional<MODEL_OBJECT>> reader, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges, Predicate<MODEL_OBJECT> hasSameKey) {
    Optional<MODEL_OBJECT> existingModelObject = reader.get();
    if (!existingModelObject.isPresent()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    MODEL_OBJECT changedModelObject = applyChanges.apply(existingModelObject.get());
    if (!hasSameKey.test(changedModelObject)) {
      return Response.status(BAD_REQUEST).entity("illegal change of id").build();
    }
    return update(getId(existingModelObject.get()), changedModelObject);
  }

  public Response delete(Supplier<Optional<MODEL_OBJECT>> reader) {
    return reader.get()
      .map(MODEL_OBJECT::getId)
      .map(this::delete)
      .orElse(null);
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
