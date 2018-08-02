package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;

abstract class CollectionToDtoMapper<E, D extends HalRepresentation> {

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
