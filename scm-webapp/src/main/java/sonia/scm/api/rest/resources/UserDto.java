package sonia.scm.api.rest.resources;

import lombok.Data;

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
}
