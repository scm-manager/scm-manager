package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.NotFoundException;
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
 *
 * @see CollectionResourceManagerAdapter
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class SingleResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation> extends AbstractManagerResource<MODEL_OBJECT> {

  private final Function<Throwable, Optional<Response>> errorHandler;
  private final Class<MODEL_OBJECT> type;

  SingleResourceManagerAdapter(Manager<MODEL_OBJECT> manager, Class<MODEL_OBJECT> type) {
    this(manager, type, e -> Optional.empty());
  }

  SingleResourceManagerAdapter(
    Manager<MODEL_OBJECT> manager,
    Class<MODEL_OBJECT> type,
    Function<Throwable, Optional<Response>> errorHandler) {
    super(manager, type);
    this.errorHandler = errorHandler;
    this.type = type;
  }

  /**
   * Reads the model object for the given id, transforms it to a dto and returns a corresponding http response.
   * This handles all corner cases, eg. no matching object for the id or missing privileges.
   */
  Response get(Supplier<MODEL_OBJECT> reader, Function<MODEL_OBJECT, DTO> mapToDto) {
    return Response.ok(mapToDto.apply(reader.get())).build();
  }

  /**
   * Updates the model object provided by the reader according to the given function and returns a corresponding http
   * response. This handles all corner cases, eg. no matching object for the id or missing privileges.
   */
  Response update(Supplier<MODEL_OBJECT> reader, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges, Predicate<MODEL_OBJECT> hasSameKey) {
    return update(reader, applyChanges, hasSameKey, ModelObject::getId);
  }

  Response update(Supplier<MODEL_OBJECT> reader, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges, Predicate<MODEL_OBJECT> hasSameKey, Function<MODEL_OBJECT, String> keyExtractor) {
    MODEL_OBJECT existingModelObject = reader.get();
    MODEL_OBJECT changedModelObject = applyChanges.apply(existingModelObject);
    if (!hasSameKey.test(changedModelObject)) {
      return Response.status(BAD_REQUEST).entity("illegal change of id").build();
    }
    else if (modelObjectWasModifiedConcurrently(existingModelObject, changedModelObject)) {
      throw new ConcurrentModificationException(type, keyExtractor.apply(existingModelObject));
    }
    return update(getId(existingModelObject), changedModelObject);
  }

  private boolean modelObjectWasModifiedConcurrently(MODEL_OBJECT existing, MODEL_OBJECT updated) {
    return existing.getLastModified() != null
      && (updated.getLastModified() == null || existing.getLastModified() > updated.getLastModified());
  }

  public Response delete(Supplier<MODEL_OBJECT> reader) {
    try {
      return delete(reader.get().getId());
    } catch (NotFoundException e) {
      // due to idempotency of delete this does not matter here.
      return null;
    }
  }

  @Override
  protected Response createErrorResponse(Throwable throwable) {
    return errorHandler.apply(throwable).orElse(super.createErrorResponse(throwable));
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
