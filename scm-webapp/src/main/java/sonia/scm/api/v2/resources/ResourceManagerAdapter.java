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

class ResourceManagerAdapter<T extends ModelObject, D extends HalRepresentation, E extends Exception> extends AbstractManagerResource<T, E> {

  ResourceManagerAdapter(Manager<T, E> manager) {
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

  public Response update(String id, Function<T, T> mapper) {
    T existingEntity = manager.get(id);
    T changedEntity = mapper.apply(existingEntity);
    return update(id, changedEntity);
  }

  public Response getAll(int page, int pageSize, String sortBy, boolean desc, Function<PageResult<T>, CollectionDto> mappger) {
    PageResult<T> pageResult = fetchPage(sortBy, desc, page, pageSize);
    return Response.ok(mappger.apply(pageResult)).build();
  }

  public Response create(D dto, Supplier<T> entitySupplyer, Function<T, String> uriCreator) throws IOException, E {
    if (dto == null) {
      return Response.status(400).build();
    }
    T entity = entitySupplyer.get();
    manager.create(entity);
    return Response.created(URI.create(uriCreator.apply(entity))).build();
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
