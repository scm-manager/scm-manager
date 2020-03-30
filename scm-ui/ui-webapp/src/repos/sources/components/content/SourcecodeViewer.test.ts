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
import { getContent, getLanguage } from "./SourcecodeViewer";

describe("get content", () => {
  const CONTENT_URL = "/repositories/scmadmin/TestRepo/content/testContent";

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should return content", done => {
    fetchMock.getOnce("/api/v2" + CONTENT_URL, "This is a testContent");

    getContent(CONTENT_URL).then(content => {
      expect(content).toBe("This is a testContent");
      done();
    });
  });
});

describe("get correct language type", () => {
  it("should return javascript", () => {
    expect(getLanguage("JAVASCRIPT")).toBe("javascript");
  });
  it("should return nothing for plain text", () => {
    expect(getLanguage("")).toBe("");
  });
});
