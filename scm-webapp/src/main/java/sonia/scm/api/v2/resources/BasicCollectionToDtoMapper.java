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
import sonia.scm.ModelObject;
import sonia.scm.PageResult;

import java.util.Optional;

public class BasicCollectionToDtoMapper<E extends ModelObject, D extends HalRepresentation, M extends BaseMapper<E, D>> extends PagedCollectionToDtoMapper<E, D> {

  private final M entityToDtoMapper;

  public BasicCollectionToDtoMapper(String collectionName, M entityToDtoMapper) {
    super(collectionName);
    this.entityToDtoMapper = entityToDtoMapper;
  }

  CollectionDto map(int pageNumber, int pageSize, PageResult<E> pageResult, String selfLink, Optional<String> createLink) {
    return map(pageNumber, pageSize, pageResult, selfLink, createLink, entityToDtoMapper::map);
  }
}
