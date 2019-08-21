package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.NotFoundException;

import javax.ws.rs.core.Response;
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
                             DTO extends HalRepresentation> {

  protected final Manager<MODEL_OBJECT> manager;
  protected final Class<MODEL_OBJECT> type;

  SingleResourceManagerAdapter(Manager<MODEL_OBJECT> manager, Class<MODEL_OBJECT> type) {
    this.manager = manager;
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
    return update(changedModelObject);
  }

  private Response update(MODEL_OBJECT item) {
    manager.modify(item);
    return Response.noContent().build();
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

  public Response delete(String name) {
    MODEL_OBJECT item = manager.get(name);

    if (item != null) {
      manager.delete(item);
      return Response.noContent().build();
    } else {
      return Response.noContent().build();
    }
  }

  protected String getId(MODEL_OBJECT item) {
    return item.getId();
  }
}
