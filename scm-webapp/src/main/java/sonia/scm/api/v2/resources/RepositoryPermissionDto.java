package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import javax.validation.constraints.NotEmpty;
import sonia.scm.util.ValidationUtil;

import javax.validation.constraints.Pattern;

import java.util.Collection;

@Getter @Setter @ToString @NoArgsConstructor
@EitherRoleOrVerbs
public class RepositoryPermissionDto extends HalRepresentation {

  public static final String GROUP_PREFIX = "@";

  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  private String name;

  @NoBlankStrings
  private Collection<String> verbs;

  private String role;

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
