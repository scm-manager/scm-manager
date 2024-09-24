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

package sonia.scm.web;

import jakarta.inject.Inject;
import org.eclipse.jgit.transport.http.HttpConnection;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.eclipse.jgit.transport.http.WrappedHttpUrlConnection;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

public class ScmHttpConnectionFactory implements HttpConnectionFactory {

  private final HttpURLConnectionFactory connectionFactory;
  private final HttpConnectionOptions options;

  @Inject
  public ScmHttpConnectionFactory(HttpURLConnectionFactory connectionFactory) {
    this(connectionFactory, new HttpConnectionOptions());
  }

  public ScmHttpConnectionFactory(HttpURLConnectionFactory connectionFactory, HttpConnectionOptions options) {
    this.connectionFactory = connectionFactory;
    this.options = options;
  }

  @Override
  public HttpConnection create(URL url) throws IOException {
    return new WrappedHttpUrlConnection(connectionFactory.create(url, options));
  }

  @Override
  public HttpConnection create(URL url, Proxy proxy) throws IOException {
    // we ignore proxy configuration of jgit, because we have our own
    return create(url);
  }
}
