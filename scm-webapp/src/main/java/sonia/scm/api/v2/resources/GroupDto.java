package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Data @NoArgsConstructor
public class GroupDto extends HalRepresentation {

  private Instant creationDate;
  private String description;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Optional<Instant> lastModified;
  private String name;
  private String type;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  public HalRepresentation withMembers(List<MemberDto> members) {
    return super.withEmbedded("members", members);
  }
}
