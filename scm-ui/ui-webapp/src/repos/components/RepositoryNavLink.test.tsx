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
import { mount, shallow } from "@scm-manager/ui-tests/enzyme-router";
import "@scm-manager/ui-tests/i18n";
import RepositoryNavLink from "./RepositoryNavLink";

describe("RepositoryNavLink", () => {
  it("should render nothing, if the sources link is missing", () => {
    const repository = {
      namespace: "Namespace",
      name: "Repo",
      type: "GIT",
      _links: {}
    };

    const navLink = shallow(
      <RepositoryNavLink
        repository={repository}
        linkName="sources"
        to="/sources"
        label="Sources"
        activeOnlyWhenExact={true}
      />
    );
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const repository = {
      namespace: "Namespace",
      name: "Repo",
      type: "GIT",
      _links: {
        sources: {
          href: "/sources"
        }
      }
    };

    const navLink = mount(
      <RepositoryNavLink
        repository={repository}
        linkName="sources"
        to="/sources"
        label="Sources"
        activeOnlyWhenExact={true}
      />
    );
    expect(navLink.text()).toBe("Sources");
  });
});
