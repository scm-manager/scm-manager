// @flow
import React from "react";
import { shallow, mount } from "enzyme";
import "../../tests/enzyme";
import "../../tests/i18n";
import ReactRouterEnzymeContext from "react-router-enzyme-context";
import RepositoryNavLink from "./RepositoryNavLink";

describe("RepositoryNavLink", () => {
  const options = new ReactRouterEnzymeContext();

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
      />,
      options.get()
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
      />,
      options.get()
    );
    expect(navLink.text()).toBe("Sources");
  });
});
