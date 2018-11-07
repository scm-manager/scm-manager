//@flow
import fetchMock from "fetch-mock";
import { updatePassword } from "./updatePassword";

describe("get content type", () => {
  const PASSWORD_URL = "/users/testuser/password";
  const password = "testpw123";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should update password", done => {

    fetchMock.put("/api/v2" + PASSWORD_URL, 204);

    updatePassword(PASSWORD_URL, password).then(content => {

      done();
    });
  });
});
