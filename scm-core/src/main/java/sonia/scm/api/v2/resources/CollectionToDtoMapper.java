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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;

public abstract class CollectionToDtoMapper<E, D extends HalRepresentation> {

  private final String collectionName;
  private final BaseMapper<E, D> mapper;

  protected CollectionToDtoMapper(String collectionName, BaseMapper<E, D> mapper) {
    this.collectionName = collectionName;
    this.mapper = mapper;
  }

  public HalRepresentation map(Collection<E> collection) {
    List<D> dtos = collection.stream().map(mapper::map).collect(Collectors.toList());
    return new HalRepresentation(
      linkingTo().self(createSelfLink()).build(),
      embeddedBuilder().with(collectionName, dtos).build()
    );
  }

  protected abstract String createSelfLink();

}
