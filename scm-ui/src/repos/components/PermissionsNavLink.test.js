import React from "react";
import { mount, shallow } from "enzyme";
import "../../tests/enzyme";
import "../../tests/i18n";
import PermissionsNavLink from "./PermissionsNavLink";

jest.mock("@scm-manager/ui-components", () => ({
  NavAction: require.requireActual("@scm-manager/ui-components").NavAction
}));

describe("PermissionsNavLink", () => {
  it("should render nothing, if the modify link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = shallow(
      <PermissionsNavLink repository={repository} permissionUrl="" />
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
      <PermissionsNavLink repository={repository} permissionUrl="" />
    );
    expect(navLink.text()).toBe("foo");
  });
});
