// @flow
import React from "react";
import { shallow, mount } from "@scm-manager/ui-tests/enzyme-router";
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
