package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static sonia.scm.api.v2.ValidationConstraints.USER_GROUP_PATTERN;

@Getter @Setter @NoArgsConstructor
public class GroupDto extends HalRepresentation {

  private Instant creationDate;
  private String description;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant lastModified;
  @Pattern(regexp = USER_GROUP_PATTERN)
  private String name;
  @NotEmpty
  private String type;
  private Map<String, String> properties;
  private List<String> members;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }

  public HalRepresentation withMembers(List<MemberDto> members) {
    return super.withEmbedded("members", members);
  }
}
