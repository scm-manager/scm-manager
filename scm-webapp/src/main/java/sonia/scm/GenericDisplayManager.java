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
