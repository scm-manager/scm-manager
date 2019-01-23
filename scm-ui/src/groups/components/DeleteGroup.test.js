import React from "react";
import { mount, shallow } from "enzyme";
import ReactRouterEnzymeContext from "react-router-enzyme-context";

import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteGroup from "./DeleteGroup";

import { confirmAlert } from "@scm-manager/ui-components";
jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  Subtitle: require.requireActual("@scm-manager/ui-components").Subtitle,
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton
}));

const options = new ReactRouterEnzymeContext();

describe("DeleteGroupNavLink", () => {
  it("should render nothing, if the delete link is missing", () => {
    const group = {
      _links: {}
    };

    const navLink = shallow(
      <DeleteGroup group={group} deleteGroup={() => {}} />
    );
    expect(navLink.text()).toBe("");
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
      <DeleteGroup group={group} deleteGroup={() => {}} />,
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
      <DeleteGroup group={group} deleteGroup={() => {}} />,
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

    let calledUrl = null;
    function capture(group) {
      calledUrl = group._links.delete.href;
    }

    const navLink = mount(
      <DeleteGroup
        group={group}
        confirmDialog={false}
        deleteGroup={capture}
      />,
      options.get()
    );
    navLink.find("button").simulate("click");

    expect(calledUrl).toBe("/groups");
  });
});
