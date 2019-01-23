import React from "react";
import { shallow } from "enzyme";
import "../../../tests/enzyme";
import "../../../tests/i18n";
import GeneralUserNavLink from "./GeneralUserNavLink";

it("should render nothing, if the edit link is missing", () => {
  const user = {
      _links: {}
  };

  const navLink = shallow(<GeneralUserNavLink user={user} editUrl='/user/edit'/>);
  expect(navLink.text()).toBe("");
});

it("should render the navLink", () => {
  const user = {
      _links: {
        update: {
          href: "/users"
        }
      }
  };

  const navLink = shallow(<GeneralUserNavLink user={user} editUrl='/user/edit'/>);
  expect(navLink.text()).not.toBe("");
});
