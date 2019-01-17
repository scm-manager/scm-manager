package sonia.scm.security;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionAssigner {

  private final SecuritySystem securitySystem;

  @Inject
  public PermissionAssigner(SecuritySystem securitySystem) {
    this.securitySystem = securitySystem;
  }

  public Collection<PermissionDescriptor> getAvailablePermissions() {
    return securitySystem.getAvailablePermissions();
  }

  public Collection<PermissionDescriptor> readPermissionsForUser(String id) {
    return securitySystem.getPermissions(p -> !p.isGroupPermission() && p.getName().equals(id)).stream().map(AssignedPermission::getPermission).collect(Collectors.toSet());
  }

  public void setPermissionsForUser(String id, Collection<PermissionDescriptor> permissions) {
    Collection<AssignedPermission> existingPermissions = securitySystem.getPermissions(p -> !p.isGroupPermission() && p.getName().equals(id));
    List<AssignedPermission> toRemove = existingPermissions.stream()
      .filter(p -> !permissions.contains(p.getPermission()))
      .collect(Collectors.toList());
    toRemove.forEach(securitySystem::deletePermission);

    permissions.stream()
      .map(p -> new AssignedPermission(id, false, p))
      .filter(p -> !existingPermissions.contains(p))
      .forEach(securitySystem::addPermission);
  }
}
