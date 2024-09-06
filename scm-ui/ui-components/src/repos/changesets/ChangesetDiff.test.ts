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
