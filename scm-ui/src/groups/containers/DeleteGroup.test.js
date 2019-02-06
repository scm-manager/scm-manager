import React from "react";
import { mount } from "enzyme";
import ReactRouterEnzymeContext from "react-router-enzyme-context";
import configureStore from "redux-mock-store";

import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteGroup from "./DeleteGroup";

import { confirmAlert } from "@scm-manager/ui-components";

jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  Subtitle: require.requireActual("@scm-manager/ui-components").Subtitle,
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton,
  ErrorNotification: ({err}) => err ? err.message : null
}));

const options = new ReactRouterEnzymeContext();

describe("DeleteGroupNavLink", () => {

  let store;

  beforeEach(() => {
    store = configureStore()({});
  });

  it("should render nothing, if the delete link is missing", () => {
    const group = {
      _links: {}
    };

    const navLink = mount(
      <DeleteGroup group={group} store={store} />,
      options.get()
    );
    expect(navLink.text()).toBeNull();
  });

  it("should render the navLink", () => {
    const group = {
      _links: {
        delete: {
          href: "/groups"
        }
      }
    };

    const navLink = mount(
      <DeleteGroup group={group} store={store} />,
      options.get()
    );
    expect(navLink.text()).not.toBe("");
  });

  it("should open the confirm dialog on navLink click", () => {
    const group = {
      _links: {
        delete: {
          href: "/groups"
        }
      }
    };

    const navLink = mount(
      <DeleteGroup group={group} store={store} />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(confirmAlert.mock.calls.length).toBe(1);
  });

  it("should call the delete group function with delete url", () => {
    const group = {
      _links: {
        delete: {
          href: "/groups"
        }
      }
    };

    store.dispatch = jest.fn();

    const navLink = mount(
      <DeleteGroup
        group={group}
        confirmDialog={false}
        store={store}
      />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(store.dispatch.mock.calls.length).toBe(1);
  });
});
