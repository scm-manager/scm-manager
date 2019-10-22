import fetchMock from "fetch-mock";
import { CONTENT_TYPE_PASSWORD_OVERWRITE, setPassword } from "./setPassword";

describe("password change", () => {
  const SET_PASSWORD_URL = "/users/testuser/password";
  const newPassword = "testpw123";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should set password", done => {
    fetchMock.put("/api/v2" + SET_PASSWORD_URL, 204, {
      headers: {
        "content-type": CONTENT_TYPE_PASSWORD_OVERWRITE
      }
    });

    setPassword(SET_PASSWORD_URL, newPassword).then(content => {
      done();
    });
  });
});
