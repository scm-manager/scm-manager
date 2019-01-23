import React from "react";
import { mount, shallow } from "enzyme";
import ReactRouterEnzymeContext from "react-router-enzyme-context";

import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteRepo from "./DeleteRepo";

import { confirmAlert } from "@scm-manager/ui-components";
jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  Subtitle: require.requireActual("@scm-manager/ui-components").Subtitle,
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton
}));

const options = new ReactRouterEnzymeContext();

describe("DeleteRepo", () => {
  it("should render nothing, if the delete link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = shallow(
      <DeleteRepo repository={repository} delete={() => {}} />
    );
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const repository = {
      _links: {
        delete: {
          href: "/repositories"
        }
      }
    };

    const navLink = mount(
      <DeleteRepo repository={repository} delete={() => {}} />,
      options.get()
    );
    expect(navLink.text()).not.toBe("");
  });

  it("should open the confirm dialog on navLink click", () => {
    const repository = {
      _links: {
        delete: {
          href: "/repositorys"
        }
      }
    };

    const navLink = mount(
      <DeleteRepo repository={repository} delete={() => {}} />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(confirmAlert.mock.calls.length).toBe(1);
  });

  it("should call the delete repository function with delete url", () => {
    const repository = {
      _links: {
        delete: {
          href: "/repos"
        }
      }
    };

    let calledUrl = null;
    function capture(repository) {
      calledUrl = repository._links.delete.href;
    }

    const navLink = mount(
      <DeleteRepo
        repository={repository}
        confirmDialog={false}
        delete={capture}
      />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(calledUrl).toBe("/repos");
  });
});
