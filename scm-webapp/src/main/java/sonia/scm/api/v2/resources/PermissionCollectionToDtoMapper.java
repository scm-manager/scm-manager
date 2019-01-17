package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.PermissionPermissions;

import javax.inject.Inject;
import java.util.Collection;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

public class PermissionCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;

  @Inject
  public PermissionCollectionToDtoMapper(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public PermissionListDto map(Collection<PermissionDescriptor> permissions, @Context String userId) {
    String[] permissionStrings = permissions
      .stream()
      .map(PermissionDescriptor::getValue)
      .toArray(String[]::new);
    PermissionListDto target = new PermissionListDto(permissionStrings);

    Links.Builder linksBuilder = linkingTo().self(resourceLinks.userPermissions().permissions(userId));

    if (PermissionPermissions.assign().isPermitted()) {
      linksBuilder.single(link("overwrite", resourceLinks.userPermissions().overwritePermissions(userId)));
    }

    target.add(linksBuilder.build());

    return target;
  }
}
