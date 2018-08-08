import React from "react";
import { mount, shallow } from "enzyme";
import "../../tests/enzyme";
import "../../tests/i18n";
import EditNavLink from "./EditNavLink";

jest.mock("../../components/modals/ConfirmAlert");
jest.mock("../../components/navigation/NavLink", () => () => <div>foo</div>);

describe("EditNavLink", () => {
  it("should render nothing, if the modify link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = shallow(<EditNavLink repository={repository} editUrl="" />);
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const repository = {
      _links: {
        update: {
          href: "/repositories"
        }
      }
    };

    const navLink = mount(<EditNavLink repository={repository} editUrl="" />);
    expect(navLink.text()).toBe("foo");
  });
});
