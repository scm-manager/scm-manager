package sonia.scm.web.lfs;

import org.eclipse.jgit.lfs.server.Response;
import sonia.scm.security.AccessToken;

import java.text.SimpleDateFormat;
import java.util.Collections;

@SuppressWarnings({"squid:S00116"})
// This class is used for json serialization, only
class ExpiringAction extends Response.Action {
  public final String expires_at;

  ExpiringAction(String href, AccessToken accessToken) {
    this.expires_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:MM:ss'Z'").format(accessToken.getExpiration());
    this.href = href;
    this.header = Collections.singletonMap("Authorization", "Bearer " + accessToken.compact());
  }
}
