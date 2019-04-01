package sonia.scm;

import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static sonia.scm.group.DisplayGroup.from;

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
    return ofNullable(dao.get(id)).map(transform);
  }
}
