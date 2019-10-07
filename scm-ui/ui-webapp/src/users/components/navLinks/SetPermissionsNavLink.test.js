import React from "react";
import { shallow } from "enzyme";
import "../../../tests/enzyme";
import "../../../tests/i18n";
import SetPermissionsNavLink from "./SetPermissionsNavLink";

it("should render nothing, if the permissions link is missing", () => {
  const user = {
    _links: {}
  };

  const navLink = shallow(
    <SetPermissionsNavLink user={user} permissionsUrl="/user/permissions" />
  );
  expect(navLink.text()).toBe("");
});

it("should render the navLink", () => {
  const user = {
    _links: {
      permissions: {
        href: "/permissions"
      }
    }
  };

  const navLink = shallow(
    <SetPermissionsNavLink user={user} permissionsUrl="/user/permissions" />
  );
  expect(navLink.text()).not.toBe("");
});
