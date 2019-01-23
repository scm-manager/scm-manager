import React from "react";
import { mount, shallow } from "enzyme";
import ReactRouterEnzymeContext from "react-router-enzyme-context";

import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteUser from "./DeleteUser";

import { confirmAlert } from "@scm-manager/ui-components";
jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  Subtitle: require.requireActual("@scm-manager/ui-components").Subtitle,
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton
}));

const options = new ReactRouterEnzymeContext();

describe("DeleteUser", () => {
  it("should render nothing, if the delete link is missing", () => {
    const user = {
      _links: {}
    };

    const navLink = shallow(
      <DeleteUser user={user} deleteUser={() => {}} />
    );
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const user = {
      _links: {
        delete: {
          href: "/users"
        }
      }
    };

    const navLink = mount(
      <DeleteUser user={user} deleteUser={() => {}} />,
      options.get()
    );
    expect(navLink.text()).not.toBe("");
  });

  it("should open the confirm dialog on navLink click", () => {
    const user = {
      _links: {
        delete: {
          href: "/users"
        }
      }
    };

    const navLink = mount(
      <DeleteUser user={user} deleteUser={() => {}} />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(confirmAlert.mock.calls.length).toBe(1);
  });

  it("should call the delete user function with delete url", () => {
    const user = {
      _links: {
        delete: {
          href: "/users"
        }
      }
    };

    let calledUrl = null;
    function capture(user) {
      calledUrl = user._links.delete.href;
    }

    const navLink = mount(
      <DeleteUser
        user={user}
        confirmDialog={false}
        deleteUser={capture}
      />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(calledUrl).toBe("/users");
  });
});
