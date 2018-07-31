package sonia.scm.config;

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;

/**
 * Base for all kinds of configurations.
 *
 * Allows for permission like
 *
 * <ul>
 *   <li>"configuration:read:global",</li>
 *   <li>"configuration:write:svn",</li>
 *   <li>"configuration:*:git",</li>
 *   <li>"configuration:*"</li>
 * </ul>
 *
 * <br/>
 *
 * And for permission checks like {@code ConfigurationPermissions.read(configurationObject).check();}
 */
@StaticPermissions(
  value = "configuration",
  permissions = {"read", "write"},
  globalPermissions = {}
)
public interface Configuration extends PermissionObject {
}
