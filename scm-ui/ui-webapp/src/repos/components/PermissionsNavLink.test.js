import React from "react";
import {mount, shallow } from "@scm-manager/ui-tests/enzyme-router";
import "@scm-manager/ui-tests/i18n";
import ReactRouterEnzymeContext from "react-router-enzyme-context";
import PermissionsNavLink from "./PermissionsNavLink";

describe("PermissionsNavLink", () => {
  const options = new ReactRouterEnzymeContext();

  it("should render nothing, if the modify link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = shallow(
      <PermissionsNavLink repository={repository} permissionUrl="" />,
      options.get()
    );
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const repository = {
      _links: {
        permissions: {
          href: "/permissions"
        }
      }
    };

    const navLink = mount(
      <PermissionsNavLink repository={repository} permissionUrl="" />,
      options.get()
    );
    expect(navLink.text()).toBe("repositoryRoot.menu.permissionsNavLink");
  });
});
