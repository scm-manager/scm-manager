/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
        id: "earth"
      }
    ],
    violations: []
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
