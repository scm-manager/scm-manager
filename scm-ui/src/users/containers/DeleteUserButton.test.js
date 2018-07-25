import React from "react";
import { mount, shallow } from "enzyme";
import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteUserButton from "./DeleteUserButton";

import { confirmAlert } from "../../components/modals/ConfirmAlert";
jest.mock("../../components/modals/ConfirmAlert");

describe("DeleteUserButton", () => {
  it("should render nothing, if the delete link is missing", () => {
    const user = {
      _links: {}
    };

    const button = shallow(
      <DeleteUserButton user={user} deleteUser={() => {}} />
    );
    expect(button.text()).toBe("");
  });

  it("should render the button", () => {
    const user = {
      _links: {
        delete: {
          href: "/users"
        }
      }
    };

    const button = mount(
      <DeleteUserButton user={user} deleteUser={() => {}} />
    );
    expect(button.text()).not.toBe("");
  });

  it("should open the confirm dialog on button click", () => {
    const user = {
      _links: {
        delete: {
          href: "/users"
        }
      }
    };

    const button = mount(
      <DeleteUserButton user={user} deleteUser={() => {}} />
    );
    button.find("a").simulate("click");

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

    const button = mount(
      <DeleteUserButton
        user={user}
        confirmDialog={false}
        deleteUser={capture}
      />
    );
    button.find("a").simulate("click");

    expect(calledUrl).toBe("/users");
  });
});
