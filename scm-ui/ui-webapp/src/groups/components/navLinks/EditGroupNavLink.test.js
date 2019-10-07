//@flow

import React from "react";
import { shallow } from "enzyme";
import "../../../tests/enzyme";
import "../../../tests/i18n";
import EditGroupNavLink from "./EditGroupNavLink";

it("should render nothing, if the edit link is missing", () => {
  const group = {
      _links: {}
  };

  const navLink = shallow(<EditGroupNavLink group={group} editUrl='/group/edit'/>);
  expect(navLink.text()).toBe("");
});

it("should render the navLink", () => {
  const group = {
      _links: {
        update: {
          href: "/groups"
        }
      }
  };

  const navLink = shallow(<EditGroupNavLink group={group} editUrl='/group/edit'/>);
  expect(navLink.text()).not.toBe("");
});
