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
