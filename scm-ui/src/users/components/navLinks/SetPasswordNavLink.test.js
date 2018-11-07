import React from "react";
import { shallow } from "enzyme";
import "../../../tests/enzyme";
import "../../../tests/i18n";
import ChangePasswordNavLink from "./SetPasswordNavLink";

it("should render nothing, if the password link is missing", () => {
  const user = {
    _links: {}
  };

  const navLink = shallow(
    <ChangePasswordNavLink user={user} passwordUrl="/user/password" />
  );
  expect(navLink.text()).toBe("");
});

it("should render the navLink", () => {
  const user = {
    _links: {
      password: {
        href: "/password"
      }
    }
  };

  const navLink = shallow(
    <ChangePasswordNavLink user={user} passwordUrl="/user/password" />
  );
  expect(navLink.text()).not.toBe("");
});
