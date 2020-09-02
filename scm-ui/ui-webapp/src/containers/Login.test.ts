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

import { from } from "./Login";

describe("from tests", () => {
  it("should use default location", () => {
    const path = from("", {});
    expect(path).toBe("/");
  });

  it("should use default location without params", () => {
    const path = from();
    expect(path).toBe("/");
  });

  it("should use default location with null params", () => {
    const path = from("", null);
    expect(path).toBe("/");
  });

  it("should use location from query parameter", () => {
    const path = from("from=/repos", {});
    expect(path).toBe("/repos");
  });

  it("should use location from state", () => {
    const path = from("", { from: "/users" });
    expect(path).toBe("/users");
  });

  it("should prefer location from query parameter", () => {
    const path = from("from=/groups", { from: "/users" });
    expect(path).toBe("/groups");
  });

  it("should decode query param", () => {
    const path = from(`from=${encodeURIComponent("/admin/plugins/installed")}`);
    expect(path).toBe("/admin/plugins/installed");
  });
});
