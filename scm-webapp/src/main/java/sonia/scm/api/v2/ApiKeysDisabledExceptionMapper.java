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

package sonia.scm.api.v2;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import sonia.scm.api.rest.ContextualExceptionMapper;
import sonia.scm.api.v2.resources.ExceptionWithContextToErrorDtoMapper;
import sonia.scm.security.ApiKeysDisabledException;

/**
 * @since 2.43.0
 */
@Provider
public class ApiKeysDisabledExceptionMapper extends ContextualExceptionMapper<ApiKeysDisabledException> {
  @Inject
  public ApiKeysDisabledExceptionMapper(ExceptionWithContextToErrorDtoMapper mapper) {
    // We will use the http status code 404 (not found) here, to make a distinction
    // between the case of disabled api keys and an already existing api key.
    super(ApiKeysDisabledException.class, Response.Status.NOT_FOUND, mapper);
  }
}
