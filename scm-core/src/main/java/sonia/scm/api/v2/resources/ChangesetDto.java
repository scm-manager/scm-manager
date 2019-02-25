package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

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

  public ChangesetDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
