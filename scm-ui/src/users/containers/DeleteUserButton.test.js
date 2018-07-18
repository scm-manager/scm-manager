import React from "react";
import { configure, mount, shallow } from "enzyme";
import DeleteUserButton from "./DeleteUserButton";
import Adapter from "enzyme-adapter-react-16";

import { confirmAlert } from "../../components/ConfirmAlert";
jest.mock("../../components/ConfirmAlert");

import "raf/polyfill";

configure({ adapter: new Adapter() });

describe("DeleteUserButton", () => {
  it("should render nothing, if the delete link is missing", () => {
    const entry = {
      entry: {
        _links: {}
      }
    };

    const button = shallow(
      <DeleteUserButton entry={entry} deleteUser={() => {}} />
    );
    expect(button.text()).toBe("");
  });

  it("should render the button", () => {
    const entry = {
      entry: {
        _links: {
          delete: {
            href: "/users"
          }
        }
      }
    };

    const button = mount(
      <DeleteUserButton entry={entry} deleteUser={() => {}} />
    );

    console.log(button);

    expect(button.text()).not.toBe("");
  });

  it("should open the confirm dialog on button click", () => {
    const entry = {
      entry: {
        _links: {
          delete: {
            href: "/users"
          }
        }
      }
    };

    const button = mount(
      <DeleteUserButton entry={entry} deleteUser={() => {}} />
    );
    button.simulate("click");

    expect(confirmAlert.mock.calls.length).toBe(1);
  });

  it("should call the delete user function with delete url", () => {
    const entry = {
      entry: {
        _links: {
          delete: {
            href: "/users"
          }
        }
      }
    };

    let calledUrl = null;
    function capture(user) {
      calledUrl = user._links.delete.href;
    }

    const button = mount(
      <DeleteUserButton
        entry={entry}
        confirmDialog={false}
        deleteUser={capture}
      />
    );
    button.simulate("click");

    expect(calledUrl).toBe("/users");
  });
});
