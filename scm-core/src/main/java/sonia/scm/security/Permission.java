package sonia.scm.security;

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;

@StaticPermissions(
  value = "permission",
  permissions = {},
  globalPermissions = {"list", "read", "assign"}
)
public interface Permission extends PermissionObject {
}
