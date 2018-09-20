package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ModificationsDto extends HalRepresentation {


  private String revision;
  /**
   * list of added files
   */
  private List<String> added;

  /**
   * list of modified files
   */
  private List<String> modified;

  /**
   * list of removed files
   */
  private List<String> removed;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

}
