package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Pattern;

import static sonia.scm.api.v2.ValidationConstraints.USER_GROUP_PATTERN;

@Getter @Setter @ToString @NoArgsConstructor
public class RepositoryPermissionDto extends HalRepresentation {

  public static final String GROUP_PREFIX = "@";

  @Pattern(regexp = USER_GROUP_PATTERN)
  private String name;

  /**
   * the type can be replaced with a dto enum if the mapstruct 1.3.0 is stable
   * the mapstruct has a Bug on mapping enums in the 1.2.0-Final Version
   *
   * see the bug fix: https://github.com/mapstruct/mapstruct/commit/460e87eef6eb71245b387fdb0509c726676a8e19
   *
   **/
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String type;


  private boolean groupPermission = false;

  public RepositoryPermissionDto(String permissionName, boolean groupPermission) {
    name = permissionName;
    this.groupPermission = groupPermission;
  }


  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
