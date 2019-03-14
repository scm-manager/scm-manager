package sonia.scm.security;

import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PermissionAssigner {

  private final SecuritySystem securitySystem;

  @Inject
  public PermissionAssigner(SecuritySystem securitySystem) {
    this.securitySystem = securitySystem;
  }

  public Collection<PermissionDescriptor> getAvailablePermissions() {
    PermissionPermissions.read().check();
    return securitySystem.getAvailablePermissions();
  }

  public Collection<PermissionDescriptor> readPermissionsForUser(String id) {
    return readPermissions(filterForUser(id));
  }

  public Collection<PermissionDescriptor> readPermissionsForGroup(String id) {
    return readPermissions(filterForGroup(id));
  }

  private Predicate<AssignedPermission> filterForUser(String id) {
    return p -> !p.isGroupPermission() && p.getName().equals(id);
  }

  private Predicate<AssignedPermission> filterForGroup(String id) {
    return p -> p.isGroupPermission() && p.getName().equals(id);
  }

  private Set<PermissionDescriptor> readPermissions(Predicate<AssignedPermission> predicate) {
    PermissionPermissions.read().check();
    return securitySystem.getPermissions(predicate)
      .stream()
      .map(AssignedPermission::getPermission)
      .collect(Collectors.toSet());
  }

  public void setPermissionsForUser(String id, Collection<PermissionDescriptor> permissions) {
    Collection<AssignedPermission> existingPermissions = securitySystem.getPermissions(filterForUser(id));
    adaptPermissions(id, false, permissions, existingPermissions);
  }

  public void setPermissionsForGroup(String id, Collection<PermissionDescriptor> permissions) {
    Collection<AssignedPermission> existingPermissions = securitySystem.getPermissions(filterForGroup(id));
    adaptPermissions(id, true, permissions, existingPermissions);
  }

  private void adaptPermissions(String id, boolean groupPermission, Collection<PermissionDescriptor> permissions, Collection<AssignedPermission> existingPermissions) {
    PermissionPermissions.assign().check();
    List<AssignedPermission> toRemove = existingPermissions.stream()
      .filter(p -> !permissions.contains(p.getPermission()))
      .collect(Collectors.toList());
    toRemove.forEach(securitySystem::deletePermission);

    Collection<PermissionDescriptor> availablePermissions = this.getAvailablePermissions();

    permissions.stream()
      .filter(permissionExists(availablePermissions, existingPermissions))
      .map(p -> new AssignedPermission(id, groupPermission, p))
      .filter(p -> !existingPermissions.contains(p))
      .forEach(securitySystem::addPermission);
  }

  private Predicate<PermissionDescriptor> permissionExists(Collection<PermissionDescriptor> availablePermissions, Collection<AssignedPermission> existingPermissions) {
    return p -> {
      if (!availablePermissions.contains(p) && existingPermissions.stream().map(AssignedPermission::getPermission).noneMatch(e -> e.equals(p))) {
        throw NotFoundException.notFound(ContextEntry.ContextBuilder.entity("Permission", p.getValue()));
      }
      return true;
    };
  }
}
