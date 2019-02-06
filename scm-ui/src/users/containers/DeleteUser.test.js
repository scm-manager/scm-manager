import React from "react";
import { mount } from "enzyme";
import ReactRouterEnzymeContext from "react-router-enzyme-context";
import configureStore from "redux-mock-store";

import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteUser from "./DeleteUser";

import { confirmAlert } from "@scm-manager/ui-components";

jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  Subtitle: require.requireActual("@scm-manager/ui-components").Subtitle,
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton,
  ErrorNotification: ({err}) => err ? err.message : null
}));

const options = new ReactRouterEnzymeContext();

describe("DeleteUser", () => {

  let store;

  beforeEach(() => {
    store = configureStore()({});
  });

  it("should render nothing, if the delete link is missing", () => {
    const user = {
      _links: {}
    };

    const navLink = mount(
      <DeleteUser user={user} store={store} />,
      options.get()
    );
    expect(navLink.text()).toBeNull();
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
      <DeleteUser user={user} store={store} />,
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
      <DeleteUser user={user} store={store} />,
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

    store.dispatch = jest.fn();

    const navLink = mount(
      <DeleteUser
        user={user}
        confirmDialog={false}
        store={store}
      />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(store.dispatch.mock.calls.length).toBe(1);
  });
});
