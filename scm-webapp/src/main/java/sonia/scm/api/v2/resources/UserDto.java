package sonia.scm.api.v2.resources;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Data
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

  @XmlElement(name = "_links")
  private Map<String, Link> links;
}
