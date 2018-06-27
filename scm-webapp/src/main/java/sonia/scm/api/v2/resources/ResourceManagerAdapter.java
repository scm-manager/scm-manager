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

/**
 * Adapter from resource http endpoints to managers.
 * @param <MODEL_OBJECT> The type of the model object, eg. {@link sonia.scm.user.User}.
 * @param <DTO> The corresponding transport object, eg. {@link UserDto}.
 * @param <EXCEPTION> The exception type for the model object, eg. {@link sonia.scm.user.UserException}.
 */
class ResourceManagerAdapter<MODEL_OBJECT extends ModelObject, DTO extends HalRepresentation, EXCEPTION extends Exception> extends AbstractManagerResource<MODEL_OBJECT, EXCEPTION> {

  ResourceManagerAdapter(Manager<MODEL_OBJECT, EXCEPTION> manager) {
    super(manager);
  }

  /**
   * Reads the model object for the given id, transforms it to a dto and returns a corresponding http response.
   * This handles all corner cases, eg. no matching object for the id or missing privileges.
   */
  Response get(String id, Function<MODEL_OBJECT, DTO> mapToDto) {
    MODEL_OBJECT modelObject = manager.get(id);
    if (modelObject == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    DTO dto = mapToDto.apply(modelObject);
    return Response.ok(dto).build();
  }

  /**
   * Update the model object for the given id according to the given function and returns a corresponding http response.
   * This handles all corner cases, eg. no matching object for the id or missing privileges.
   */
  public Response update(String id, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges) {
    MODEL_OBJECT existingModelObject = manager.get(id);
    if (existingModelObject == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    MODEL_OBJECT changedModelObject = applyChanges.apply(existingModelObject);
    return update(id, changedModelObject);
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
  public Response create(DTO dto, Supplier<MODEL_OBJECT> modelObjectSupplier, Function<MODEL_OBJECT, String> uriCreator) throws IOException, EXCEPTION {
    if (dto == null) {
      return Response.status(400).build();
    }
    MODEL_OBJECT modelObject = modelObjectSupplier.get();
    manager.create(modelObject);
    return Response.created(URI.create(uriCreator.apply(modelObject))).build();
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
