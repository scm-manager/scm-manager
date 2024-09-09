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

package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.web.ScmHttpConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class PullHttpConnectionProvider {

  private final HttpURLConnectionFactory httpURLConnectionFactory;

  @Inject
  PullHttpConnectionProvider(HttpURLConnectionFactory httpURLConnectionFactory) {
    this.httpURLConnectionFactory = httpURLConnectionFactory;
  }

  HttpConnectionFactory createHttpConnectionFactory(PullCommandRequest request) {
    HttpConnectionOptions options = new HttpConnectionOptions();
    if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
      String encodedAuth = Base64.getEncoder().encodeToString((request.getUsername() + ":" + request.getPassword()).getBytes(StandardCharsets.UTF_8));
      String authHeaderValue = "Basic " + encodedAuth;
      options.addRequestProperty("Authorization", authHeaderValue);
    }
    return new ScmHttpConnectionFactory(httpURLConnectionFactory, options);
  }
}
