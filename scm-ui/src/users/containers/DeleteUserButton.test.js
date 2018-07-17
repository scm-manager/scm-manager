import React from "react";
import { configure, shallow } from "enzyme";
import DeleteUserButton from "./DeleteUserButton";
import Adapter from "enzyme-adapter-react-16";

import { confirmAlert } from "../../components/ConfirmAlert";
jest.mock("../../components/ConfirmAlert");

import "raf/polyfill";

configure({ adapter: new Adapter() });

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

  const button = shallow(
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

  const button = shallow(
    <DeleteUserButton user={user} deleteUser={() => {}} />
  );
  button.simulate("click");

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

  const button = shallow(
    <DeleteUserButton user={user} confirmDialog={false} deleteUser={capture} />
  );
  button.simulate("click");

  expect(calledUrl).toBe("/users");
});
