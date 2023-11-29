/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import jakarta.ws.rs.core.Response;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.NotFoundException;
import sonia.scm.PageResult;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Facade for {@link SingleResourceManagerAdapter} and {@link CollectionResourceManagerAdapter}
 * for model objects handled by a single id.
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class IdResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation> {

  private final Manager<MODEL_OBJECT> manager;
  private final String type;

  private final SingleResourceManagerAdapter<MODEL_OBJECT, DTO> singleAdapter;
  private final CollectionResourceManagerAdapter<MODEL_OBJECT, DTO> collectionAdapter;

  IdResourceManagerAdapter(Manager<MODEL_OBJECT> manager, Class<MODEL_OBJECT> type) {
    this.manager = manager;
    singleAdapter = new SingleResourceManagerAdapter<>(manager, type);
    collectionAdapter = new CollectionResourceManagerAdapter<>(manager, type);
    this.type = type.getSimpleName();
  }

  Response get(String id, Function<MODEL_OBJECT, DTO> mapToDto) {
    return singleAdapter.get(loadBy(id), mapToDto);
  }

  public Response update(String id, Function<MODEL_OBJECT, MODEL_OBJECT> applyChanges) {
    return singleAdapter.update(
      loadBy(id),
      applyChanges,
      idStaysTheSame(id)
    );
  }

  public Response getAll(int page, int pageSize, Predicate<MODEL_OBJECT> filter, String sortBy, boolean desc, Function<PageResult<MODEL_OBJECT>, CollectionDto> mapToDto) {
    return collectionAdapter.getAll(page, pageSize, filter, sortBy, desc, mapToDto);
  }

  public Response create(DTO dto, Supplier<MODEL_OBJECT> modelObjectSupplier, Function<MODEL_OBJECT, String> uriCreator) {
    return collectionAdapter.create(dto, modelObjectSupplier, uriCreator);
  }

  public Response delete(String id) {
    return singleAdapter.delete(id);
  }

  private Supplier<MODEL_OBJECT> loadBy(String id) {
    return () -> Optional.ofNullable(manager.get(id)).orElseThrow(() -> new NotFoundException(type, id));
  }

  private Predicate<MODEL_OBJECT> idStaysTheSame(String id) {
    return changed -> changed.getId().equals(id);
  }
}
