package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.api.rest.resources.AbstractManagerResource;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.function.Function;

public class ResourceManagerAdapter<T extends ModelObject, D extends HalRepresentation, E extends Exception> extends AbstractManagerResource<T, E> {

  public ResourceManagerAdapter(Manager<T, E> manager) {
    super(manager);
  }

  public Response get(String id, Function<T, D> mapper) {
    T entity = manager.get(id);
    if (entity == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    D dto = mapper.apply(entity);
    return Response.ok(dto).build();
  }

  public Response update(String id, D dto, Function<T, T> mapper) {
    T existingEntity = manager.get(id);
    T changedEntity = mapper.apply(existingEntity);
    return update(id, changedEntity);
  }

  @Override
  protected GenericEntity<Collection<T>> createGenericEntity(Collection<T> items) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected String getId(T item) {
    return item.getId();
  }

  @Override
  protected String getPathPart() {
    throw new UnsupportedOperationException();
  }
}
