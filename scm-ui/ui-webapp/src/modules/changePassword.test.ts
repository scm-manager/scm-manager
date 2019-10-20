import fetchMock from "fetch-mock";
import { changePassword, CONTENT_TYPE_PASSWORD_CHANGE } from "./changePassword";

describe("change password", () => {
  const CHANGE_PASSWORD_URL = "/me/password";
  const oldPassword = "old";
  const newPassword = "new";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should update password", done => {
    fetchMock.put("/api/v2" + CHANGE_PASSWORD_URL, 204, {
      headers: {
        "content-type": CONTENT_TYPE_PASSWORD_CHANGE
      }
    });

    changePassword(CHANGE_PASSWORD_URL, oldPassword, newPassword).then(
      content => {
        done();
      }
    );
  });
});
