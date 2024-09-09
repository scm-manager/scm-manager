/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web.lfs;

import org.eclipse.jgit.lfs.server.Response;
import sonia.scm.security.AccessToken;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.TimeZone;

class ExpiringAction extends Response.Action {

  @SuppressWarnings({"squid:S00116"})
  // This class is used for json serialization, only
  public final String expires_at;

  ExpiringAction(String href, AccessToken accessToken) {
    this.expires_at = createDateFormat().format(accessToken.getExpiration());
    this.href = href;
    this.header = Collections.singletonMap("Authorization", "Bearer " + accessToken.compact());
  }

  private DateFormat createDateFormat() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat;
  }
}
