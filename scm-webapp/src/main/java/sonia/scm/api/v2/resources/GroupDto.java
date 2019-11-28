package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.util.ValidationUtil;

import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor
public class GroupDto extends HalRepresentation {

  private Instant creationDate;
  private String description;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant lastModified;
  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  private String name;
  private String type;
  private Map<String, String> properties;
  private List<String> members;
  private boolean external;

  GroupDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
