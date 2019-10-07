// @flow

import { BackendError, UnauthorizedError, createBackendError, NotFoundError } from "./errors";

describe("test createBackendError", () => {

  const earthNotFoundError = {
    transactionId: "42t",
    errorCode: "42e",
    message: "earth not found",
    context: [{
      type: "planet",
      id: "earth"
    }]
  };

  it("should return a default backend error", () => {
    const err = createBackendError(earthNotFoundError, 500);
    expect(err).toBeInstanceOf(BackendError);
    expect(err.name).toBe("BackendError");
  });

  it("should return an unauthorized error for status code 403", () => {
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
