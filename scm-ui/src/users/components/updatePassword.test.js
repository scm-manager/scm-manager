//@flow
import fetchMock from "fetch-mock";
import { setPassword, updatePassword } from "./changePassword";

describe("password change", () => {
  const SET_PASSWORD_URL = "/users/testuser/password";
  const CHANGE_PASSWORD_URL = "/me/password";
  const oldPassword = "old";
  const newPassword = "testpw123";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  // TODO: Verify content type
  it("should set password", done => {
    fetchMock.put("/api/v2" + SET_PASSWORD_URL, 204);

    setPassword(SET_PASSWORD_URL, newPassword).then(content => {
      done();
    });
  });

  // TODO: Verify content type
  it("should update password", done => {
    fetchMock.put("/api/v2" + CHANGE_PASSWORD_URL, 204);

    updatePassword(CHANGE_PASSWORD_URL, oldPassword, newPassword).then(
      content => {
        done();
      }
    );
  });
});
