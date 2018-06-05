package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserDto extends HalRepresentation {
  private boolean active;
  private boolean admin;
  private Instant creationDate;
  private String displayName;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Optional<Instant> lastModified;
  private String mail;
  private String name;
  private String password;
  private String type;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
