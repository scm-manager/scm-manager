package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Data @AllArgsConstructor @NoArgsConstructor
public class UserDto {
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

  @JsonProperty("_links")
  private Map<String, Link> links;
}
