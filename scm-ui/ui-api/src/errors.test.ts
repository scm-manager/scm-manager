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

import { BackendError, UnauthorizedError, createBackendError, NotFoundError } from "./errors";

describe("test createBackendError", () => {
  const earthNotFoundError = {
    transactionId: "42t",
    errorCode: "42e",
    message: "earth not found",
    context: [
      {
        type: "planet",
        id: "earth",
      },
    ],
    violations: [],
  };

  it("should return a default backend error", () => {
    const err = createBackendError(earthNotFoundError, 500);
    expect(err).toBeInstanceOf(BackendError);
    expect(err.name).toBe("BackendError");
  });

  // 403 is no backend error
  xit("should return an unauthorized error for status code 403", () => {
    const err = createBackendError(earthNotFoundError, 403);
    expect(err).toBeInstanceOf(UnauthorizedError);
    expect(err.name).toBe("UnauthorizedError");
  });

  it("should return an not found error for status code 404", () => {
    const err = createBackendError(earthNotFoundError, 404);
    expect(err).toBeInstanceOf(NotFoundError);
    expect(err.name).toBe("NotFoundError");
  });
});
