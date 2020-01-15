import React from "react";
import { mount, shallow } from "@scm-manager/ui-tests/enzyme-router";
import "@scm-manager/ui-tests/enzyme";
import "@scm-manager/ui-tests/i18n";

import EditRepoNavLink from "./EditRepoNavLink";

describe("GeneralNavLink", () => {
  it("should render nothing, if the modify link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = shallow(<EditRepoNavLink repository={repository} editUrl="" />);
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

    const navLink = mount(<EditRepoNavLink repository={repository} editUrl="" />);
    expect(navLink.text()).toBe("repositoryRoot.menu.generalNavLink");
  });
});
