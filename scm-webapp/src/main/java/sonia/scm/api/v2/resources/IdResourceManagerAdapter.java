package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.PageResult;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Facade for {@link SingleResourceManagerAdapter} and {@link CollectionResourceManagerAdapter}.
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class IdResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation,
                             EXCEPTION extends Exception> {

  private final Manager<MODEL_OBJECT, EXCEPTION> manager;

  private final SingleResourceManagerAdapter<MODEL_OBJECT, DTO, EXCEPTION> singleAdapter;
  private final CollectionResourceManagerAdapter<MODEL_OBJECT, DTO, EXCEPTION> collectionAdapter;

  IdResourceManagerAdapter(Manager<MODEL_OBJECT, EXCEPTION> manager) {
    this.manager = manager;
    singleAdapter = new SingleResourceManagerAdapter<>(manager);
    collectionAdapter = new CollectionResourceManagerAdapter<>(manager);
  }

  Response get(String id, Function<MODEL_OBJECT, DTO> mapToDto) {
    return singleAdapter.get(() -> manager.get(id), mapToDto);
  }

  public Response update(String id, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges) {
    return singleAdapter.update(
      () -> manager.get(id),
      applyChanges,
      changed -> changed.getId().equals(id)
    );
  }

  public Response getAll(int page, int pageSize, String sortBy, boolean desc, Function<PageResult<MODEL_OBJECT>, CollectionDto> mapToDto) {
    return collectionAdapter.getAll(page, pageSize, sortBy, desc, mapToDto);
  }

  public Response create(DTO dto, Supplier<MODEL_OBJECT> modelObjectSupplier, Function<MODEL_OBJECT, String> uriCreator) throws IOException, EXCEPTION {
    return collectionAdapter.create(dto, modelObjectSupplier, uriCreator);
  }

  public Response delete(String id) {
    return singleAdapter.delete(id);
  }
}
