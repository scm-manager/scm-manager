package sonia.scm.api.rest.resources;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

@Data
public class UserDto {
  private boolean active;
  private boolean admin;
  private Long creationDate;
  private String displayName;
  private Long lastModified;
  private String mail;
  private String name;
  private String password;
  private String type;

  @XmlElement(name = "_links")
  private Map<String, Link> links;
}
