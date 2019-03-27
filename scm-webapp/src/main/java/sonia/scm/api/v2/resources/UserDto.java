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
import java.util.Map;

@NoArgsConstructor @Getter @Setter
public class UserDto extends HalRepresentation {
  private boolean active;
  private Instant creationDate;
  @NotEmpty
  private String displayName;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant lastModified;
  @NotEmpty @Email
  private String mail;
  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String password;
  private String type;
  private Map<String, String> properties;

  UserDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
