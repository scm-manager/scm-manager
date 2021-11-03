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

import { createUrl, isDiffSupported } from "./ChangesetDiff";

describe("isDiffSupported tests", () => {
  it("should return true if diff link is defined", () => {
    const supported = isDiffSupported({
      _links: {
        diff: {
          href: "http://diff",
        },
      },
    });

    expect(supported).toBe(true);
  });

  it("should return true if parsed diff link is defined", () => {
    const supported = isDiffSupported({
      _links: {
        diffParsed: {
          href: "http://diff",
        },
      },
    });

    expect(supported).toBe(true);
  });

  it("should return false if not diff link was provided", () => {
    const supported = isDiffSupported({
      _links: {},
    });

    expect(supported).toBe(false);
  });
});

describe("createUrl tests", () => {
  it("should return the diff url, if only diff url is defined", () => {
    const url = createUrl({
      _links: {
        diff: {
          href: "http://diff",
        },
      },
    });

    expect(url).toBe("http://diff?format=GIT");
  });

  it("should return the diff parsed url, if only diff parsed url is defined", () => {
    const url = createUrl({
      _links: {
        diffParsed: {
          href: "http://diff-parsed",
        },
      },
    });

    expect(url).toBe("http://diff-parsed");
  });

  it("should return the diff parsed url, if both diff links are defined", () => {
    const url = createUrl({
      _links: {
        diff: {
          href: "http://diff",
        },
        diffParsed: {
          href: "http://diff-parsed",
        },
      },
    });

    expect(url).toBe("http://diff-parsed");
  });

  it("should throw an error if no diff link is defined", () => {
    expect(() =>
      createUrl({
        _links: {},
      })
    ).toThrow();
  });
});
