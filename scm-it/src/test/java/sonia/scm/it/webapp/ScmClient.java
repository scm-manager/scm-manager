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

package sonia.scm.it.webapp;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;

import java.util.Base64;

import static sonia.scm.it.webapp.IntegrationTestUtil.createClient;

public class ScmClient {
  private final String user;
  private final String password;

  private final Client client;

  public static ScmClient anonymous() {
    return new ScmClient(null, null);
  }

  public ScmClient(String user, String password) {
    this.user = user;
    this.password = password;
    this.client = createClient();
  }

  public Invocation.Builder resource(String url) {
    if (user == null) {
      return client.target(url).request();
    } else {
      return client.target(url).request().header("Authorization", createAuthHeaderValue());
    }
  }

  public Invocation.Builder resource(String url, MediaType mediaType) {
    if (user == null) {
      return client.target(url).request(mediaType);
    } else {
      return client.target(url).request(mediaType).header("Authorization", createAuthHeaderValue());
    }
  }

  public String createAuthHeaderValue() {
    return "Basic " + Base64.getEncoder().encodeToString((user +":"+ password).getBytes());
  }
}
