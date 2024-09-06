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
import sonia.scm.ConcurrentModificationException;
import sonia.scm.IllegalIdentifierChangeException;
import sonia.scm.Manager;
import sonia.scm.ModelObject;
import sonia.scm.NotFoundException;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
      throw new IllegalIdentifierChangeException("illegal change of id");
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
