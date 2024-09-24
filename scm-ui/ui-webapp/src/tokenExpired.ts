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

import { apiClient, clearCache, TokenExpiredError } from "@scm-manager/ui-api";

let tokenExpired = false;

// If the token is expired we want to show an error with the login link.
// This error should be displayed with the state (e.g. navigation) of the previous logged in user.
// But if the user navigates away, we want to reset the state to an anonymous one.

apiClient.onError(error => {
  if (error instanceof TokenExpiredError) {
    tokenExpired = true;
  }
});

apiClient.onRequest(() => {
  if (tokenExpired) {
    clearCache().then(() => {
      tokenExpired = false;
    });
  }
});
