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

package sonia.scm.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;

@RequestScoped
public class DefaultRepositoryProvider implements RepositoryProvider {

  public static final String ATTRIBUTE_NAME = "scm.request.repository";

  private final Provider<HttpServletRequest> requestProvider;

  @Inject
  public DefaultRepositoryProvider(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  @Override
  public Repository get() {
    HttpServletRequest request = requestProvider.get();

    if (request != null) {
      return (Repository) request.getAttribute(ATTRIBUTE_NAME);
    }

    throw new IllegalStateException("request not found");
  }
}
