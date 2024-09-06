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

import com.google.inject.ProvisionException;

public class OutOfRequestScopeException extends RuntimeException {

  public OutOfRequestScopeException(ProvisionException cause) {
    super("Out of request scope\n\n" +
        "==============================================================\n" +
        "Cannot create links, because we are out of the request scope.\n" +
        "Most probably this is because you were trying to create a link\n" +
        "in a context that has no encapsulating http request. It is not\n" +
        "possible to create these links outside of requests, because\n" +
        "the computation uses the originating url of the http request\n" +
        "as a basis. To create urls outside of request scopes, please\n" +
        "inject an instance of ScmConfiguration and use getBaseUrl().\n" +
        "==============================================================\n",
      cause);
  }
}
