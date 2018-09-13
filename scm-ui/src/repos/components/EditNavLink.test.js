import React from "react";
import { shallow, mount } from "enzyme";
import "../../tests/enzyme";
import "../../tests/i18n";
import ReactRouterEnzymeContext from "react-router-enzyme-context";
import EditNavLink from "./EditNavLink";

describe("EditNavLink", () => {
  const options = new ReactRouterEnzymeContext();

  it("should render nothing, if the modify link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = shallow(
      <EditNavLink repository={repository} editUrl="" />,
      options.get()
    );
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

    const navLink = mount(
      <EditNavLink repository={repository} editUrl="" />,
      options.get()
    );
    expect(navLink.text()).toBe("edit-nav-link.label");
  });
});
