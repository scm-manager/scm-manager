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

import { Repository } from "@scm-manager/ui-types";
import { getProtocolLinkByType } from "./repositories";

describe("getProtocolLinkByType tests", () => {
  it("should return the http protocol link", () => {
    const repository: Repository = {
      namespace: "scm",
      name: "core",
      type: "git",
      _links: {
        protocol: [
          {
            name: "http",
            href: "http://scm.scm-manager.org/repo/scm/core",
          },
        ],
      },
    };

    const link = getProtocolLinkByType(repository, "http");
    expect(link).toBe("http://scm.scm-manager.org/repo/scm/core");
  });

  it("should return the http protocol link from multiple protocols", () => {
    const repository: Repository = {
      namespace: "scm",
      name: "core",
      type: "git",
      _links: {
        protocol: [
          {
            name: "http",
            href: "http://scm.scm-manager.org/repo/scm/core",
          },
          {
            name: "ssh",
            href: "git@scm.scm-manager.org:scm/core",
          },
        ],
      },
    };

    const link = getProtocolLinkByType(repository, "http");
    expect(link).toBe("http://scm.scm-manager.org/repo/scm/core");
  });

  it("should return the http protocol, even if the protocol is a single link", () => {
    const repository: Repository = {
      namespace: "scm",
      name: "core",
      type: "git",
      _links: {
        protocol: {
          name: "http",
          href: "http://scm.scm-manager.org/repo/scm/core",
        },
      },
    };

    const link = getProtocolLinkByType(repository, "http");
    expect(link).toBe("http://scm.scm-manager.org/repo/scm/core");
  });

  it("should return null, if such a protocol does not exists", () => {
    const repository: Repository = {
      namespace: "scm",
      name: "core",
      type: "git",
      _links: {
        protocol: [
          {
            name: "http",
            href: "http://scm.scm-manager.org/repo/scm/core",
          },
          {
            name: "ssh",
            href: "git@scm.scm-manager.org:scm/core",
          },
        ],
      },
    };

    const link = getProtocolLinkByType(repository, "awesome");
    expect(link).toBeNull();
  });

  it("should return null, if no protocols are available", () => {
    const repository: Repository = {
      namespace: "scm",
      name: "core",
      type: "git",
      _links: {},
    };

    const link = getProtocolLinkByType(repository, "http");
    expect(link).toBeNull();
  });
});
