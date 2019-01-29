package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChangesetDto extends HalRepresentation {

  /**
   * The changeset identification string
   */
  private String id;

  /**
   * The author of the changeset
   */
  private PersonDto author;

  /**
   * The date when the changeset was committed
   */
  private Instant date;

  /**
   * The text of the changeset description
   */
  private String description;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation withEmbedded(String rel, List<? extends HalRepresentation> halRepresentations) {
    return super.withEmbedded(rel, halRepresentations);
  }


}
