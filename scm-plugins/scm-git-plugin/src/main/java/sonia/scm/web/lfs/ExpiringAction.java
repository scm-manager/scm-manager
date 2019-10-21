package sonia.scm.web.lfs;

import org.eclipse.jgit.lfs.server.Response;
import sonia.scm.security.AccessToken;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.TimeZone;

class ExpiringAction extends Response.Action {
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  static {
    DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  @SuppressWarnings({"squid:S00116"})
  // This class is used for json serialization, only
  public final String expires_at;

  ExpiringAction(String href, AccessToken accessToken) {
    this.expires_at = DATE_FORMAT.format(accessToken.getExpiration());
    this.href = href;
    this.header = Collections.singletonMap("Authorization", "Bearer " + accessToken.compact());
  }
}
