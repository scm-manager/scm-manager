package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true)
public class GroupDto extends HalRepresentation {

  private Instant creationDate;
  private String description;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Optional<Instant> lastModified;
  private String name;
  private String type;
  private Map<String, String> properties;
  private List<String> members;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  public HalRepresentation withMembers(List<MemberDto> members) {
    return super.withEmbedded("members", members);
  }
}
