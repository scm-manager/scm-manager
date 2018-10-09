import React from "react";
import { mount, shallow } from "enzyme";
import "../../../../tests/enzyme";
import "../../../../tests/i18n";
import DeletePermissionButton from "./DeletePermissionButton";

import { confirmAlert } from "@scm-manager/ui-components";
jest.mock("@scm-manager/ui-components", () => ({
  confirmAlert: jest.fn(),
  DeleteButton: require.requireActual("@scm-manager/ui-components").DeleteButton
}));

describe("DeletePermissionButton", () => {
  it("should render nothing, if the delete link is missing", () => {
    const permission = {
      _links: {}
    };

    const navLink = shallow(
      <DeletePermissionButton
        permission={permission}
        deletePermission={() => {}}
      />
    );
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const permission = {
      _links: {
        delete: {
          href: "/permission"
        }
      }
    };

    const navLink = mount(
      <DeletePermissionButton
        permission={permission}
        deletePermission={() => {}}
      />
    );
    expect(navLink.text()).not.toBe("");
  });

  it("should open the confirm dialog on button click", () => {
    const permission = {
      _links: {
        delete: {
          href: "/permission"
        }
      }
    };

    const button = mount(
      <DeletePermissionButton
        permission={permission}
        deletePermission={() => {}}
      />
    );
    button.find("button").simulate("click");

    expect(confirmAlert.mock.calls.length).toBe(1);
  });

  it("should call the delete permission function with delete url", () => {
    const permission = {
      _links: {
        delete: {
          href: "/permission"
        }
      }
    };

    let calledUrl = null;
    function capture(permission) {
      calledUrl = permission._links.delete.href;
    }

    const button = mount(
      <DeletePermissionButton
        permission={permission}
        confirmDialog={false}
        deletePermission={capture}
      />
    );
    button.find("button").simulate("click");

    expect(calledUrl).toBe("/permission");
  });
});
