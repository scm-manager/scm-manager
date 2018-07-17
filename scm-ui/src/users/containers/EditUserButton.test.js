import React from "react";
import { configure, shallow } from "enzyme";
import EditUserButton from "./EditUserButton";
import Adapter from "enzyme-adapter-react-16";

import "raf/polyfill";

configure({ adapter: new Adapter() });

it("should render nothing, if the edit link is missing", () => {
  const user = {
    _links: {}
  };

  const button = shallow(<EditUserButton user={user} />);
  expect(button.text()).toBe("");
});

it("should render the button", () => {
  const user = {
    _links: {
      update: {
        href: "/users"
      }
    }
  };

  const button = shallow(<EditUserButton user={user} />);
  expect(button.text()).not.toBe("");
});

