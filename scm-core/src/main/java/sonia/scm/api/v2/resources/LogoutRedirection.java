package sonia.scm.api.v2.resources;

import sonia.scm.plugin.ExtensionPoint;

import java.net.URI;
import java.util.Optional;

@ExtensionPoint(multi = false)
@FunctionalInterface
public interface LogoutRedirection {
  Optional<URI> afterLogoutRedirectTo();
}
