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

import React from "react";
import { createLocalLink } from "./MarkdownImageRenderer";

describe("createLocalLink tests", () => {
  const revision = "revision";
  const basePath = `/repo/namespace/name/code/sources/${revision}/`;
  const contentLink = "http://localhost:8081/scm/api/v2/repositories/namespace/name/content/{revision}/{path}";
  const currentPath = basePath + "README.md/";
  const link = "image.png";

  it("should return link for internal scm repo link", () => {
    const internalScmLink = "/repo/namespace/name/code/sources/develop/myImg.png";
    expect(createLocalLink(basePath, contentLink, revision, currentPath, internalScmLink)).toBe(internalScmLink);
  });

  it("should return modified contentLink for absolute link", () => {
    expect(createLocalLink(basePath, contentLink, revision, currentPath, "/path/anotherImg.jpg")).toBe(
      "http://localhost:8081/scm/api/v2/repositories/namespace/name/content/revision/path/anotherImg.jpg"
    );
  });

  it("should URI encode branch", () => {
    expect(
      createLocalLink(
        "/repo/namespace/name/code/sources/feature/awesome/",
        contentLink,
        "feature/awesome",
        currentPath,
        link
      )
    ).toContain("feature%2Fawesome");
  });
});
