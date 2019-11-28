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
