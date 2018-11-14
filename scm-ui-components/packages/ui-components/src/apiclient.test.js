// @flow
import {apiClient, createUrl} from "./apiclient";
import fetchMock from "fetch-mock";

describe("apiClient", () => {
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  describe("create url", () => {
    it("should not change absolute urls", () => {
      expect(createUrl("https://www.scm-manager.org")).toBe(
        "https://www.scm-manager.org"
      );
    });

    it("should add prefix for api", () => {
      expect(createUrl("/users")).toBe("/api/v2/users");
      expect(createUrl("users")).toBe("/api/v2/users");
    });
  });

  describe("error handling", () => {
    const error = {
      message: "Error!!"
    };

    it("should append default error message for 401 if none provided", () => {
      fetchMock.mock("api/v2/foo", 401);
      return apiClient
        .get("foo")
        .catch(err => {
          expect(err.message).toEqual("unauthorized");
        });
    });

    it("should append error message for 401 if provided", () => {
      fetchMock.mock("api/v2/foo", {"status": 401, body: error});
      return apiClient
        .get("foo")
        .catch(err => {
          expect(err.message).toEqual("Error!!");
        });
    });

    it("should append default error message for 401 if none provided", () => {
      fetchMock.mock("api/v2/foo", 404);
      return apiClient
        .get("foo")
        .catch(err => {
          expect(err.message).toEqual("not found");
        });
    });

    it("should append error message for 404 if provided", () => {
      fetchMock.mock("api/v2/foo", {"status": 404, body: error});
      return apiClient
        .get("foo")
        .catch(err => {
          expect(err.message).toEqual("Error!!");
        });
    });
  });
});
