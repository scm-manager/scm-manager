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
import { requiredLink } from "./links";

describe("requireLink tests", () => {
  it("should return required link", () => {
    const link = requiredLink(
      {
        _links: {
          spaceship: {
            href: "/v2/ship",
          },
        },
      },
      "spaceship"
    );
    expect(link).toBe("/v2/ship");
  });

  it("should throw error, if link is missing", () => {
    const object = { _links: {} };
    expect(() => requiredLink(object, "spaceship")).toThrowError();
  });

  it("should throw error, if link is array", () => {
    const object = {
      _links: {
        spaceship: [
          {
            name: "one",
            href: "/v2/one",
          },
          {
            name: "two",
            href: "/v2/two",
          },
        ],
      },
    };
    expect(() => requiredLink(object, "spaceship")).toThrowError();
  });

  it("should return sub-link if it exists", () => {
    const object = {
      _links: {
        spaceship: [
          {
            name: "one",
            href: "/v2/one",
          },
          {
            name: "two",
            href: "/v2/two",
          },
        ],
      },
    };
    expect(requiredLink(object, "spaceship", "one")).toBe("/v2/one");
  });

  it("should throw error, if sub-link does not exist in link array", () => {
    const object = {
      _links: {
        spaceship: [
          {
            name: "one",
            href: "/v2/one",
          },
          {
            name: "two",
            href: "/v2/two",
          },
        ],
      },
    };
    expect(() => requiredLink(object, "spaceship", "three")).toThrowError();
  });
});
