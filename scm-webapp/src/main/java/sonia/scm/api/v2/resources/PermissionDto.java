package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PermissionDto {
  private String type;
  private String name;
  private boolean groupPermission;
}
