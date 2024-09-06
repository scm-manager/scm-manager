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

package sonia.scm;

import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public abstract class GenericDisplayManager<D, T extends ReducedModelObject> implements DisplayManager<T> {

  private final GenericDAO<D> dao;
  private final Function<D, T> transform;

  protected GenericDisplayManager(GenericDAO<D> dao, Function<D, T> transform) {
    this.dao = dao;
    this.transform = transform;
  }

  @Override
  public Collection<T> autocomplete(String filter) {
    checkPermission();
    SearchRequest searchRequest = new SearchRequest(filter, true, DEFAULT_LIMIT);
    return SearchUtil.search(
      searchRequest,
      dao.getAll(),
      object -> matches(searchRequest, object)? transform.apply(object): null
    );
  }

  protected abstract void checkPermission();

  protected abstract boolean matches(SearchRequest searchRequest, D object);

  @Override
  public Optional<T> get(String id) {
    if (id == null) {
      return empty();
    }
    return ofNullable(dao.get(id)).map(transform);
  }
}
