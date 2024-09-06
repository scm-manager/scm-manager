/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
