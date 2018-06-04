package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserDto {
  private boolean active;
  private boolean admin;
  private Instant creationDate;
  private String displayName;
  private Optional<Instant> lastModified;
  private String mail;
  private String name;
  private String password;
  private String type;

  @JsonProperty("_links")
  private Map<String, Link> links;
}
