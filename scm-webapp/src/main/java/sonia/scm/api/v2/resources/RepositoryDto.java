package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class RepositoryDto extends HalRepresentation {

  @Email
  private String contact;
  private Instant creationDate;
  private String description;
  private List<HealthCheckFailureDto> healthCheckFailures;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant lastModified;
  private String namespace;
  @Pattern(regexp = "(?!^\\.\\.$)(?!^\\.$)(?!.*[\\\\\\[\\]])^[A-z0-9\\.][A-z0-9\\.\\-_/]*$")
  private String name;
  private boolean archived = false;
  @NotEmpty
  private String type;
  protected Map<String, String> properties;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
