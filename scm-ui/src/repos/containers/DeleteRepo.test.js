import React from "react";
import { mount } from "enzyme";
import ReactRouterEnzymeContext from "react-router-enzyme-context";
import configureStore from "redux-mock-store";

import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteRepo from "./DeleteRepo";

import { confirmAlert } from "@scm-manager/ui-components";

jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  Subtitle: require.requireActual("@scm-manager/ui-components").Subtitle,
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton,
  ErrorNotification: ({err}) => err ? err.message : null
}));

const options = new ReactRouterEnzymeContext();

describe("DeleteRepo", () => {

  let store;

  beforeEach(() => {
    store = configureStore()({});
  });

  it("should render nothing, if the delete link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = mount(
      <DeleteRepo repository={repository} store={store} />,
      options.get()
    );
    expect(navLink.text()).toBeNull();
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
      <DeleteRepo repository={repository} store={store} />,
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
      <DeleteRepo repository={repository} store={store} />,
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

    store.dispatch = jest.fn();

    const navLink = mount(
      <DeleteRepo
        repository={repository}
        confirmDialog={false}
        store={store}
      />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(store.dispatch.mock.calls.length).toBe(1);
  });
});
