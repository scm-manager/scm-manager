import React from "react";
import { mount, shallow } from "enzyme";
import "../../../tests/enzyme";
import "../../../tests/i18n";
import DeleteUserNavLink from "./DeleteUserNavLink";

import { confirmAlert } from "../../../components/modals/ConfirmAlert";
jest.mock("../../../components/modals/ConfirmAlert");

describe("DeleteUserNavLink", () => {
  it("should render nothing, if the delete link is missing", () => {
    const user = {
      _links: {}
    };

    const navLink = shallow(
      <DeleteUserNavLink user={user} deleteUser={() => {}} />
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
      <DeleteUserNavLink user={user} deleteUser={() => {}} />
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
      <DeleteUserNavLink user={user} deleteUser={() => {}} />
    );
    navLink.find("a").simulate("click");

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
      <DeleteUserNavLink
        user={user}
        confirmDialog={false}
        deleteUser={capture}
      />
    );
    navLink.find("a").simulate("click");

    expect(calledUrl).toBe("/users");
  });
});
