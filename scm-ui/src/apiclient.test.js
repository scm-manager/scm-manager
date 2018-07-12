// @flow
import { createUrl } from "./apiclient";

test("create url, should not change absolute urls", () => {
  expect(createUrl("https://www.scm-manager.org")).toBe(
    "https://www.scm-manager.org"
  );
});

test("create url, should add prefix for api", () => {
  expect(createUrl("/users")).toBe("/scm/api/rest/v2/users");
  expect(createUrl("users")).toBe("/scm/api/rest/v2/users");
});
