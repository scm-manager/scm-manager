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
import sonia.scm.PageResult;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Comparables;
import sonia.scm.util.Util;

import java.net.URI;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Adapter from resource http endpoints to managers, for Collection resources (e.g. {@code /users}).
 *
 * Provides common CRUD operations and DTO to Model Object mapping to keep Resources more DRY.
 *
 * @param <MODEL_OBJECT> The type of the model object, eg. {@link sonia.scm.user.User}.
 * @param <DTO> The corresponding transport object, eg. {@link UserDto}.
 *
 * @see SingleResourceManagerAdapter
 */
@SuppressWarnings("squid:S00119") // "MODEL_OBJECT" is much more meaningful than "M", right?
class CollectionResourceManagerAdapter<MODEL_OBJECT extends ModelObject,
                             DTO extends HalRepresentation>{

  protected final Manager<MODEL_OBJECT> manager;
  protected final Class<MODEL_OBJECT> type;

  CollectionResourceManagerAdapter(Manager<MODEL_OBJECT> manager, Class<MODEL_OBJECT> type) {
    this.manager = manager;
    this.type = type;
  }

  /**
   * Reads all model objects in a paged way, maps them using the given function and returns a corresponding http response.
   * This handles all corner cases, eg. missing privileges.
   */
  public Response getAll(int page, int pageSize, Predicate<MODEL_OBJECT> filter, String sortBy, boolean desc, Function<PageResult<MODEL_OBJECT>, CollectionDto> mapToDto) {
    PageResult<MODEL_OBJECT> pageResult = fetchPage(filter, sortBy, desc, page, pageSize);
    return Response.ok(mapToDto.apply(pageResult)).build();
  }

  private PageResult<MODEL_OBJECT> fetchPage(Predicate<MODEL_OBJECT> filter, String sortBy, boolean desc, int pageNumber,
                                    int pageSize) {
    AssertUtil.assertPositive(pageNumber);
    AssertUtil.assertPositive(pageSize);

    Comparator<MODEL_OBJECT> comparator = null;
    if (!Util.isEmpty(sortBy)) {
      comparator = createComparator(sortBy, desc);
    }

    return manager.getPage(filter, comparator, pageNumber, pageSize);
  }

  private Comparator<MODEL_OBJECT> createComparator(String sortBy, boolean desc) {
    Comparator<MODEL_OBJECT> comparator = Comparables.comparator(type, sortBy);
    if (desc) {
      comparator = comparator.reversed();
    }
    return comparator;
  }

  /**
   * Creates a model object for the given dto and returns a corresponding http response.
   * This handles all corner cases, eg. no conflicts or missing privileges.
   */
  public Response create(DTO dto, Supplier<MODEL_OBJECT> modelObjectSupplier, Function<MODEL_OBJECT, String> uriCreator) {
    if (dto == null) {
      return Response.status(BAD_REQUEST).build();
    }
    MODEL_OBJECT modelObject = modelObjectSupplier.get();
    MODEL_OBJECT created = manager.create(modelObject);
    return Response.created(URI.create(uriCreator.apply(created))).build();
  }

  protected String getId(MODEL_OBJECT item) {
    return item.getId();
  }
}
