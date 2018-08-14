package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PermissionDto extends HalRepresentation {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private PermissionTypeDto type = PermissionTypeDto.READ;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String name;

  private boolean groupPermission = false;


  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
