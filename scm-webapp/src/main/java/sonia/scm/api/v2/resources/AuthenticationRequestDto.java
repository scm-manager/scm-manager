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

package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import jakarta.ws.rs.FormParam;

import java.util.List;

public class AuthenticationRequestDto {

  @FormParam("grant_type")
  @JsonProperty("grant_type")
  private String grantType;

  @FormParam("username")
  private String username;

  @FormParam("password")
  private String password;

  @FormParam("cookie")
  private boolean cookie;

  @FormParam("scope")
  private List<String> scope;

  public String getGrantType() {
    return grantType;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public boolean isCookie() {
    return cookie;
  }

  public List<String> getScope() {
    return scope;
  }

  public boolean isValid() {
    // password is currently the only valid grant_type
    return "password".equals(grantType) && !Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password);
  }
}
