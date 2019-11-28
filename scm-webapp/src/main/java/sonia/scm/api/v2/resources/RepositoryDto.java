package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import sonia.scm.util.ValidationUtil;

import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor
public class RepositoryDto extends HalRepresentation {

  @Email
  private String contact;
  private Instant creationDate;
  private String description;
  private List<HealthCheckFailureDto> healthCheckFailures;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant lastModified;
  // we could not validate the namespace, this must be done by the namespace strategy
  private String namespace;
  @Pattern(regexp = ValidationUtil.REGEX_REPOSITORYNAME)
  private String name;
  private boolean archived = false;
  @NotEmpty
  private String type;
  protected Map<String, String> properties;

  RepositoryDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
