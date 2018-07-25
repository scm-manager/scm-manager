import React from "react";
import { shallow } from "enzyme";
import "../../tests/enzyme";
import "../../tests/i18n";
import EditUserButton from "./EditUserButton";

it("should render nothing, if the edit link is missing", () => {
  const entry = {
    entry: {
      _links: {}
    }
  };

  const button = shallow(<EditUserButton entry={entry} />);
  expect(button.text()).toBe("");
});

it("should render the button", () => {
  const entry = {
    entry: {
      _links: {
        update: {
          href: "/users"
        }
      }
    }
  };

  const button = shallow(<EditUserButton entry={entry} />);
  expect(button.text()).not.toBe("");
});
