/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
