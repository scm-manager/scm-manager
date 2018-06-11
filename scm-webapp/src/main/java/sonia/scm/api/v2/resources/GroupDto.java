package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Data @Accessors @NoArgsConstructor
public class GroupDto extends HalRepresentation {

  private Instant creationDate;
  private String description;
  private Optional<Instant> lastModified;
  private String name;
  private String type;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  @Override
  protected HalRepresentation withEmbedded(String rel, List<? extends HalRepresentation> embeddedItems) {
    return super.withEmbedded(rel, embeddedItems);
  }
}
